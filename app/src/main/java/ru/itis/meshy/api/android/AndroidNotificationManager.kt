package ru.itis.meshy.api.android

import android.app.Notification
import ru.itis.messaging_engine.api.contact.ContactId

/**
 * Manages notifications for private messages and introductions.
 *
 * Полностью убрана работа с blog/forum/private group уведомлениями —
 * соответствующие фичи удалены из приложения.
 *
 * Все оставшиеся константы сохранили свои строковые/числовые значения,
 * чтобы не сломать SharedPreferences и channelId уже установленных
 * пользователей. Ключи и каналы с префиксами blog/forum/group просто
 * больше не используются — старые каналы Android сам удалит при
 * первом запуске, если они не пересоздаются.
 */
interface AndroidNotificationManager {

    fun getForegroundNotification(): Notification

    fun updateForegroundNotification(locked: Boolean)

    fun clearContactNotification(c: ContactId)

    fun clearAllContactNotifications()

    fun clearAllContactAddedNotifications()

    fun showSignInNotification()

    fun clearSignInNotification()

    fun blockSignInNotification()

    fun blockContactNotification(c: ContactId)

    fun unblockContactNotification(c: ContactId)

    fun showHotspotNotification()

    fun clearHotspotNotification()

    fun showMailboxProblemNotification()

    fun clearMailboxProblemNotification()

    companion object {
        // Keys for notification preferences
        const val PREF_NOTIFY_PRIVATE = "notifyPrivateMessages"

        const val PREF_NOTIFY_SOUND = "notifySound"
        const val PREF_NOTIFY_RINGTONE_NAME = "notifyRingtoneName"
        const val PREF_NOTIFY_RINGTONE_URI = "notifyRingtoneUri"
        const val PREF_NOTIFY_VIBRATION = "notifyVibration"

        // Notification IDs
        const val ONGOING_NOTIFICATION_ID = 1
        const val REMINDER_NOTIFICATION_ID = 3
        const val PRIVATE_MESSAGE_NOTIFICATION_ID = 4
        const val CONTACT_ADDED_NOTIFICATION_ID = 8
        const val HOTSPOT_NOTIFICATION_ID = 9
        const val MAILBOX_PROBLEM_NOTIFICATION_ID = 10

        // Channel IDs
        const val CONTACT_CHANNEL_ID = "contacts"

        // Channels are sorted by channel ID in the Settings app, so use IDs
        // that will sort below the main channels such as contacts
        const val ONGOING_CHANNEL_OLD_ID = "zForegroundService"
        const val ONGOING_CHANNEL_ID = "zForegroundService2"
        const val REMINDER_CHANNEL_ID = "zSignInReminder"
        const val HOTSPOT_CHANNEL_ID = "zHotspot"
        const val MAILBOX_PROBLEM_CHANNEL_ID = "zMailboxProblem"

        // Actions for pending intents
        const val ACTION_DISMISS_REMINDER = "dismissReminder"
        const val ACTION_STOP_HOTSPOT = "stopHotspot"
    }
}