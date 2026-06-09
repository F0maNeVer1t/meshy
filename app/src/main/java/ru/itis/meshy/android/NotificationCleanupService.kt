package ru.itis.meshy.android

import android.app.IntentService
import android.content.Intent
import ru.itis.meshy.android.navdrawer.NavDrawerActivity.CONTACT_ADDED_URI
import ru.itis.meshy.android.navdrawer.NavDrawerActivity.CONTACT_URI
import ru.itis.meshy.api.android.AndroidNotificationManager
import javax.inject.Inject

/**
 * IntentService отмечен как устаревший с API 30; апстрим Briar его
 * использовал — сохраняем при миграции, deprecation подавлен локально.
 *
 * После вырезания blog/forum/group остались только два URI: контактные
 * уведомления о новых сообщениях и о добавленных контактах.
 */
@Suppress("DEPRECATION")
class NotificationCleanupService : IntentService(TAG) {

    @Inject
    lateinit var notificationManager: AndroidNotificationManager

    override fun onCreate() {
        super.onCreate()
        val applicationComponent =
            (application as MeshyApplication).getApplicationComponent()
        applicationComponent.inject(this)
    }

    override fun onHandleIntent(i: Intent?) {
        val uri = i?.data ?: return
        when (uri) {
            CONTACT_URI -> notificationManager.clearAllContactNotifications()
            CONTACT_ADDED_URI -> notificationManager.clearAllContactAddedNotifications()
        }
    }

    companion object {
        private val TAG: String = NotificationCleanupService::class.java.name
    }
}