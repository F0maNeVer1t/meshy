package ru.itis.meshy.android.fragment

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.android.DestroyableContext
import ru.itis.meshy.android.activity.ActivityComponent

/**
 * Базовый фрагмент приложения. Подклассы обязаны выставить [uniqueTag];
 * опционально — переопределить [injectFragment] для Dagger-инъекции.
 *
 * `getActivityComponent()` из Java-интерфейса [BaseFragmentListener]
 * стал Kotlin property `activityComponent`. На уровне bytecode подпись
 * остаётся той же — `abstract ActivityComponent getActivityComponent()`,
 * — поэтому ещё-не-мигрированные Java-имплементации продолжают работать
 * без правок. Унаследованный property `activityComponent` из `BaseActivity`
 * автоматически удовлетворяет контракт у подклассов вроде
 * `CrashReportActivity`, `IntroductionActivity`, и т.п.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
abstract class BaseFragment : Fragment(), DestroyableContext {

    protected lateinit var listener: BaseFragmentListener

    abstract val uniqueTag: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as BaseFragmentListener
        injectFragment(listener.activityComponent)
    }

    /** Подклассы, которым нужна DI-инъекция, переопределяют этот метод. */
    open fun injectFragment(component: ActivityComponent) = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow the "up" button to act as back button.
        @Suppress("DEPRECATION") // setHasOptionsMenu deprecated с Fragment 1.5
        setHasOptionsMenu(true)
    }

    @Deprecated(
        "setHasOptionsMenu / onOptionsItemSelected заменены MenuProvider API. " +
                "Миграция — отдельная задача.",
    )
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            @Suppress("DEPRECATION") // onBackPressed deprecated в API 33; миграция отдельная
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @UiThread
    protected open fun finish() {
        activity?.supportFinishAfterTransition()
    }

    /**
     * Контракт, который должна имплементировать хост-Activity для
     * [BaseFragment]. Реализуется конкретными подклассами `BaseActivity`.
     * Большинство методов унаследованы от `BaseActivity` автоматически;
     * `runOnDbThread` каждый подкласс реализует сам.
     */
    interface BaseFragmentListener {
        @Deprecated("Use DbExecutor / lifecycleScope coroutines.")
        fun runOnDbThread(runnable: Runnable)

        @UiThread
        fun onBackPressed()

        @get:UiThread
        val activityComponent: ActivityComponent

        @UiThread
        fun showNextFragment(f: BaseFragment)

        @UiThread
        fun handleException(e: Exception)
    }

    @Deprecated("Use viewLifecycleOwner.lifecycleScope coroutines for UI work.")
    @CallSuper
    override fun runOnUiThreadUnlessDestroyed(r: Runnable) {
        val activity = activity ?: return
        activity.runOnUiThread {
            // Note we don't check a `destroyed` flag — the Fragment is still attached.
            if (isAdded && !activity.isFinishing) {
                r.run()
            }
        }
    }

    protected open fun showNextFragment(f: BaseFragment) {
        listener.showNextFragment(f)
    }

    @UiThread
    protected open fun handleException(e: Exception) {
        listener.handleException(e)
    }
}