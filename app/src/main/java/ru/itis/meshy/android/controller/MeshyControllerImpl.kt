package ru.itis.meshy.android.controller

import android.app.Activity
import android.content.Intent
import android.os.IBinder
import androidx.annotation.CallSuper
import org.briarproject.android.dontkillmelib.DozeUtils.needsDozeWhitelisting
import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager
import ru.itis.messaging_engine.api.account.AccountManager
import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.messaging_engine.api.db.DbException
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES
import ru.itis.messaging_engine.api.settings.Settings
import ru.itis.messaging_engine.api.settings.SettingsManager
import ru.itis.messaging_engine.util.LogUtils.logException
import ru.itis.meshy.android.MeshyApplication
import ru.itis.meshy.android.MeshyService
import ru.itis.meshy.android.MeshyService.MeshyBinder
import ru.itis.meshy.android.MeshyService.MeshyServiceConnection
import ru.itis.meshy.android.controller.handler.ResultHandler
import ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE
import ru.itis.meshy.api.android.DozeWatchdog
import java.util.concurrent.Executor
import java.util.logging.Level.WARNING
import java.util.logging.Logger
import java.util.logging.Logger.getLogger
import javax.inject.Inject

open class MeshyControllerImpl @Inject constructor(
	private val serviceConnection: MeshyServiceConnection,
	private val accountManager: AccountManager,
	private val lifecycleManager: LifecycleManager,
	@DatabaseExecutor
	private val databaseExecutor: Executor,
	private val settingsManager: SettingsManager,
	private val dozeWatchdog: DozeWatchdog,
	private val wakeLockManager: AndroidWakeLockManager,
	private val activity: Activity
) : MeshyController {

	private var bound = false

	@CallSuper
	override fun onActivityCreate(activity: Activity) {
		if (accountManager.hasDatabaseKey()) {
			startAndBindService()
		}
	}

	override fun onActivityStart() {
		// no-op
	}

	override fun onActivityStop() {
		// no-op
	}

	@CallSuper
	override fun onActivityDestroy() {
		unbindService()
	}

	override fun startAndBindService() {

		activity.startService(
			Intent(activity, MeshyService::class.java)
		)

		bound = activity.bindService(
			Intent(activity, MeshyService::class.java),
			serviceConnection,
			0
		)
	}

	override fun accountSignedIn(): Boolean {

		return accountManager.hasDatabaseKey() &&
			lifecycleManager.lifecycleState.isAfter(
				STARTING_SERVICES
			)
	}

	override fun hasDozed(
		handler: ResultHandler<Boolean>
	) {
		val app =
			activity.application as MeshyApplication

		if (
			app.isInstrumentationTest() ||
			!dozeWatchdog.getAndResetDozeFlag() ||
			!needsDozeWhitelisting(activity)
		) {
			handler.onResult(false)
			return
		}

		databaseExecutor.execute {

			try {

				val settings = settingsManager.getSettings(
					SETTINGS_NAMESPACE
				)

				val ask = settings.getBoolean(
					DOZE_ASK_AGAIN,
					true
				)

				handler.onResult(ask)

			} catch (e: DbException) {
				logException(LOG, WARNING, e)
			}
		}
	}

	override fun doNotAskAgainForDozeWhiteListing() {

		databaseExecutor.execute {

			try {

				val settings = Settings()

				settings.putBoolean(
					DOZE_ASK_AGAIN,
					false
				)

				settingsManager.mergeSettings(
					settings,
					SETTINGS_NAMESPACE
				)

			} catch (e: DbException) {
				logException(LOG, WARNING, e)
			}
		}
	}

	override fun signOut(
		handler: ResultHandler<Void>,
		deleteAccount: Boolean
	) {

		wakeLockManager.executeWakefully({

			try {

				// Wait until the service finishes startup
				val binder: IBinder =
					serviceConnection.waitForBinder()

				val service =
					(binder as MeshyBinder).getService()

				service.waitForStartup()

				LOG.info("Shutting down Meshy service")

				// Shutdown service and wait for completion
				service.shutdown(true)

				service.waitForShutdown()

			} catch (e: InterruptedException) {

				LOG.warning(
					"Interrupted while waiting for service"
				)

			} finally {

				if (deleteAccount) {
					accountManager.deleteAccount()
				}
			}

			handler.onResult(null)

		}, "SignOut")
	}

	override fun deleteAccount() {
		accountManager.deleteAccount()
	}

	private fun unbindService() {

		if (bound) {
			activity.unbindService(serviceConnection)
		}
	}

	companion object {

		private val LOG: Logger =
			getLogger(MeshyControllerImpl::class.java.name)

		const val DOZE_ASK_AGAIN = "dozeAskAgain"
	}
}