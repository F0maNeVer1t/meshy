package ru.itis.meshy.android.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.os.Build.VERSION.SDK_INT
import android.transition.Transition
import android.widget.CheckBox
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.briarproject.android.dontkillmelib.DozeUtils.getDozeWhitelistingIntent
import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager
import ru.itis.messaging_engine.api.system.Wakeful
import ru.itis.messaging_engine.util.LogUtils.logException
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.R
import ru.itis.meshy.android.MeshyApplication
import ru.itis.meshy.android.account.UnlockActivity
import ru.itis.meshy.android.activity.RequestCodes.REQUEST_DOZE_WHITELISTING
import ru.itis.meshy.android.activity.RequestCodes.REQUEST_PASSWORD
import ru.itis.meshy.android.activity.RequestCodes.REQUEST_UNLOCK
import ru.itis.meshy.android.controller.DbController
import ru.itis.meshy.android.controller.MeshyController
import ru.itis.meshy.android.controller.handler.UiResultHandler
import ru.itis.meshy.android.login.StartupActivity
import ru.itis.meshy.android.logout.ExitActivity
import ru.itis.meshy.android.util.UiUtils.excludeSystemUi
import ru.itis.meshy.android.util.UiUtils.isSamsung7
import ru.itis.meshy.api.android.LockManager
import java.util.concurrent.Executor
import java.util.logging.Level.INFO
import java.util.logging.Level.WARNING
import java.util.logging.Logger

import javax.inject.Inject

