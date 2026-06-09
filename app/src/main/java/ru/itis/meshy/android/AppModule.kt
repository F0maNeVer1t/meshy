package ru.itis.meshy.android

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import android.os.StrictMode
import com.vanniktech.emoji.RecentEmoji
import dagger.Module
import dagger.Provides
import ru.itis.messaging_engine.api.FeatureFlags
import ru.itis.messaging_engine.api.FormatException
import ru.itis.messaging_engine.api.crypto.CryptoComponent
import ru.itis.messaging_engine.api.crypto.KeyStrengthener
import ru.itis.messaging_engine.api.crypto.PublicKey
import ru.itis.messaging_engine.api.db.DatabaseConfig
import ru.itis.messaging_engine.api.event.EventBus
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager
import ru.itis.messaging_engine.api.mailbox.MailboxDirectory
import ru.itis.messaging_engine.api.plugin.BluetoothConstants
import ru.itis.messaging_engine.api.plugin.LanTcpConstants
import ru.itis.messaging_engine.api.plugin.PluginConfig
import ru.itis.messaging_engine.api.plugin.TorConstants.DEFAULT_CONTROL_PORT
import ru.itis.messaging_engine.api.plugin.TorConstants.DEFAULT_SOCKS_PORT
import ru.itis.messaging_engine.api.plugin.TorControlPort
import ru.itis.messaging_engine.api.plugin.TorDirectory
import ru.itis.messaging_engine.api.plugin.TorSocksPort
import ru.itis.messaging_engine.api.plugin.TransportId
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPluginFactory
import ru.itis.messaging_engine.api.plugin.simplex.SimplexPluginFactory
import ru.itis.messaging_engine.api.reporting.DevConfig
import ru.itis.messaging_engine.api.reporting.ReportingConstants.DEV_ONION_ADDRESS
import ru.itis.messaging_engine.api.reporting.ReportingConstants.DEV_PUBLIC_KEY_HEX
import ru.itis.messaging_engine.plugin.bluetooth.AndroidBluetoothPluginFactory
import ru.itis.messaging_engine.plugin.file.AndroidRemovableDrivePluginFactory
import ru.itis.messaging_engine.plugin.file.MailboxPluginFactory
import ru.itis.messaging_engine.plugin.tcp.AndroidLanTcpPluginFactory
import ru.itis.messaging_engine.plugin.tor.AndroidTorPluginFactory
import ru.itis.messaging_engine.util.AndroidUtils
import ru.itis.messaging_engine.util.StringUtils
import ru.itis.meshy.android.account.DozeHelperModule
import ru.itis.meshy.android.account.LockManagerImpl
import ru.itis.meshy.android.account.SetupModule
import ru.itis.meshy.android.contact.ContactListModule
import ru.itis.meshy.android.contact.add.nearby.AddNearbyContactModule
import ru.itis.meshy.android.contact.connect.ConnectViaBluetoothModule
import ru.itis.meshy.android.hotspot.HotspotModule
import ru.itis.meshy.android.introduction.IntroductionModule
import ru.itis.meshy.android.logging.LoggingModule
import ru.itis.meshy.android.login.LoginModule
import ru.itis.meshy.android.mailbox.MailboxModule
import ru.itis.meshy.android.navdrawer.NavDrawerModule
import ru.itis.meshy.android.removabledrive.TransferDataModule
import ru.itis.meshy.android.reporting.DevReportModule
import ru.itis.meshy.android.settings.SettingsModule
import ru.itis.meshy.android.sharing.SharingModule
import ru.itis.meshy.android.viewmodel.ViewModelModule
import ru.itis.meshy.api.android.AndroidNotificationManager
import ru.itis.meshy.api.android.DozeWatchdog
import ru.itis.meshy.api.android.LockManager
import ru.itis.meshy.api.android.NetworkUsageMetrics
import ru.itis.meshy.api.android.ScreenFilterMonitor
import java.io.File
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Удалено из `includes`: BlogModule, ForumModule, GroupListModule,
 * GroupConversationModule — соответствующие фичи вырезаны.
 *
 * Удалены провайдеры: TestAvatarCreator. Удалены debug-ветки в
 * [provideTorSocksPort] / [provideTorControlPort] — порты теперь
 * всегда дефолтные.
 *
 * В [provideFeatureFlags] для blog/forum/privategroup возвращаем `false` —
 * чтобы апстримное ядро bramble/briar не работало с этими типами,
 * даже если в БД от предыдущей версии что-то осталось.
 */
