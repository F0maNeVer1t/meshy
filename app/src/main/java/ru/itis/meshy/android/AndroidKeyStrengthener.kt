package ru.itis.meshy.android

import android.os.Build.VERSION.SDK_INT
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.KEY_ALGORITHM_HMAC_SHA256
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import androidx.annotation.RequiresApi
import ru.itis.messaging_engine.api.crypto.KeyStrengthener
import ru.itis.messaging_engine.api.crypto.SecretKey
import ru.itis.messaging_engine.util.LogUtils.logException
import org.briarproject.nullsafety.NotNullByDefault
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import java.security.spec.AlgorithmParameterSpec
import java.util.logging.Level.INFO
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import javax.annotation.concurrent.GuardedBy
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey as JcaSecretKey

/**
 * Усиливает входной [SecretKey] HMAC-SHA256 с ключом, лежащим в
 * AndroidKeyStore. На устройствах с StrongBox (Pixel 3+, некоторые
 * Samsung'и) ключ генерируется в защищённом hardware-чипе.
 *
 * Внутри используются два типа `SecretKey`: bramble'овский
 * `org.briarproject.bramble.api.crypto.SecretKey` (на входе и выходе) и
 * JCA-шный `javax.crypto.SecretKey` (хранится в keystore). Чтобы не
 * путать, JCA-ключ импортирован под алиасом [JcaSecretKey].
 *
 * Все методы — `@Synchronized`, т.е. лок на `this`. Та же семантика,
 * что и у `synchronized`-методов в Java.
 */
@RequiresApi(23)
@NotNullByDefault
internal class AndroidKeyStrengthener : KeyStrengthener {

    private val specs: List<AlgorithmParameterSpec>

    init {
        val noStrongBox = KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_SIGN)
            .setKeySize(KEY_BITS)
            .build()
        specs = if (SDK_INT >= 28) {
            // Prefer StrongBox if available
            val strongBox = KeyGenParameterSpec.Builder(KEY_ALIAS, PURPOSE_SIGN)
                .setIsStrongBoxBacked(true)
                .setKeySize(KEY_BITS)
                .build()
            listOf(strongBox, noStrongBox)
        } else {
            listOf(noStrongBox)
        }
    }

    @GuardedBy("this")
    private var storedKey: JcaSecretKey? = null

    @Synchronized
    override fun isInitialised(): Boolean {
        if (storedKey != null) return true
        try {
            val ks = KeyStore.getInstance(KEY_STORE_TYPE)
            ks.load(null)
            val entry = ks.getEntry(KEY_ALIAS, null)
            if (entry is SecretKeyEntry) {
                storedKey = entry.secretKey
                LOG.info("Loaded key from keystore")
                return true
            }
            return false
        } catch (e: GeneralSecurityException) {
            logException(LOG, WARNING, e)
            return false
        } catch (e: IOException) {
            // Соответствует Java-оригиналу: IOException из ks.load(null) —
            // программная ошибка, не recoverable.
            throw RuntimeException(e)
        }
    }

    @Synchronized
    override fun strengthenKey(k: SecretKey): SecretKey {
        try {
            if (!isInitialised()) initialise()
            // Use the input key and the stored key to derive the output key
            val mac = Mac.getInstance(KEY_ALGORITHM_HMAC_SHA256)
            mac.init(storedKey)
            return SecretKey(mac.doFinal(k.bytes))
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }
    }

    @Synchronized
    @Throws(GeneralSecurityException::class)
    private fun initialise() {
        // Try the parameter specs in order of preference
        for (spec in specs) {
            try {
                val kg = KeyGenerator.getInstance(KEY_ALGORITHM_HMAC_SHA256, PROVIDER_NAME)
                kg.init(spec)
                storedKey = kg.generateKey()
                LOG.info("Stored key in keystore")
                return
            } catch (e: Exception) {
                if (LOG.isLoggable(INFO)) {
                    LOG.info("Could not generate key: $e")
                }
                // Fall back to next spec
            }
        }
        throw GeneralSecurityException("Could not generate key")
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(AndroidKeyStrengthener::class.java.name)

        private const val KEY_STORE_TYPE = "AndroidKeyStore"
        private const val PROVIDER_NAME = "AndroidKeyStore"
        private const val KEY_ALIAS = "db"
        private const val KEY_BITS = 256
    }
}