/**
 * Базовая Activity для всех экранов, требующих залогиненного пользователя.
 *
 * Унаследована от [BaseActivity], добавляет:
 *  - перенаправление на [StartupActivity], если пользователь не залогинен,
 *  - перенаправление на [UnlockActivity], если приложение залочено,
 *  - показ диалога-предупреждения о Doze-режиме,
 *  - стандартизированный `signOut` / `exit`.
 *
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
abstract class MeshyActivity : BaseActivity() {

    @Inject
    internal lateinit var meshyController: MeshyController

    @Deprecated("Use coroutines / DbExecutor instead.")
    @Inject
    internal lateinit var dbController: DbController

    @Inject
    internal lateinit var lockManager: LockManager

    @Inject
    internal lateinit var wakeLockManager: AndroidWakeLockManager

    override fun onStart() {
        super.onStart()
        lockManager.onActivityStart()
    }

    @Suppress("DEPRECATION") // onActivityResult deprecated — миграция на ActivityResultContracts отдельно
    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        when (request) {
            REQUEST_PASSWORD -> {
                // Recreate the activity so any DB tasks that failed before
                // signing in can be retried.
                if (result == RESULT_OK) {
                    if (LOG.isLoggable(INFO)) {
                        LOG.info("Recreating ${javaClass.simpleName} after signing in")
                    }
                    recreate()
                }
            }
            REQUEST_UNLOCK -> if (result != RESULT_OK) {
                // We arrive here if the user presses 'back' in the Keyguard
                // unlock screen, because UnlockActivity finishes. If we don't
                // finish here, isFinishing will be false in onResume() and
                // we'd launch a new UnlockActivity, causing a loop.
                // If the result is OK, we don't need to do anything here.
                supportFinishAfterTransition()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!meshyController.accountSignedIn() && !isFinishing) {
            // Also check that the activity isn't finishing already. This is
            // possible if we finished in onActivityResult(). Launching another
            // StartupActivity would cause a loop.
            LOG.info("Not signed in, launching StartupActivity")
            val i = Intent(this, StartupActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(i, REQUEST_PASSWORD)
        } else if (lockManager.isLocked() && !isFinishing) {
            // Also check that the activity isn't finishing already.
            LOG.info("Locked, launching UnlockActivity")
            val i = Intent(this, UnlockActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(i, REQUEST_UNLOCK)
        } else if (SDK_INT >= 23) {
            meshyController.hasDozed(object : UiResultHandler<Boolean>(this) {
                override fun onResultUi(result: Boolean) {
                    if (result) showDozeDialog(R.string.dnkm_warning_dozed_1)
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        lockManager.onActivityStop()
    }

    /**
     * Sets the transition animations.
     *
     * @param enterTransition used to move views into initial positions.
     * @param exitTransition used to move views out when starting a **new** activity.
     * @param returnTransition used when window is closing because the activity is finishing.
     */
    protected open fun setSceneTransitionAnimation(
        enterTransition: Transition?,
        exitTransition: Transition?,
        returnTransition: Transition?,
    ) {
        // workaround for #1007
        if (isSamsung7()) return
        enterTransition?.let { excludeSystemUi(it) }
        exitTransition?.let { excludeSystemUi(it) }
        returnTransition?.let { excludeSystemUi(it) }
        window.enterTransition = enterTransition
        window.exitTransition = exitTransition
        window.returnTransition = returnTransition
    }

    /**
     * Should be called after the content view has been added in `onCreate()`.
     *
     * @param ownLayout `true` if the custom toolbar brings its own layout.
     * @return the [Toolbar] object, or `null` if content view didn't contain one.
     */
    protected open fun setUpCustomToolbar(ownLayout: Boolean): Toolbar? {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowCustomEnabled(ownLayout)
            setDisplayShowTitleEnabled(!ownLayout)
        }
        return toolbar
    }

    protected open fun showDozeDialog(@StringRes message: Int) {
        val b = MaterialAlertDialogBuilder(this, R.style.MeshyDialogTheme)
        b.setMessage(message)
        b.setView(R.layout.checkbox)
        b.setPositiveButton(R.string.fix) { dialog, _ ->
            val i = getDozeWhitelistingIntent(this@MeshyActivity)
            try {
                @Suppress("DEPRECATION")
                startActivityForResult(i, REQUEST_DOZE_WHITELISTING)
            } catch (e: ActivityNotFoundException) {
                logException(LOG, WARNING, e)
                Toast.makeText(this, R.string.error_start_activity, LENGTH_LONG).show()
            }
            dialog.dismiss()
        }
        b.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        b.setOnDismissListener { dialog ->
            val checkBox = (dialog as AlertDialog).findViewById<CheckBox>(R.id.checkbox)
            if (checkBox?.isChecked == true) {
                meshyController.doNotAskAgainForDozeWhiteListing()
            }
        }
        b.show()
    }

    protected fun signOut(removeFromRecentApps: Boolean, deleteAccount: Boolean) {
        // Hold a wake lock to ensure we exit before the device goes to sleep.
        wakeLockManager.runWakefully({
            if (meshyController.accountSignedIn()) {
                // Don't use UiResultHandler because we want the result even if
                // this activity has been destroyed.
                meshyController.signOut({ _ ->
                    // Локальная переменная названа exitTask, чтобы не пересекаться
                    // с приватным методом fun exit().
                    val exitTask = Runnable { exit(removeFromRecentApps) }
                    wakeLockManager.executeWakefully(
                        exitTask,
                        Executor { runOnUiThread(it) },
                        "SignOut",
                    )
                }, deleteAccount)
            } else {
                if (deleteAccount) meshyController.deleteAccount()
                exit(removeFromRecentApps)
            }
        }, "SignOut")
    }

    @Wakeful
    private fun exit(removeFromRecentApps: Boolean) {
        if (removeFromRecentApps) startExitActivity()
        else finishAndExit()
    }

    @Wakeful
    private fun startExitActivity() {
        val i = Intent(this, ExitActivity::class.java)
        i.addFlags(
            FLAG_ACTIVITY_NEW_TASK or
                FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                FLAG_ACTIVITY_NO_ANIMATION or
                FLAG_ACTIVITY_CLEAR_TASK,
        )
        startActivity(i)
    }

    @Wakeful
    private fun finishAndExit() {
        finishAndRemoveTask()
        LOG.info("Exiting")
        val app = application as MeshyApplication
        if (!app.isInstrumentationTest()) System.exit(0)
    }

    @Deprecated("Use lifecycleScope.launch(DbExecutor.asCoroutineDispatcher()).")
    open fun runOnDbThread(task: Runnable) {
        @Suppress("DEPRECATION") // dbController сам помечен deprecated
        dbController.runOnDbThread(task)
    }

    @Deprecated("Use lifecycleScope.launch(Dispatchers.Main).")
    protected fun finishOnUiThread() {
        runOnUiThreadUnlessDestroyed(Runnable { supportFinishAfterTransition() })
    }

    companion object {
        // Intent extra keys — namespace переименован под новое приложение.
        // Это внутри-приложенческие ключи, внешних потребителей нет.
        const val GROUP_ID = "meshy.GROUP_ID"
        const val GROUP_NAME = "meshy.GROUP_NAME"

        private val LOG: Logger = Logger.getLogger(MeshyActivity::class.java.name)
    }
}