package ru.itis.meshy.android

import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PACKAGE_ADDED
import android.content.Intent.ACTION_PACKAGE_CHANGED
import android.content.Intent.ACTION_PACKAGE_REMOVED
import android.content.Intent.ACTION_PACKAGE_REPLACED
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
import android.content.pm.PackageInfo
import android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_PERMISSIONS
import android.content.pm.PackageManager.GET_SIGNATURES
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.UiThread
import ru.itis.messaging_engine.api.lifecycle.Service
import ru.itis.messaging_engine.api.system.AndroidExecutor
import ru.itis.messaging_engine.util.AndroidUtils.registerReceiver
import ru.itis.messaging_engine.util.LogUtils.logException
import ru.itis.messaging_engine.util.StringUtils
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.api.android.ScreenFilterMonitor
import ru.itis.meshy.api.android.ScreenFilterMonitor.AppDetails
import java.io.ByteArrayInputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Сканирует установленные приложения, запросившие
 * `SYSTEM_ALERT_WINDOW`, и возвращает только подозрительные —
 * исключая системные, Google Play Services (по подписи) и явно
 * разрешённые пользователем.
 *
 * Слушает изменения списка пакетов через `BroadcastReceiver`, чтобы
 * сбрасывать кэш при установке/удалении/обновлении приложений.
 */
