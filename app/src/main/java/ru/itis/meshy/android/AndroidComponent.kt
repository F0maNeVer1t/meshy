package ru.itis.meshy.android

import androidx.lifecycle.ViewModelProvider
import dagger.Component
import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager
import ru.itis.messaging_engine.BrambleAndroidEagerSingletons
import ru.itis.messaging_engine.BrambleAndroidModule
import ru.itis.messaging_engine.BrambleAppComponent
import ru.itis.messaging_engine.BrambleCoreEagerSingletons
import ru.itis.messaging_engine.BrambleCoreModule
import ru.itis.messaging_engine.api.FeatureFlags
import ru.itis.messaging_engine.api.account.AccountManager
import ru.itis.messaging_engine.api.connection.ConnectionRegistry
import ru.itis.messaging_engine.api.contact.ContactExchangeManager
import ru.itis.messaging_engine.api.contact.ContactManager
import ru.itis.messaging_engine.api.crypto.CryptoExecutor
import ru.itis.messaging_engine.api.crypto.PasswordStrengthEstimator
import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.messaging_engine.api.db.TransactionManager
import ru.itis.messaging_engine.api.event.EventBus
import ru.itis.messaging_engine.api.identity.IdentityManager
import ru.itis.messaging_engine.api.keyagreement.KeyAgreementTask
import ru.itis.messaging_engine.api.keyagreement.PayloadEncoder
import ru.itis.messaging_engine.api.keyagreement.PayloadParser
import ru.itis.messaging_engine.api.lifecycle.IoExecutor
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager
import ru.itis.messaging_engine.api.plugin.PluginManager
import ru.itis.messaging_engine.api.settings.SettingsManager
import ru.itis.messaging_engine.api.system.AndroidExecutor
import ru.itis.messaging_engine.api.system.Clock
import ru.itis.messaging_engine.mailbox.ModularMailboxModule
import ru.itis.messaging_engine.plugin.file.RemovableDriveModule
import ru.itis.messaging_engine.system.ClockModule
import ru.itis.meshy.BriarCoreEagerSingletons
import ru.itis.meshy.BriarCoreModule
import ru.itis.meshy.api.attachment.AttachmentReader
import ru.itis.meshy.api.autodelete.AutoDeleteManager
import ru.itis.meshy.api.client.MessageTracker
import ru.itis.meshy.api.conversation.ConversationManager
import ru.itis.meshy.api.identity.AuthorManager
import ru.itis.meshy.api.introduction.IntroductionManager
import ru.itis.meshy.api.messaging.MessagingManager
import ru.itis.meshy.api.messaging.PrivateMessageFactory
import org.briarproject.onionwrapper.CircumventionProvider
import org.briarproject.onionwrapper.LocationUtils
import ru.itis.messaging_engine.account.MeshyAccountModule
import ru.itis.meshy.android.attachment.AttachmentModule
import ru.itis.meshy.android.attachment.media.MediaModule
import ru.itis.meshy.android.contact.connect.BluetoothIntroFragment
import ru.itis.meshy.android.conversation.glide.MeshyModelLoader
import ru.itis.meshy.android.hotspot.AbstractTabsFragment
import ru.itis.meshy.android.hotspot.FallbackFragment
import ru.itis.meshy.android.hotspot.HotspotIntroFragment
import ru.itis.meshy.android.hotspot.ManualHotspotFragment
import ru.itis.meshy.android.hotspot.QrHotspotFragment
import ru.itis.meshy.android.logging.CachingLogHandler
import ru.itis.meshy.android.login.SignInReminderReceiver
import ru.itis.meshy.android.mailbox.ErrorFragment
import ru.itis.meshy.android.mailbox.ErrorWizardFragment
import ru.itis.meshy.android.mailbox.MailboxScanFragment
import ru.itis.meshy.android.mailbox.MailboxStatusFragment
import ru.itis.meshy.android.mailbox.OfflineFragment
import ru.itis.meshy.android.mailbox.SetupDownloadFragment
import ru.itis.meshy.android.mailbox.SetupIntroFragment
import ru.itis.meshy.android.removabledrive.ChooserFragment
import ru.itis.meshy.android.removabledrive.ReceiveFragment
import ru.itis.meshy.android.removabledrive.SendFragment
import ru.itis.meshy.android.settings.ConnectionsFragment
import ru.itis.meshy.android.settings.NotificationsFragment
import ru.itis.meshy.android.settings.SecurityFragment
import ru.itis.meshy.android.settings.SettingsFragment
import ru.itis.meshy.android.view.EmojiTextInputView
import ru.itis.meshy.api.android.AndroidNotificationManager
import ru.itis.meshy.api.android.DozeWatchdog
import ru.itis.meshy.api.android.LockManager
import ru.itis.meshy.api.android.ScreenFilterMonitor
import java.util.concurrent.Executor
import javax.inject.Singleton

