package ru.itis.meshy.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SHUTDOWN
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import android.os.Process.myPid
import androidx.annotation.UiThread
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import com.bumptech.glide.Glide
import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager
import ru.itis.messaging_engine.api.account.AccountManager
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.ALREADY_RUNNING
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.SUCCESS
import ru.itis.messaging_engine.api.system.AndroidExecutor
import ru.itis.messaging_engine.api.system.Clock
import ru.itis.messaging_engine.util.AndroidUtils
import ru.itis.messaging_engine.util.AndroidUtils.isUiThread
import ru.itis.meshy.R
import ru.itis.meshy.android.MeshyApplication.Companion.ENTRY_ACTIVITY
import ru.itis.meshy.android.logout.HideUiActivity
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ONGOING_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ONGOING_CHANNEL_OLD_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ONGOING_NOTIFICATION_ID
import ru.itis.meshy.api.android.LockManager
import ru.itis.meshy.api.android.LockManager.Companion.ACTION_LOCK
import ru.itis.meshy.api.android.LockManager.Companion.EXTRA_PID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level.INFO
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import javax.inject.Inject

/**
 * Главный foreground-service приложения (бывший `BriarService`).
 * Удерживает lifecycle ядра meshy/MessagingEngine в живом состоянии,
 * пока запущен — иначе Android может прибить процесс приложения.
 *
 * Помечен `open`, потому что [MeshyServiceConnection.waitForBinder]
 * возвращает `IBinder`, который потребитель приводит к [MeshyBinder]
 * и через него получает сам экземпляр сервиса. Inner-классы тоже `open`.
 */
open class MeshyService : Service() {

    private val created = AtomicBoolean(false)
    private val binder: Binder = MeshyBinder()

    private var receiver: BroadcastReceiver? = null
    private lateinit var app: MeshyApplication

    @Inject lateinit var notificationManager: ru.itis.meshy.api.android.AndroidNotificationManager
    @Inject lateinit var accountManager: AccountManager
    @Inject lateinit var lockManager: LockManager
    @Inject lateinit var wakeLockManager: AndroidWakeLockManager

    // Fields that are accessed from background threads must be volatile.
    @JvmField @Volatile @Inject var lifecycleManager: LifecycleManager? = null
    @JvmField @Volatile @Inject var androidExecutor: AndroidExecutor? = null
    @JvmField @Volatile @Inject var clock: Clock? = null

    @Volatile private var started = false
    @Volatile private var glideCacheCleared: Long = 0

