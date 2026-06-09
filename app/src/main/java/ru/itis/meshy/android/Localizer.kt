package ru.itis.meshy.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import org.briarproject.nullsafety.NotNullByDefault
import ru.itis.meshy.android.settings.DisplayFragment.PREF_LANGUAGE
import java.util.Locale

/**
 * Управляет локалью приложения (системной либо выбранной пользователем
 * в настройках). Singleton с ленивой инициализацией — нужна
 * [SharedPreferences], недоступная в `init {}` блоке `object`'а.
 *
 * Замечания по конкурентности:
 * - INSTANCE помечен `@Volatile`: чтение через [getInstance] неблокирующее.
 * - Запись (initialize/reinitialize) — под общим [LOCK].
 * - Поведение точь-в-точь соответствует Java-оригиналу с `synchronized`
 *   static-методами.
 */
@NotNullByDefault
class Localizer private constructor(
    private val systemLocale: Locale,
    userLocale: Locale?,
) {

    private val locale: Locale = userLocale ?: systemLocale

    private constructor(prefs: SharedPreferences) : this(
        Locale.getDefault(),
        getLocaleFromTag(prefs.getString(PREF_LANGUAGE, "default") ?: "default"),
    )

    /**
     * Apply localization to the specified context.
     *
     * It updates the configuration of the context's resources object but can
     * also return a new context derived from the context parameter. Hence
     * make sure to work with the return value of this method instead of
     * the context you passed as a parameter.
     *
     * This method also has side-effects as it calls [Locale.setDefault].
     *
     * When using this in `attachBaseContext()` of Application, Service or
     * Activity subclasses, it is important to not only apply this method to the
     * base Context parameter received in that method, but also apply it on the
     * class itself which also extends Context.
     */
    fun setLocale(context: Context): Context {
        val res = context.resources
        val conf = res.configuration
        val currentLocale: Locale = if (SDK_INT >= 24) {
            conf.locales[0]
        } else {
            @Suppress("DEPRECATION")
            conf.locale
        }
        if (locale == currentLocale) return context
        Locale.setDefault(locale)
        conf.setLocale(locale)
        val newContext = context.createConfigurationContext(conf)
        @Suppress("DEPRECATION")
        res.updateConfiguration(conf, res.displayMetrics)
        return newContext
    }

    companion object {
        // Отдельный lock-объект вместо лока на классе — явнее по намерению
        // и не подвержен внешнему `synchronized(Localizer::class.java)`.
        private val LOCK = Any()

        @Volatile
        private var INSTANCE: Localizer? = null

        /** Instantiate the [Localizer]. */
        @JvmStatic
        fun initialize(prefs: SharedPreferences) = synchronized(LOCK) {
            if (INSTANCE == null) {
                INSTANCE = Localizer(prefs)
            }
        }

        /** Reinstantiate the [Localizer] with the system locale. */
        @JvmStatic
        fun reinitialize() = synchronized(LOCK) {
            INSTANCE?.let { current ->
                INSTANCE = Localizer(current.systemLocale, null)
            }
        }

        /** Get the current instance. */
        @JvmStatic
        fun getInstance(): Localizer = synchronized(LOCK) {
            INSTANCE ?: error("Localizer not initialized")
        }

        /** Get [Locale] from BCP-47 tag. Returns null for tag `"default"`. */
        @JvmStatic
        fun getLocaleFromTag(tag: String): Locale? =
            if (tag == "default") null else Locale.forLanguageTag(tag)
    }
}