/**
 * Корневой Dagger-component приложения.
 *
 * Удалены геттеры на удалённые API: BlogManager/BlogPostFactory/
 * BlogSharingManager, ForumManager/ForumSharingManager, PrivateGroupManager/
 * PrivateGroupFactory/GroupMessageFactory/GroupInvitationFactory/
 * GroupInvitationManager, FeedManager, TestDataCreator. И inject-точки
 * на удалённые UI-классы (GroupActivity, ForumActivity, Blog*Activity и т.п.).
 */
@Singleton
@Component(
    modules = [
        BrambleCoreModule::class,
        BriarCoreModule::class,
        BrambleAndroidModule::class,
        MeshyAccountModule::class,
        AppModule::class,
        AttachmentModule::class,
        ClockModule::class,
        MediaModule::class,
        ModularMailboxModule::class,
        RemovableDriveModule::class,
    ],
)
interface AndroidComponent :
    BrambleCoreEagerSingletons,
    BrambleAndroidEagerSingletons,
    BriarCoreEagerSingletons,
    AndroidEagerSingletons,
    BrambleAppComponent {

    // ── Exposed objects ────────────────────────────────────────────────

    @CryptoExecutor
    fun cryptoExecutor(): Executor

    fun passwordStrengthIndicator(): PasswordStrengthEstimator

    @DatabaseExecutor
    fun databaseExecutor(): Executor

    fun transactionManager(): TransactionManager

    fun messageTracker(): MessageTracker

    fun lifecycleManager(): LifecycleManager

    fun identityManager(): IdentityManager

    fun attachmentReader(): AttachmentReader

    fun authorManager(): AuthorManager

    fun pluginManager(): PluginManager

    fun eventBus(): EventBus

    fun androidNotificationManager(): AndroidNotificationManager

    fun screenFilterMonitor(): ScreenFilterMonitor

    fun connectionRegistry(): ConnectionRegistry

    fun contactManager(): ContactManager

    fun conversationManager(): ConversationManager

    fun messagingManager(): MessagingManager

    fun privateMessageFactory(): PrivateMessageFactory

    fun settingsManager(): SettingsManager

    fun contactExchangeManager(): ContactExchangeManager

    fun keyAgreementTask(): KeyAgreementTask

    fun payloadEncoder(): PayloadEncoder

    fun payloadParser(): PayloadParser

    fun introductionManager(): IntroductionManager

    fun androidExecutor(): AndroidExecutor

    fun clock(): Clock

    fun dozeWatchdog(): DozeWatchdog

    @IoExecutor
    fun ioExecutor(): Executor

    fun accountManager(): AccountManager

    fun lockManager(): LockManager

    fun locationUtils(): LocationUtils

    fun circumventionProvider(): CircumventionProvider

    fun viewModelFactory(): ViewModelProvider.Factory

    fun featureFlags(): FeatureFlags

    fun wakeLockManager(): AndroidWakeLockManager

    fun logHandler(): CachingLogHandler

    fun exceptionHandler(): Thread.UncaughtExceptionHandler

    fun autoDeleteManager(): AutoDeleteManager

    // ── Injection sites ────────────────────────────────────────────────

    fun inject(signInReminderReceiver: SignInReminderReceiver)

    fun inject(meshyService: MeshyService)

    fun inject(notificationCleanupService: NotificationCleanupService)

    fun inject(textInputView: EmojiTextInputView)

    fun inject(meshyModelLoader: MeshyModelLoader)

    fun inject(settingsFragment: SettingsFragment)

    fun inject(connectionsFragment: ConnectionsFragment)

    fun inject(securityFragment: SecurityFragment)

    fun inject(notificationsFragment: NotificationsFragment)

    fun inject(hotspotIntroFragment: HotspotIntroFragment)

    fun inject(abstractTabsFragment: AbstractTabsFragment)

    fun inject(qrHotspotFragment: QrHotspotFragment)

    fun inject(manualHotspotFragment: ManualHotspotFragment)

    fun inject(fallbackFragment: FallbackFragment)

    fun inject(chooserFragment: ChooserFragment)

    fun inject(sendFragment: SendFragment)

    fun inject(receiveFragment: ReceiveFragment)

    fun inject(bluetoothIntroFragment: BluetoothIntroFragment)

    fun inject(setupIntroFragment: SetupIntroFragment)

    fun inject(setupDownloadFragment: SetupDownloadFragment)

    fun inject(mailboxScanFragment: MailboxScanFragment)

    fun inject(offlineFragment: OfflineFragment)

    fun inject(errorFragment: ErrorFragment)

    fun inject(mailboxStatusFragment: MailboxStatusFragment)

    fun inject(errorWizardFragment: ErrorWizardFragment)
}