package ru.itis.meshy.android.activity

import android.app.Activity
import dagger.Component
import ru.itis.meshy.android.AndroidComponent
import ru.itis.meshy.android.StartupFailureActivity
import ru.itis.meshy.android.account.SetupActivity
import ru.itis.meshy.android.account.SetupFragment
import ru.itis.meshy.android.account.UnlockActivity
import ru.itis.meshy.android.contact.ContactListFragment
import ru.itis.meshy.android.contact.add.nearby.AddNearbyContactActivity
import ru.itis.meshy.android.contact.add.nearby.AddNearbyContactErrorFragment
import ru.itis.meshy.android.contact.add.nearby.AddNearbyContactFragment
import ru.itis.meshy.android.contact.add.nearby.AddNearbyContactIntroFragment
import ru.itis.meshy.android.contact.add.remote.AddContactActivity
import ru.itis.meshy.android.contact.add.remote.LinkExchangeFragment
import ru.itis.meshy.android.contact.add.remote.NicknameFragment
import ru.itis.meshy.android.contact.add.remote.PendingContactListActivity
import ru.itis.meshy.android.contact.connect.ConnectViaBluetoothActivity
import ru.itis.meshy.android.conversation.AliasDialogFragment
import ru.itis.meshy.android.conversation.ConversationActivity
import ru.itis.meshy.android.conversation.ConversationSettingsDialog
import ru.itis.meshy.android.conversation.ImageActivity
import ru.itis.meshy.android.conversation.ImageFragment
import ru.itis.meshy.android.fragment.ScreenFilterDialogFragment
import ru.itis.meshy.android.hotspot.HotspotActivity
import ru.itis.meshy.android.introduction.ContactChooserFragment
import ru.itis.meshy.android.introduction.IntroductionActivity
import ru.itis.meshy.android.introduction.IntroductionMessageFragment
import ru.itis.meshy.android.login.ChangePasswordActivity
import ru.itis.meshy.android.login.OpenDatabaseFragment
import ru.itis.meshy.android.login.PasswordFragment
import ru.itis.meshy.android.login.StartupActivity
import ru.itis.meshy.android.mailbox.MailboxActivity
import ru.itis.meshy.android.navdrawer.NavDrawerActivity
import ru.itis.meshy.android.navdrawer.TransportsActivity
import ru.itis.meshy.android.removabledrive.RemovableDriveActivity
import ru.itis.meshy.android.reporting.CrashFragment
import ru.itis.meshy.android.reporting.CrashReportActivity
import ru.itis.meshy.android.reporting.ReportFormFragment
import ru.itis.meshy.android.settings.ConfirmAvatarDialogFragment
import ru.itis.meshy.android.settings.SettingsActivity
import ru.itis.meshy.android.settings.SettingsFragment
import ru.itis.meshy.android.sharing.SharingModule
import ru.itis.meshy.android.splash.SplashScreenActivity

@ActivityScope
@Component(
	modules = [
		ActivityModule::class,
		SharingModule.SharingLegacyModule::class
	],
	dependencies = [AndroidComponent::class]
)
interface ActivityComponent {

	fun activity(): Activity

	fun inject(activity: SplashScreenActivity)

	fun inject(activity: StartupActivity)

	fun inject(activity: SetupActivity)

	fun inject(activity: NavDrawerActivity)

	fun inject(activity: AddNearbyContactActivity)

	fun inject(activity: ConversationActivity)

	fun inject(activity: ImageActivity)

	fun inject(activity: SettingsActivity)

	fun inject(activity: TransportsActivity)

	fun inject(activity: ChangePasswordActivity)

	fun inject(activity: IntroductionActivity)

	fun inject(activity: StartupFailureActivity)

	fun inject(activity: UnlockActivity)

	fun inject(activity: AddContactActivity)

	fun inject(activity: PendingContactListActivity)

	fun inject(activity: CrashReportActivity)

	fun inject(activity: HotspotActivity)

	fun inject(activity: RemovableDriveActivity)

	// Fragments

	fun inject(fragment: SetupFragment)

	fun inject(fragment: PasswordFragment)

	fun inject(fragment: OpenDatabaseFragment)

	fun inject(fragment: ContactListFragment)

	fun inject(fragment: AddNearbyContactIntroFragment)

	fun inject(fragment: AddNearbyContactFragment)

	fun inject(fragment: LinkExchangeFragment)

	fun inject(fragment: NicknameFragment)

	fun inject(fragment: ContactChooserFragment)

	fun inject(fragment: IntroductionMessageFragment)

	fun inject(fragment: SettingsFragment)

	fun inject(fragment: ScreenFilterDialogFragment)

	fun inject(fragment: AddNearbyContactErrorFragment)

	fun inject(fragment: AliasDialogFragment)

	fun inject(fragment: ImageFragment)

	fun inject(fragment: ReportFormFragment)

	fun inject(fragment: CrashFragment)

	fun inject(fragment: ConfirmAvatarDialogFragment)

	fun inject(dialog: ConversationSettingsDialog)

	fun inject(activity: ConnectViaBluetoothActivity)

	fun inject(activity: MailboxActivity)
}