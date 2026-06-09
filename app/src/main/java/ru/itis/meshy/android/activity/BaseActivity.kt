package ru.itis.meshy.android.activity

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle.State.STARTED
import ru.itis.meshy.R
import ru.itis.meshy.android.AndroidComponent
import ru.itis.meshy.android.DestroyableContext
import ru.itis.meshy.android.Localizer
import ru.itis.meshy.android.MeshyApplication
import ru.itis.meshy.android.controller.ActivityLifecycleController
import ru.itis.meshy.android.fragment.BaseFragment
import ru.itis.meshy.android.fragment.ScreenFilterDialogFragment
import ru.itis.meshy.android.util.UiUtils
import ru.itis.meshy.android.util.UiUtils.hideSoftKeyboard
import ru.itis.meshy.android.util.UiUtils.showFragment
import ru.itis.meshy.android.widget.TapSafeFrameLayout
import ru.itis.meshy.android.widget.TapSafeFrameLayout.OnTapFilteredListener
import ru.itis.meshy.api.android.ScreenFilterMonitor
import ru.itis.meshy.api.android.ScreenFilterMonitor.AppDetails
import java.util.Collections.emptyList
import java.util.logging.Level.INFO
import java.util.logging.Logger
import java.util.logging.Logger.getLogger
import javax.inject.Inject

/**
 * Warning: some activities do not extend [BaseActivity].
 */
