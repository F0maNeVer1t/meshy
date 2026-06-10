package ru.itis.meshy.android

import android.app.Activity
import android.content.SharedPreferences
import ru.itis.messaging_engine.MessagingEngineApplication
import ru.itis.meshy.android.navdrawer.NavDrawerActivity

/**
 * Существует, чтобы объект Application не приходилось безусловно
 * приводить к meshy-application: коду достаточно интерфейса.
 */
interface MeshyApplication : MessagingEngineApplication {

    fun getApplicationComponent(): AndroidComponent

    fun getDefaultSharedPreferences(): SharedPreferences

    fun isRunningInBackground(): Boolean

    fun isInstrumentationTest(): Boolean

    companion object {
        /**
         * Точка входа в приложение — Activity, открывающаяся после успешного
         * запуска ядра.
         */
        @JvmField
        val ENTRY_ACTIVITY: Class<out Activity> = NavDrawerActivity::class.java
    }
}