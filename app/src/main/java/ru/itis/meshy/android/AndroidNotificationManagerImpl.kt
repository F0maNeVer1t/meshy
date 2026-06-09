package ru.itis.meshy.android

import android.annotation.TargetApi
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_MESSAGE
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.PRIORITY_LOW
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getColor
import ru.itis.messaging_engine.api.Multiset
import ru.itis.messaging_engine.api.contact.ContactId
import ru.itis.messaging_engine.api.contact.event.ContactAddedEvent
import ru.itis.messaging_engine.api.db.DbException
import ru.itis.messaging_engine.api.event.Event
import ru.itis.messaging_engine.api.event.EventListener
import ru.itis.messaging_engine.api.lifecycle.Service
import ru.itis.messaging_engine.api.lifecycle.ServiceException
import ru.itis.messaging_engine.api.mailbox.event.MailboxProblemEvent
import ru.itis.messaging_engine.api.mailbox.event.OwnMailboxConnectionStatusEvent
import ru.itis.messaging_engine.api.settings.Settings
import ru.itis.messaging_engine.api.settings.SettingsManager
import ru.itis.messaging_engine.api.settings.event.SettingsUpdatedEvent
import ru.itis.messaging_engine.api.system.AndroidExecutor
import ru.itis.messaging_engine.api.system.Clock
import ru.itis.messaging_engine.util.AndroidUtils.getImmutableFlags
import ru.itis.messaging_engine.util.StringUtils
import ru.itis.meshy.api.conversation.ConversationResponse
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.R
import ru.itis.meshy.android.conversation.ConversationActivity
import ru.itis.meshy.android.conversation.ConversationActivity.CONTACT_ID
import ru.itis.meshy.android.hotspot.HotspotActivity
import ru.itis.meshy.android.login.SignInReminderReceiver
import ru.itis.meshy.android.mailbox.MailboxActivity
import ru.itis.meshy.android.navdrawer.NavDrawerActivity
import ru.itis.meshy.android.navdrawer.NavDrawerActivity.CONTACT_ADDED_URI
import ru.itis.meshy.android.navdrawer.NavDrawerActivity.CONTACT_URI
import ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE
import ru.itis.meshy.android.splash.SplashScreenActivity
import ru.itis.meshy.android.util.MeshyNotificationBuilder
import ru.itis.meshy.api.android.AndroidNotificationManager
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ACTION_DISMISS_REMINDER
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ACTION_STOP_HOTSPOT
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.CONTACT_ADDED_NOTIFICATION_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.CONTACT_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.HOTSPOT_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.HOTSPOT_NOTIFICATION_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.MAILBOX_PROBLEM_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.MAILBOX_PROBLEM_NOTIFICATION_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ONGOING_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.ONGOING_NOTIFICATION_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.PREF_NOTIFY_PRIVATE
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.PREF_NOTIFY_RINGTONE_URI
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.PREF_NOTIFY_SOUND
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.PREF_NOTIFY_VIBRATION
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.PRIVATE_MESSAGE_NOTIFICATION_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.REMINDER_CHANNEL_ID
import ru.itis.meshy.api.android.AndroidNotificationManager.Companion.REMINDER_NOTIFICATION_ID
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject

/**
 * Менеджер уведомлений приложения.
 *
 * После удаления blog/forum/private-group остались:
 *  - foreground service notification
 *  - notifications о новых private message'ах
 *  - notifications о добавленных контактах
 *  - sign-in reminder
 *  - hotspot
 *  - mailbox-problem
 *
 * Состояние (`contactCounts`, `contactAddedTotal`, `blockedContact`,
 * `blockSignInReminder`, `lastSound`, `nextRequestId`) доступается
 * только на UI-потоке — все мутации идут через `androidExecutor.runOnUiThread`.
 * `settings` — `@Volatile`, обновляется из любого потока ядра.
 */