@NotNullByDefault
internal class ScreenFilterMonitorImpl @Inject constructor(
    private val app: Application,
    private val androidExecutor: AndroidExecutor,
    private val prefs: SharedPreferences,
) : ScreenFilterMonitor, Service {

    private val pm: PackageManager = app.packageManager
    private val used = AtomicBoolean(false)

    // UiThread
    private var receiver: BroadcastReceiver? = null

    // UiThread
    private var cachedApps: Collection<AppDetails>? = null

    @UiThread
    override fun getApps(): Collection<AppDetails> {
        cachedApps?.let { return it }

        val allowed: Set<String> =
            prefs.getStringSet(PREF_KEY_ALLOWED, emptySet()) ?: emptySet()

        @SuppressLint("QueryPermissionsNeeded")
        @Suppress("DEPRECATION") // getInstalledPackages(int) deprecated в API 33,
        // PackageInfoFlags заменитель требует SDK 33+ → отдельная миграция
        val packageInfos: List<PackageInfo> = pm.getInstalledPackages(GET_PERMISSIONS)

        val apps = packageInfos
            .asSequence()
            .filter { it.packageName !in allowed && isOverlayApp(it) }
            .map { AppDetails(getAppName(it), it.packageName) }
            .sortedBy { it.name }
            .toList()

        val immutable: List<AppDetails> = java.util.Collections.unmodifiableList(apps)
        cachedApps = immutable
        return immutable
    }

    @UiThread
    override fun allowApps(packageNames: Collection<String>) {
        cachedApps = null
        val allowed = prefs.getStringSet(PREF_KEY_ALLOWED, emptySet()) ?: emptySet()
        val merged = HashSet(allowed).apply { addAll(packageNames) }
        prefs.edit().putStringSet(PREF_KEY_ALLOWED, merged).apply()
    }

    /**
     * Returns the application name for a given package, or the package name
     * if no application name is available.
     */
    private fun getAppName(pkgInfo: PackageInfo): String {
        val seq = pm.getApplicationLabel(pkgInfo.applicationInfo!!)
        return seq.toString().ifEmpty { pkgInfo.packageName }
    }

    /**
     * Checks if an installed package is a user app using the permission.
     */
    private fun isOverlayApp(packageInfo: PackageInfo): Boolean {
        val mask = FLAG_SYSTEM or FLAG_UPDATED_SYSTEM_APP
        // Ignore system apps
        if ((packageInfo.applicationInfo!!.flags and mask) != 0) return false
        // Ignore Play Services, it's effectively a system app
        if (isPlayServices(packageInfo.packageName)) return false

        val requestedPermissions = packageInfo.requestedPermissions ?: return false
        return if (SDK_INT < 23) {
            // Check whether the permission has been requested and granted
            val flags = packageInfo.requestedPermissionsFlags
            requestedPermissions.indexOfFirst { it == SYSTEM_ALERT_WINDOW }
                .takeIf { it >= 0 }
                ?.let { idx ->
                    // 'flags' may be null on Robolectric
                    flags == null || (flags[idx] and REQUESTED_PERMISSION_GRANTED) != 0
                } ?: false
        } else {
            // Check whether the permission has been requested
            SYSTEM_ALERT_WINDOW in requestedPermissions
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun isPlayServices(pkg: String): Boolean {
        if (PLAY_SERVICES_PACKAGE != pkg) return false
        return try {
            @Suppress("DEPRECATION") // getPackageInfo(name, GET_SIGNATURES)
            val sigs = pm.getPackageInfo(pkg, GET_SIGNATURES)

            @Suppress("DEPRECATION") // PackageInfo#signatures, заменитель — signingInfo (SDK 28+)
            val signatures = sigs.signatures
            // The genuine Play Services app should have a single signature
            if (signatures == null || signatures.size != 1) return false

            // Extract the public key from the signature
            val certFactory = CertificateFactory.getInstance("X509")
            val signatureBytes = signatures[0].toByteArray()
            val cert = certFactory.generateCertificate(
                ByteArrayInputStream(signatureBytes)
            ) as X509Certificate
            val publicKeyBytes = cert.publicKey.encoded
            val publicKey = StringUtils.toHexString(publicKeyBytes)
            PLAY_SERVICES_PUBLIC_KEY == publicKey
        } catch (e: NameNotFoundException) {
            logException(LOG, WARNING, e)
            false
        } catch (e: CertificateException) {
            logException(LOG, WARNING, e)
            false
        }
    }

    override fun startService() {
        check(!used.getAndSet(true)) { "ScreenFilterMonitor already started" }
        androidExecutor.runOnUiThread {
            val filter = IntentFilter().apply {
                addAction(ACTION_PACKAGE_ADDED)
                addAction(ACTION_PACKAGE_CHANGED)
                addAction(ACTION_PACKAGE_REMOVED)
                addAction(ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            val r = PackageBroadcastReceiver()
            receiver = r
            registerReceiver(app, r, filter, false)
            cachedApps = null
        }
    }

    override fun stopService() {
        androidExecutor.runOnUiThread {
            receiver?.let { app.unregisterReceiver(it) }
        }
    }

    private inner class PackageBroadcastReceiver : BroadcastReceiver() {

        @UiThread
        override fun onReceive(context: Context, intent: Intent) {
            cachedApps = null
        }
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(ScreenFilterMonitorImpl::class.java.name)

        /*
         * Ignore Play Services if it uses this package name and public key —
         * it's effectively a system app, but not flagged as such on older systems.
         */
        private const val PLAY_SERVICES_PACKAGE = "com.google.android.gms"
        private const val PLAY_SERVICES_PUBLIC_KEY =
            "30820120300D06092A864886F70D01010105000382010D0030820108" +
                "0282010100AB562E00D83BA208AE0A966F124E29DA11F2AB56D08F58" +
                "E2CCA91303E9B754D372F640A71B1DCB130967624E4656A7776A9219" +
                "3DB2E5BFB724A91E77188B0E6A47A43B33D9609B77183145CCDF7B2E" +
                "586674C9E1565B1F4C6A5955BFF251A63DABF9C55C27222252E875E4" +
                "F8154A645F897168C0B1BFC612EABF785769BB34AA7984DC7E2EA276" +
                "4CAE8307D8C17154D7EE5F64A51A44A602C249054157DC02CD5F5C0E" +
                "55FBEF8519FBE327F0B1511692C5A06F19D18385F5C4DBC2D6B93F68" +
                "CC2979C70E18AB93866B3BD5DB8999552A0E3B4C99DF58FB918BEDC1" +
                "82BA35E003C1B4B10DD244A8EE24FFFD333872AB5221985EDAB0FC0D" +
                "0B145B6AA192858E79020103"

        private const val PREF_KEY_ALLOWED = "allowedOverlayApps"
    }
}