package ru.itis.meshy.android

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.preference.PreferenceManager
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import ru.itis.messaging_engine.MessagingEngineAndroidEagerSingletons
import ru.itis.messaging_engine.MessagingEngineAppComponent
import ru.itis.messaging_engine.BrambleCoreEagerSingletons
import ru.itis.meshy.MeshyCoreEagerSingletons
import ru.itis.meshy.R
import ru.itis.meshy.android.logging.CachingLogHandler
import ru.itis.meshy.android.settings.DisplayFragment.PREF_THEME
import ru.itis.meshy.android.util.UiUtils
import java.util.logging.Level.INFO
import java.util.logging.Logger

/**
 * Главный [Application] класс приложения.
 *
 * `open`, потому что наследуется в инструментационных тестах для подмены
 * graphа DI (см. [createApplicationComponent] — `protected open`).
 *
 * Раньше использовал `TestingConstants.IS_DEBUG_BUILD` для включения
 * StrictMode и подъёма логирования до уровня FINE. Поскольку проект
 * теперь имеет один build type, эти ветки удалены — поведение
 * соответствует тому, что было в production-сборке Briar'а.
 */
open class MeshyApplicationImpl : Application(), MeshyApplication {

    private lateinit var applicationComponent: AndroidComponent

    @Volatile
    private var prefs: SharedPreferences? = null

    override fun attachBaseContext(base: Context) {
        if (prefs == null) {
            @Suppress("DEPRECATION") // androidx.preference.PreferenceManager — отдельная миграция
            prefs = PreferenceManager.getDefaultSharedPreferences(base)
        }
        val initialPrefs = checkNotNull(prefs) { "prefs must be loaded before attachBaseContext" }
        Localizer.initialize(initialPrefs)
        super.attachBaseContext(Localizer.getInstance().setLocale(base))
        Localizer.getInstance().setLocale(this)
        applyTheme(base, initialPrefs)
    }

    override fun onCreate() {
        super.onCreate()

        applicationComponent = createApplicationComponent()
        val exceptionHandler = applicationComponent.exceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)

        val rootLogger = Logger.getLogger("")
        // Снимаем дефолтные Android-хендлеры — пишем только в свой
        // CachingLogHandler, чтобы логи не утекали в logcat.
        rootLogger.handlers.forEach { rootLogger.removeHandler(it) }
        val logHandler: CachingLogHandler = applicationComponent.logHandler()
        rootLogger.addHandler(logHandler)
        rootLogger.level = INFO

        LOG.info("Created")

        EmojiManager.install(GoogleEmojiProvider())
    }

    protected open fun createApplicationComponent(): AndroidComponent {
        val androidComponent: AndroidComponent = DaggerAndroidComponent.builder()
            .appModule(AppModule(this))
            .build()

        // Eager-синглтоны нужно поднять сразу после построения графа.
        BrambleCoreEagerSingletons.Helper.injectEagerSingletons(androidComponent)
        MessagingEngineAndroidEagerSingletons.Helper.injectEagerSingletons(androidComponent)
        MeshyCoreEagerSingletons.Helper.injectEagerSingletons(androidComponent)
        AndroidEagerSingletons.Helper.injectEagerSingletons(androidComponent)
        return androidComponent
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Localizer.getInstance().setLocale(this)
    }

    private fun applyTheme(ctx: Context, prefs: SharedPreferences) {
        var theme = prefs.getString(PREF_THEME, null)
        if (theme == null) {
            theme = getString(R.string.pref_theme_system_value)
            prefs.edit().putString(PREF_THEME, theme).apply()
        }
        UiUtils.setTheme(ctx, theme)
    }

    override fun getBrambleAppComponent(): MessagingEngineAppComponent = applicationComponent

    override fun getApplicationComponent(): AndroidComponent = applicationComponent

    override fun getDefaultSharedPreferences(): SharedPreferences =
        checkNotNull(prefs) { "SharedPreferences not loaded yet — attachBaseContext was not called" }

    override fun isRunningInBackground(): Boolean {
        val info = RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(info)
        return info.importance != IMPORTANCE_FOREGROUND
    }

    override fun isInstrumentationTest(): Boolean = false

    companion object {
        private val LOG: Logger = Logger.getLogger(MeshyApplicationImpl::class.java.name)
    }
}