@ThreadSafe
@MethodsNotNullByDefault
@ParametersNotNullByDefault
internal class AndroidNotificationManagerImpl @Inject constructor(
    private val settingsManager: SettingsManager,
    private val androidExecutor: AndroidExecutor,
    app: Application,
    private val clock: Clock,
) : AndroidNotificationManager, Service, EventListener {

    private val appContext: Context = app.applicationContext
    private val notificationManager: NotificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val used = AtomicBoolean(false)

    // UI thread only.
    private val contactCounts = Multiset<ContactId>()
    private var contactAddedTotal: Int = 0
    private var nextRequestId: Int = 0
    private var blockedContact: ContactId? = null
    private var blockSignInReminder: Boolean = false
    private var lastSound: Long = 0L

    @Volatile
    private var settings: Settings = Settings()

    @Throws(ServiceException::class)
    override fun startService() {
        check(!used.getAndSet(true)) { "Notification manager already started" }
        try {
            settings = settingsManager.getSettings(SETTINGS_NAMESPACE)
        } catch (e: DbException) {
            throw ServiceException(e)
        }
        if (SDK_INT >= 26) {
            val task = Callable<Void?> {
                createNotificationChannel(CONTACT_CHANNEL_ID, R.string.contact_list_button)
                null
            }
            try {
                androidExecutor.runOnUiThread(task).get()
            } catch (e: InterruptedException) {
                throw ServiceException(e)
            } catch (e: ExecutionException) {
                throw ServiceException(e)
            }
        }
    }

    @TargetApi(26)
    private fun createNotificationChannel(channelId: String, @StringRes name: Int) {
        val nc = NotificationChannel(
            channelId,
            appContext.getString(name),
            IMPORTANCE_DEFAULT,
        )
        nc.lockscreenVisibility = VISIBILITY_SECRET
        nc.enableVibration(true)
        nc.enableLights(true)
        nc.lightColor = getColor(appContext, R.color.meshy_gold_400)
        notificationManager.createNotificationChannel(nc)
    }

    @Throws(ServiceException::class)
    override fun stopService() {
        val f = androidExecutor.runOnUiThread(Callable<Void?> {
            clearContactNotification()
            clearContactAddedNotification()
            clearMailboxProblemNotification()
            null
        })
        try {
            f.get()
        } catch (e: InterruptedException) {
            throw ServiceException(e)
        } catch (e: ExecutionException) {
            throw ServiceException(e)
        }
    }

    @UiThread
    private fun clearContactNotification() {
        contactCounts.clear()
        notificationManager.cancel(PRIVATE_MESSAGE_NOTIFICATION_ID)
    }

    @UiThread
    private fun clearContactAddedNotification() {
        contactAddedTotal = 0
        notificationManager.cancel(CONTACT_ADDED_NOTIFICATION_ID)
    }

    override fun eventOccurred(e: Event) {
        when (e) {
            is SettingsUpdatedEvent -> {
                if (e.namespace == SETTINGS_NAMESPACE) settings = e.settings
            }
            is ConversationMessageReceivedEvent<*> -> {
                val header = e.messageHeader
                if (header is ConversationResponse) {
                    if (header.isAutoDecline) return
                }
                showContactNotification(e.contactId)
            }
            is ContactAddedEvent -> {
                if (!e.isVerified) showContactAddedNotification()
            }
            is MailboxProblemEvent -> showMailboxProblemNotification()
            is OwnMailboxConnectionStatusEvent -> {
                val s = e.status
                if (s.attemptsSinceSuccess == 0) clearMailboxProblemNotification()
            }
        }
    }

    // ── Foreground notification ────────────────────────────────────────

    @UiThread
    override fun getForegroundNotification(): Notification = getForegroundNotification(false)

    @UiThread
    private fun getForegroundNotification(locked: Boolean): Notification {
        val title = if (locked) R.string.lock_is_locked else R.string.ongoing_notification_title
        val text = if (locked) R.string.lock_tap_to_unlock else R.string.ongoing_notification_text
        val icon = if (locked) R.drawable.notification_lock else R.drawable.notification_ongoing
        val b = NotificationCompat.Builder(appContext, ONGOING_CHANNEL_ID)
        b.setSmallIcon(icon)
        b.color = getColor(appContext, R.color.meshy_primary)
        b.setContentTitle(appContext.getText(title))
        b.setContentText(appContext.getText(text))
        b.setWhen(0)
        b.setOngoing(true)
        val i = Intent(appContext, SplashScreenActivity::class.java)
        b.setContentIntent(getActivity(appContext, 0, i, getImmutableFlags(0)))
        b.setCategory(CATEGORY_SERVICE)
        b.setVisibility(VISIBILITY_SECRET)
        b.priority = PRIORITY_MIN
        return b.build()
    }

    @UiThread
    override fun updateForegroundNotification(locked: Boolean) {
        val n = getForegroundNotification(locked)
        notificationManager.notify(ONGOING_NOTIFICATION_ID, n)
    }

    // ── Contact / private message notifications ────────────────────────

    @UiThread
    private fun showContactNotification(c: ContactId) {
        if (c == blockedContact) return
        contactCounts.add(c)
        updateContactNotification(true)
    }

    override fun clearContactNotification(c: ContactId) {
        androidExecutor.runOnUiThread(Runnable {
            if (contactCounts.removeAll(c) > 0) updateContactNotification(false)
        })
    }

    @UiThread
    private fun updateContactNotification(mayAlertAgain: Boolean) {
        val contactTotal = contactCounts.total
        if (contactTotal == 0) {
            clearContactNotification()
        } else if (settings.getBoolean(PREF_NOTIFY_PRIVATE, true)) {
            val b = MeshyNotificationBuilder(appContext, CONTACT_CHANNEL_ID)
            b.setSmallIcon(R.drawable.notification_private_message)
            b.setColorRes(R.color.meshy_primary)
            b.setContentTitle(appContext.getText(R.string.app_name))
            b.setContentText(
                appContext.resources.getQuantityString(
                    R.plurals.private_message_notification_text,
                    contactTotal,
                    contactTotal,
                ),
            )
            b.setNumber(contactTotal)
            b.setNotificationCategory(CATEGORY_MESSAGE)
            if (mayAlertAgain) setAlertProperties(b)
            setDeleteIntent(b, CONTACT_URI)
            val contacts: Set<ContactId> = contactCounts.keySet()
            if (contacts.size == 1) {
                // Touching the notification shows the relevant conversation
                val i = Intent(appContext, ConversationActivity::class.java)
                val c = contacts.iterator().next()
                i.putExtra(CONTACT_ID, c.int)
                i.data = Uri.parse("$CONTACT_URI/${c.int}")
                i.flags = FLAG_ACTIVITY_CLEAR_TOP
                val t = TaskStackBuilder.create(appContext)
                t.addParentStack(ConversationActivity::class.java)
                t.addNextIntent(i)
                b.setContentIntent(t.getPendingIntent(nextRequestId++, getImmutableFlags(0)))
            } else {
                // Touching the notification shows the contact list
                val i = Intent(appContext, NavDrawerActivity::class.java)
                i.flags = FLAG_ACTIVITY_CLEAR_TOP
                i.data = CONTACT_URI
                val t = TaskStackBuilder.create(appContext)
                t.addParentStack(NavDrawerActivity::class.java)
                t.addNextIntent(i)
                b.setContentIntent(t.getPendingIntent(nextRequestId++, getImmutableFlags(0)))
            }
            notificationManager.notify(PRIVATE_MESSAGE_NOTIFICATION_ID, b.build())
        }
    }

    @UiThread
    private fun setAlertProperties(b: MeshyNotificationBuilder) {
        val currentTime = clock.currentTimeMillis()
        if (currentTime - lastSound > SOUND_DELAY) {
            val sound = settings.getBoolean(PREF_NOTIFY_SOUND, true)
            val ringtoneUri = settings[PREF_NOTIFY_RINGTONE_URI]
            if (sound && !StringUtils.isNullOrEmpty(ringtoneUri)) {
                val uri = Uri.parse(ringtoneUri)
                if ("file" != uri.scheme) b.setSound(uri)
            }
            b.setDefaults(getDefaults())
            lastSound = currentTime
        }
    }

    @UiThread
    private fun getDefaults(): Int {
        var defaults = Notification.DEFAULT_LIGHTS
        val sound = settings.getBoolean(PREF_NOTIFY_SOUND, true)
        val ringtoneUri = settings[PREF_NOTIFY_RINGTONE_URI]
        if (sound && (StringUtils.isNullOrEmpty(ringtoneUri) ||
                    "file" == Uri.parse(ringtoneUri).scheme)
        ) {
            defaults = defaults or Notification.DEFAULT_SOUND
        }
        if (settings.getBoolean(PREF_NOTIFY_VIBRATION, true)) {
            defaults = defaults or Notification.DEFAULT_VIBRATE
        }
        return defaults
    }

    private fun setDeleteIntent(b: MeshyNotificationBuilder, uri: Uri) {
        val i = Intent(appContext, NotificationCleanupService::class.java)
        i.data = uri
        b.setDeleteIntent(
            PendingIntent.getService(appContext, nextRequestId++, i, getImmutableFlags(0)),
        )
    }

    override fun clearAllContactNotifications() {
        androidExecutor.runOnUiThread(Runnable { clearContactNotification() })
    }

    // ── Contact-added notifications ────────────────────────────────────

    @UiThread
    private fun showContactAddedNotification() {
        contactAddedTotal++
        updateContactAddedNotification()
    }

    @UiThread
    private fun updateContactAddedNotification() {
        val b = MeshyNotificationBuilder(appContext, CONTACT_CHANNEL_ID)
        b.setSmallIcon(R.drawable.notification_contact_added)
        b.setColorRes(R.color.meshy_primary)
        b.setContentTitle(appContext.getText(R.string.app_name))
        b.setContentText(
            appContext.resources.getQuantityString(
                R.plurals.contact_added_notification_text,
                contactAddedTotal,
                contactAddedTotal,
            ),
        )
        b.setNotificationCategory(CATEGORY_MESSAGE)
        setAlertProperties(b)
        setDeleteIntent(b, CONTACT_ADDED_URI)
        val i = Intent(appContext, NavDrawerActivity::class.java)
        i.flags = FLAG_ACTIVITY_CLEAR_TOP
        i.data = CONTACT_URI
        val t = TaskStackBuilder.create(appContext)
        t.addParentStack(NavDrawerActivity::class.java)
        t.addNextIntent(i)
        b.setContentIntent(t.getPendingIntent(nextRequestId++, getImmutableFlags(0)))

        notificationManager.notify(CONTACT_ADDED_NOTIFICATION_ID, b.build())
    }

    override fun clearAllContactAddedNotifications() {
        androidExecutor.runOnUiThread(Runnable { clearContactAddedNotification() })
    }

    // ── Sign-in reminder ───────────────────────────────────────────────

    override fun showSignInNotification() {
        if (blockSignInReminder) return
        if (SDK_INT >= 26) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                appContext.getString(R.string.reminder_notification_channel_title),
                IMPORTANCE_LOW,
            )
            channel.lockscreenVisibility = VISIBILITY_SECRET
            notificationManager.createNotificationChannel(channel)
        }

        val b = NotificationCompat.Builder(appContext, REMINDER_CHANNEL_ID)
        b.setSmallIcon(R.drawable.notification_signout)
        b.color = getColor(appContext, R.color.meshy_primary)
        b.setContentTitle(appContext.getText(R.string.reminder_notification_title))
        b.setContentText(appContext.getText(R.string.reminder_notification_text))
        b.setAutoCancel(true)
        b.setWhen(0)
        b.priority = PRIORITY_LOW

        val actionTitle = appContext.getString(R.string.reminder_notification_dismiss)
        val i1 = Intent(appContext, SignInReminderReceiver::class.java)
        i1.action = ACTION_DISMISS_REMINDER
        val actionIntent: PendingIntent = getBroadcast(appContext, 0, i1, getImmutableFlags(0))
        b.addAction(0, actionTitle, actionIntent)

        val i = Intent(appContext, SplashScreenActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP
        b.setContentIntent(getActivity(appContext, 0, i, getImmutableFlags(0)))

        notificationManager.notify(REMINDER_NOTIFICATION_ID, b.build())
    }

    override fun clearSignInNotification() {
        notificationManager.cancel(REMINDER_NOTIFICATION_ID)
    }

    override fun blockSignInNotification() {
        blockSignInReminder = true
    }

    override fun blockContactNotification(c: ContactId) {
        androidExecutor.runOnUiThread(Runnable { blockedContact = c })
    }

    override fun unblockContactNotification(c: ContactId) {
        androidExecutor.runOnUiThread(Runnable {
            if (c == blockedContact) blockedContact = null
        })
    }

    // ── Hotspot notification ───────────────────────────────────────────

    override fun showHotspotNotification() {
        if (SDK_INT >= 26) {
            val channelTitle = appContext.getString(R.string.hotspot_notification_channel_title)
            val channel = NotificationChannel(
                HOTSPOT_CHANNEL_ID,
                channelTitle,
                IMPORTANCE_LOW,
            )
            channel.lockscreenVisibility = VISIBILITY_SECRET
            notificationManager.createNotificationChannel(channel)
        }
        val b = MeshyNotificationBuilder(appContext, HOTSPOT_CHANNEL_ID)
        b.setSmallIcon(R.drawable.notification_hotspot)
        b.setColorRes(R.color.meshy_brand_green)
        b.setContentTitle(appContext.getText(R.string.hotspot_notification_title))
        b.setNotificationCategory(CATEGORY_SERVICE)
        b.setOngoing(true)
        b.setShowWhen(true)

        val actionTitle = appContext.getString(R.string.hotspot_button_stop_sharing)
        val i = Intent(appContext, HotspotActivity::class.java)
        i.addFlags(FLAG_ACTIVITY_SINGLE_TOP)
        i.action = ACTION_STOP_HOTSPOT
        val actionIntent: PendingIntent = getActivity(appContext, 0, i, getImmutableFlags(0))
        b.addAction(R.drawable.ic_portable_wifi_off, actionTitle, actionIntent)
        notificationManager.notify(HOTSPOT_NOTIFICATION_ID, b.build())
    }

    override fun clearHotspotNotification() {
        notificationManager.cancel(HOTSPOT_NOTIFICATION_ID)
    }

    // ── Mailbox-problem notification ───────────────────────────────────

    override fun showMailboxProblemNotification() {
        if (SDK_INT >= 26) {
            val channel = NotificationChannel(
                MAILBOX_PROBLEM_CHANNEL_ID,
                appContext.getString(R.string.mailbox_error_notification_channel_title),
                IMPORTANCE_DEFAULT,
            )
            channel.lockscreenVisibility = VISIBILITY_SECRET
            notificationManager.createNotificationChannel(channel)
        }

        val b = NotificationCompat.Builder(appContext, MAILBOX_PROBLEM_CHANNEL_ID)
        b.setSmallIcon(R.drawable.notification_mailbox)
        b.color = getColor(appContext, R.color.meshy_red_500)
        b.setContentTitle(appContext.getText(R.string.mailbox_error_notification_title))
        b.setContentText(appContext.getText(R.string.mailbox_error_notification_text))
        b.setAutoCancel(true)
        @Suppress("DEPRECATION")
        b.setNotificationSilent()
        b.setWhen(0)
        b.priority = PRIORITY_HIGH

        val i = Intent(appContext, MailboxActivity::class.java)
        i.data = Uri.EMPTY
        i.flags = FLAG_ACTIVITY_CLEAR_TOP
        val t = TaskStackBuilder.create(appContext)
        t.addParentStack(MailboxActivity::class.java)
        t.addNextIntent(i)
        b.setContentIntent(t.getPendingIntent(nextRequestId++, getImmutableFlags(0)))

        notificationManager.notify(MAILBOX_PROBLEM_NOTIFICATION_ID, b.build())
    }

    override fun clearMailboxProblemNotification() {
        notificationManager.cancel(MAILBOX_PROBLEM_NOTIFICATION_ID)
    }

    companion object {
        private val SOUND_DELAY: Long = TimeUnit.SECONDS.toMillis(2)
    }
}