    override fun onCreate() {
        super.onCreate()

        app = application as MeshyApplication
        app.getApplicationComponent().inject(this)

        LOG.info("Created")
        if (created.getAndSet(true)) {
            LOG.info("Already created")
            stopSelf()
            return
        }
        val dbKey = accountManager.databaseKey
        if (dbKey == null) {
            LOG.info("No database key")
            stopSelf()
            return
        }

        // Hold a wake lock during startup
        wakeLockManager.runWakefully(Runnable {
            // Create notification channels
            if (SDK_INT >= 26) {
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                // Delete the old notification channel, which had
                // IMPORTANCE_NONE and showed a badge.
                nm.deleteNotificationChannel(ONGOING_CHANNEL_OLD_ID)
                // Use IMPORTANCE_LOW so the system doesn't show its own
                // notification on API 26-27.
                val ongoingChannel = NotificationChannel(
                    ONGOING_CHANNEL_ID,
                    getString(R.string.ongoing_notification_title),
                    NotificationManager.IMPORTANCE_LOW,
                )
                ongoingChannel.lockscreenVisibility = VISIBILITY_SECRET
                ongoingChannel.setShowBadge(false)
                nm.createNotificationChannel(ongoingChannel)
            }
            val foregroundNotification: Notification =
                notificationManager.getForegroundNotification()
            startForeground(ONGOING_NOTIFICATION_ID, foregroundNotification)

            // Start the services in a background thread
            wakeLockManager.executeWakefully(Runnable {
                val result: StartResult = lifecycleManager!!.startServices(dbKey)
                when (result) {
                    SUCCESS -> started = true
                    ALREADY_RUNNING -> {
                        LOG.warning("Already running")
                        // Ядро пережило исходный экземпляр MeshyService.
                        // Мы не умеем восстанавливаться из такого состояния —
                        // пытаемся выйти штатно.
                        shutdownFromBackground()
                    }
                    else -> {
                        if (LOG.isLoggable(WARNING)) {
                            LOG.warning("Startup failed: $result")
                        }
                        showStartupFailure(result)
                        stopSelf()
                    }
                }
            }, "LifecycleStartup")

            // Register for device shutdown broadcasts
            val r = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    LOG.info("Device is shutting down")
                    shutdownFromBackground()
                }
            }
            receiver = r
            val filter = IntentFilter().apply {
                addAction(ACTION_SHUTDOWN)
                addAction("android.intent.action.QUICKBOOT_POWEROFF")
                addAction("com.htc.intent.action.QUICKBOOT_POWEROFF")
            }
            AndroidUtils.registerReceiver(applicationContext, r, filter, false)
        }, "LifecycleStartup")
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(Localizer.getInstance().setLocale(base))
        Localizer.getInstance().setLocale(this)
    }

    private fun showStartupFailure(result: StartResult) {
        androidExecutor!!.runOnUiThread(Runnable {
            // Bring the entry activity to the front to clear the back stack.
            val i = Intent(this@MeshyService, ENTRY_ACTIVITY)
            i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
            i.putExtra(EXTRA_STARTUP_FAILED, true)
            i.putExtra(EXTRA_START_RESULT, result)
            startActivity(i)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_LOCK == intent.action) {
            val pid = intent.getIntExtra(EXTRA_PID, -1)
            if (pid == myPid()) {
                lockManager.setLocked(true)
            } else if (LOG.isLoggable(WARNING)) {
                LOG.warning("Tried to lock process $pid but this is ${myPid()}")
            }
        }
        return START_NOT_STICKY // Don't restart automatically if killed
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        LOG.info("Destroyed")
        // Stop the lifecycle, if not already stopped
        shutdown(false)
        @Suppress("DEPRECATION") // stopForeground(boolean) deprecated в API 33;
        // STOP_FOREGROUND_REMOVE — отдельная миграция вместе с переходом на NotificationCompat'овский путь.
        stopForeground(true)
        receiver?.let { applicationContext.unregisterReceiver(it) }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        LOG.warning("Memory is low")
        maybeClearGlideCache()
        // If we're not in the foreground, clear the UI to save memory.
        if (app.isRunningInBackground()) hideUi()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> LOG.info("Trim memory: UI hidden")
            TRIM_MEMORY_BACKGROUND -> LOG.info("Trim memory: added to LRU list")
            TRIM_MEMORY_MODERATE -> LOG.info("Trim memory: near middle of LRU list")
            TRIM_MEMORY_COMPLETE -> LOG.info("Trim memory: near end of LRU list")
            TRIM_MEMORY_RUNNING_MODERATE -> {
                LOG.info("Trim memory: running moderately low")
                maybeClearGlideCache()
            }
            TRIM_MEMORY_RUNNING_LOW -> {
                LOG.info("Trim memory: running low")
                maybeClearGlideCache()
            }
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                LOG.warning("Trim memory: running critically low")
                maybeClearGlideCache()
                if (app.isRunningInBackground()) hideUi()
            }
            else -> if (LOG.isLoggable(INFO)) LOG.info("Trim memory: unknown level $level")
        }
    }

    private fun maybeClearGlideCache() {
        if (isUiThread()) {
            maybeClearGlideCacheUiThread()
        } else {
            LOG.warning("Low memory callback was not called on main thread")
            androidExecutor!!.runOnUiThread(Runnable(::maybeClearGlideCacheUiThread))
        }
    }

    @UiThread
    private fun maybeClearGlideCacheUiThread() {
        val now = clock!!.currentTimeMillis()
        if (now - glideCacheCleared >= MIN_GLIDE_CACHE_CLEAR_INTERVAL_MS) {
            LOG.info("Clearing Glide cache")
            Glide.get(applicationContext).clearMemory()
            glideCacheCleared = now
        }
    }

    private fun hideUi() {
        val i = Intent(this, HideUiActivity::class.java)
        i.addFlags(
            FLAG_ACTIVITY_NEW_TASK or
                    FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    FLAG_ACTIVITY_NO_ANIMATION or
                    FLAG_ACTIVITY_CLEAR_TASK,
        )
        startActivity(i)
    }

    private fun shutdownFromBackground() {
        // Hold a wake lock during shutdown
        wakeLockManager.runWakefully(Runnable {
            // Begin lifecycle shutdown
            shutdown(true)
            // Hide the UI
            hideUi()
            // Wait for shutdown to complete, then exit
            wakeLockManager.executeWakefully(Runnable {
                try {
                    if (started) lifecycleManager!!.waitForShutdown()
                } catch (e: InterruptedException) {
                    LOG.info("Interrupted while waiting for shutdown")
                }
                LOG.info("Exiting")
                if (!app.isInstrumentationTest()) {
                    System.exit(0)
                }
            }, "BackgroundShutdown")
        }, "BackgroundShutdown")
    }

    /** Waits for all services to start before returning. */
    @Throws(InterruptedException::class)
    fun waitForStartup() {
        lifecycleManager!!.waitForStartup()
    }

    /** Waits for all services to stop before returning. */
    @Throws(InterruptedException::class)
    fun waitForShutdown() {
        lifecycleManager!!.waitForShutdown()
    }

    /** Starts the shutdown process. */
    fun shutdown(stopAndroidService: Boolean) {
        // Hold a wake lock during shutdown
        wakeLockManager.runWakefully(Runnable {
            // Stop the lifecycle services in a background thread, then
            // stop this Android service if needed.
            wakeLockManager.executeWakefully(Runnable {
                if (started) lifecycleManager!!.stopServices()
                if (stopAndroidService) {
                    androidExecutor!!.runOnUiThread(Runnable { stopSelf() })
                }
            }, "LifecycleShutdown")
        }, "LifecycleShutdown")
    }

    /**
     * Inner-Binder, через который связанный клиент получает сам экземпляр
     * сервиса. Был `public class BriarBinder extends Binder` — теперь
     * `MeshyBinder`. Inner — чтобы `this@MeshyService` был доступен.
     */
    open inner class MeshyBinder : Binder() {
        /** Returns the bound service. */
        fun getService(): MeshyService = this@MeshyService
    }

    /**
     * Bind-helper, ожидающий установления соединения через [CountDownLatch].
     * Был `public static class BriarServiceConnection` — теперь nested-static
     * `MeshyServiceConnection` (без `inner`).
     */
    open class MeshyServiceConnection : ServiceConnection {

        private val binderLatch = CountDownLatch(1)

        @Volatile
        private var binder: IBinder? = null

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            this.binder = binder
            binderLatch.countDown()
        }

        override fun onServiceDisconnected(name: ComponentName) = Unit

        /** Waits for the service to connect and returns its binder. */
        @Throws(InterruptedException::class)
        fun waitForBinder(): IBinder {
            binderLatch.await()
            return binder!!
        }
    }

    companion object {
        // Расширенные строки переименованы под новый namespace приложения.
        // Это внутри-приложенческие ключи extras, никаких внешних
        // потребителей у них нет, поэтому переименование безопасно.
        const val EXTRA_START_RESULT = "ru.itis.meshy.START_RESULT"
        const val EXTRA_STARTUP_FAILED = "ru.itis.meshy.STARTUP_FAILED"

        private val LOG: Logger = Logger.getLogger(MeshyService::class.java.name)

        /**
         * Don't clear the Glide cache repeatedly if low memory warnings arrive
         * in quick succession.
         */
        private const val MIN_GLIDE_CACHE_CLEAR_INTERVAL_MS: Long = 5000L
    }
}