@Module(
    includes = [
        SetupModule::class,
        DozeHelperModule::class,
        AddNearbyContactModule::class,
        LoggingModule::class,
        LoginModule::class,
        NavDrawerModule::class,
        ViewModelModule::class,
        SettingsModule::class,
        DevReportModule::class,
        ContactListModule::class,
        IntroductionModule::class,
        ConnectViaBluetoothModule::class,
        // below need to be within same scope as ViewModelProvider.Factory
        SharingModule::class,
        HotspotModule::class,
        TransferDataModule::class,
        MailboxModule::class,
    ],
)
class AppModule(private val application: Application) {

    class EagerSingletons {
        @Inject lateinit var androidNotificationManager: AndroidNotificationManager
        @Inject lateinit var screenFilterMonitor: ScreenFilterMonitor
        @Inject lateinit var networkUsageMetrics: NetworkUsageMetrics
        @Inject lateinit var dozeWatchdog: DozeWatchdog
        @Inject lateinit var lockManager: LockManager
        @Inject lateinit var recentEmoji: RecentEmoji
    }

    @Provides
    @Singleton
    internal fun providesApplication(): Application = application

    @Provides
    @Singleton
    internal fun provideDatabaseConfig(app: Application): DatabaseConfig {
        // FIXME: StrictMode — getDir() задевает диск; поднимаем relax
        // на время вызова и возвращаем строгий policy обратно.
        val tp = StrictMode.allowThreadDiskReads()
        StrictMode.allowThreadDiskWrites()
        val dbDir = app.applicationContext.getDir("db", MODE_PRIVATE)
        val keyDir = app.applicationContext.getDir("key", MODE_PRIVATE)
        StrictMode.setThreadPolicy(tp)
        val keyStrengthener: KeyStrengthener? =
            if (SDK_INT >= 23) AndroidKeyStrengthener() else null
        return AndroidDatabaseConfig(dbDir, keyDir, keyStrengthener)
    }

    @Provides
    @Singleton
    @MailboxDirectory
    internal fun provideMailboxDirectory(app: Application): File =
        app.getDir("mailbox", MODE_PRIVATE)

    @Provides
    @Singleton
    @TorDirectory
    internal fun provideTorDirectory(app: Application): File =
        app.getDir("tor", MODE_PRIVATE)

    @Provides
    @Singleton
    @TorSocksPort
    internal fun provideTorSocksPort(): Int = DEFAULT_SOCKS_PORT

    @Provides
    @Singleton
    @TorControlPort
    internal fun provideTorControlPort(): Int = DEFAULT_CONTROL_PORT

    @Provides
    @Singleton
    internal fun providePluginConfig(
        bluetooth: AndroidBluetoothPluginFactory,
        tor: AndroidTorPluginFactory,
        lan: AndroidLanTcpPluginFactory,
        drive: AndroidRemovableDrivePluginFactory,
        mailbox: MailboxPluginFactory,
        featureFlags: FeatureFlags,
    ): PluginConfig = object : PluginConfig {

        override fun getDuplexFactories(): Collection<DuplexPluginFactory> =
            listOf(bluetooth, tor, lan)

        override fun getSimplexFactories(): Collection<SimplexPluginFactory> =
            listOf(mailbox, drive)

        override fun shouldPoll(): Boolean = true

        override fun getTransportPreferences(): Map<TransportId, List<TransportId>> =
            // Prefer LAN to Bluetooth
            mapOf(BluetoothConstants.ID to listOf(LanTcpConstants.ID))
    }

    @Provides
    @Singleton
    internal fun provideDevConfig(app: Application, crypto: CryptoComponent): DevConfig =
        object : DevConfig {