abstract class BaseActivity : AppCompatActivity(),
	DestroyableContext,
	OnTapFilteredListener {

	@Inject
    internal lateinit var screenFilterMonitor: ScreenFilterMonitor

    lateinit var activityComponent: ActivityComponent
        private set

    private val lifecycleControllers =
		mutableListOf<ActivityLifecycleController>()

	private var destroyed = false

	private var toolbar: Toolbar? = null

	private var searchedForToolbar = false

	abstract fun injectActivity(component: ActivityComponent)

	fun addLifecycleController(
		controller: ActivityLifecycleController
	) {
		lifecycleControllers.add(controller)
	}

	override fun onCreate(savedInstanceState: Bundle?) {

		// Create ActivityComponent before calling super.onCreate()
		// because fragments may already require dependency injection
        val applicationComponent: AndroidComponent =
            (application as MeshyApplication).getApplicationComponent()

		activityComponent = DaggerActivityComponent.builder()
			.androidComponent(applicationComponent)
			.activityModule(getActivityModule())
			.build()

		injectActivity(activityComponent)

		super.onCreate(savedInstanceState)

		if (LOG.isLoggable(INFO)) {
			LOG.info("Creating ${javaClass.simpleName}")
		}

        window.addFlags(FLAG_SECURE)

		if (SDK_INT >= 31) {
			window.setHideOverlayWindows(true)
		}

		for (controller in lifecycleControllers) {
			controller.onActivityCreate(this)
		}
	}

	override fun attachBaseContext(base: Context) {
		super.attachBaseContext(
			Localizer.getInstance().setLocale(base)
		)

		Localizer.getInstance().setLocale(this)
	}

	// Exists to simplify test overrides
	protected open fun getActivityModule(): ActivityModule {
		return ActivityModule(this)
	}

	override fun onStart() {
		super.onStart()

		if (LOG.isLoggable(INFO)) {
			LOG.info("Starting ${javaClass.simpleName}")
		}

		for (controller in lifecycleControllers) {
			controller.onActivityStart()
		}

		protectToolbar()

		val fragment = findDialogFragment()

		fragment?.setDismissListener {
			protectToolbar()
		}
	}

	private fun findDialogFragment():
		ScreenFilterDialogFragment? {

		val fragment = supportFragmentManager.findFragmentByTag(
			ScreenFilterDialogFragment.TAG
		)

		return fragment as? ScreenFilterDialogFragment
	}

	override fun onResume() {
		super.onResume()

		if (LOG.isLoggable(INFO)) {
			LOG.info("Resuming ${javaClass.simpleName}")
		}
	}

	override fun onPause() {
		super.onPause()

		if (LOG.isLoggable(INFO)) {
			LOG.info("Pausing ${javaClass.simpleName}")
		}
	}

	override fun onStop() {
		super.onStop()

		if (LOG.isLoggable(INFO)) {
			LOG.info("Stopping ${javaClass.simpleName}")
		}

		for (controller in lifecycleControllers) {
			controller.onActivityStop()
		}
	}

	protected fun showInitialFragment(fragment: BaseFragment) {
		supportFragmentManager.beginTransaction()
			.replace(
				R.id.fragmentContainer,
				fragment,
				fragment.uniqueTag
			)
			.commit()
	}

	fun showNextFragment(fragment: BaseFragment) {

		if (!lifecycle.currentState.isAtLeast(STARTED)) {
			return
		}

		showFragment(
			supportFragmentManager,
			fragment,
			fragment.uniqueTag
		)
	}

	private fun showScreenFilterWarning(): Boolean {

		if ((application as MeshyApplication)
				.isInstrumentationTest()
		) {
			return false
		}

		// If dialog is already visible — filter the tap
		val existingDialog = findDialogFragment()

		if (existingDialog != null &&
			existingDialog.isVisible
		) {
			return false
		}

		val apps: Collection<AppDetails>

		// Querying all apps is only supported on API 29 and below
		if (SDK_INT <= 29) {
			apps = screenFilterMonitor.getApps()

			// Allow the tap if all overlay apps were approved
			if (apps.isEmpty()) {
				return true
			}
		} else {
			apps = emptyList()
		}

		// Avoid showing dialog after onSaveInstanceState()
		val fragmentManager: FragmentManager =
			supportFragmentManager

		if (!fragmentManager.isStateSaved) {

			val dialog =
				ScreenFilterDialogFragment.newInstance(apps)

			dialog.setDismissListener {
				protectToolbar()
			}

			// Hide keyboard before showing dialog
			currentFocus?.let {
				hideSoftKeyboard(it)
			}

			dialog.show(
				fragmentManager,
				ScreenFilterDialogFragment.TAG
			)
		}

		// Filter the tap
		return false
	}

	override fun onDestroy() {
		super.onDestroy()

		if (LOG.isLoggable(INFO)) {
			LOG.info("Destroying ${javaClass.simpleName}")
		}

		destroyed = true

		for (controller in lifecycleControllers) {
			controller.onActivityDestroy()
		}
	}

	override fun runOnUiThreadUnlessDestroyed(
		runnable: Runnable
	) {
		runOnUiThread {

			if (!destroyed && !isFinishing) {
				runnable.run()
			}
		}
	}

	@UiThread
	open fun handleException(exception: Exception) {
		supportFinishAfterTransition()
	}

	/**
	 * Wraps a view inside a TapSafeFrameLayout
	 * that reports filtered touches.
	 */
	private fun makeTapSafeWrapper(view: View): View {

		return TapSafeFrameLayout(this).apply {
			layoutParams = LayoutParams(
				MATCH_PARENT,
				MATCH_PARENT
			)

			setOnTapFilteredListener(this@BaseActivity)

			addView(view)
		}
	}

	/**
	 * Finds AppCompat Toolbar and enables
	 * obscured touch filtering.
	 *
	 * Custom toolbars are already protected
	 * because they belong to the wrapped content view.
	 */
	private fun protectToolbar() {

		findToolbar()

		toolbar?.let { toolbar ->

			val shouldFilter =
				if (SDK_INT <= 29) {
					screenFilterMonitor.getApps().isNotEmpty()
				} else {
					true
				}

			UiUtils.setFilterTouchesWhenObscured(
				toolbar,
				shouldFilter
			)
		}
	}

	private fun findToolbar() {

		if (searchedForToolbar) {
			return
		}

		val decorView = window.decorView

		if (decorView is ViewGroup) {
			toolbar = findToolbar(decorView)
		}

		searchedForToolbar = true
	}

	private fun findToolbar(
		viewGroup: ViewGroup
	): Toolbar? {

		// Views inside TapSafeFrameLayout
		// are already protected
		if (viewGroup is TapSafeFrameLayout) {
			return null
		}

		for (i in 0 until viewGroup.childCount) {

			val child = viewGroup.getChildAt(i)

			if (child is Toolbar) {
				return child
			}

			if (child is ViewGroup) {

				val toolbar = findToolbar(child)

				if (toolbar != null) {
					return toolbar
				}
			}
		}

		return null
	}

	override fun setContentView(
		@LayoutRes layoutResId: Int
	) {
		setContentView(
			layoutInflater.inflate(layoutResId, null)
		)
	}

	override fun setContentView(view: View) {
		super.setContentView(
			makeTapSafeWrapper(view)
		)
	}

	override fun setContentView(
		view: View,
		layoutParams: LayoutParams
	) {
		super.setContentView(
			makeTapSafeWrapper(view),
			layoutParams
		)
	}

	override fun addContentView(
		view: View,
		layoutParams: LayoutParams
	) {
		super.addContentView(
			makeTapSafeWrapper(view),
			layoutParams
		)
	}

	override fun shouldAllowTap(): Boolean {
		return showScreenFilterWarning()
	}

	companion object {

		private val LOG: Logger =
			getLogger(BaseActivity::class.java.name)
	}
}