            override fun getDevPublicKey(): PublicKey {
                try {
                    return crypto.messageKeyParser.parsePublicKey(
                        StringUtils.fromHexString(DEV_PUBLIC_KEY_HEX),
                    )
                } catch (e: GeneralSecurityException) {
                    throw RuntimeException(e)
                } catch (e: FormatException) {
                    throw RuntimeException(e)
                }
            }

            override fun getDevOnionAddress(): String = DEV_ONION_ADDRESS

            override fun getReportDir(): File =
                AndroidUtils.getReportDir(app.applicationContext)

            override fun getLogcatFile(): File =
                AndroidUtils.getLogcatFile(app.applicationContext)
        }

    @Provides
    internal fun provideSharedPreferences(app: Application): SharedPreferences {
        // FIXME: unify this with getDefaultSharedPreferences()
        return app.getSharedPreferences("db", MODE_PRIVATE)
    }

    @Provides
    @Singleton
    internal fun provideAndroidNotificationManager(
        lifecycleManager: LifecycleManager,
        eventBus: EventBus,
        notificationManager: AndroidNotificationManagerImpl,
    ): AndroidNotificationManager {
        lifecycleManager.registerService(notificationManager)
        eventBus.addListener(notificationManager)
        return notificationManager
    }

    @Provides
    @Singleton
    internal fun provideScreenFilterMonitor(
        lifecycleManager: LifecycleManager,
        screenFilterMonitor: ScreenFilterMonitorImpl,
    ): ScreenFilterMonitor {
        if (SDK_INT <= 29) {
            lifecycleManager.registerService(screenFilterMonitor)
        }
        return screenFilterMonitor
    }

    @Provides
    @Singleton
    internal fun provideNetworkUsageMetrics(
        lifecycleManager: LifecycleManager,
    ): NetworkUsageMetrics {
        val networkUsageMetrics = NetworkUsageMetricsImpl()
        lifecycleManager.registerService(networkUsageMetrics)
        return networkUsageMetrics
    }

    @Provides
    @Singleton
    internal fun provideDozeWatchdog(lifecycleManager: LifecycleManager): DozeWatchdog {
        val dozeWatchdog = DozeWatchdogImpl(application)
        lifecycleManager.registerService(dozeWatchdog)
        return dozeWatchdog
    }

    @Provides
    @Singleton
    internal fun provideLockManager(
        lifecycleManager: LifecycleManager,
        eventBus: EventBus,
        lockManager: LockManagerImpl,
    ): LockManager {
        lifecycleManager.registerService(lockManager)
        eventBus.addListener(lockManager)
        return lockManager
    }

    @Provides
    @Singleton
    internal fun provideRecentEmoji(
        lifecycleManager: LifecycleManager,
        recentEmoji: RecentEmojiImpl,
    ): RecentEmoji {
        lifecycleManager.registerOpenDatabaseHook(recentEmoji)
        return recentEmoji
    }

    /**
     * Поведение приложения по фичам:
     *  - image attachments / profile pictures / disappearing messages — включены
     *  - private groups / forums / blogs в ядре — **отключены**
     *
     * Отключение последних трёх в `FeatureFlags` нужно, чтобы апстримное
     * Briar Core / Bramble Core не делало автоматический шаринг блогов
     * с новым контактом, не пыталось доставлять forum/group invitation'ы и т.п.
     */
    @Provides
    internal fun provideFeatureFlags(): FeatureFlags = object : FeatureFlags {
        override fun shouldEnableImageAttachments(): Boolean = true
        override fun shouldEnableProfilePictures(): Boolean = true
        override fun shouldEnableDisappearingMessages(): Boolean = true
        override fun shouldEnablePrivateGroupsInCore(): Boolean = false
        override fun shouldEnableForumsInCore(): Boolean = false
        override fun shouldEnableBlogsInCore(): Boolean = false
    }

    companion object {
        @JvmStatic
        fun getAndroidComponent(ctx: Context): AndroidComponent {
            val app = ctx.applicationContext as MeshyApplication
            return app.getApplicationComponent()
        }
    }
}