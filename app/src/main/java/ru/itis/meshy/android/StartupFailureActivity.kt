package ru.itis.meshy.android

import android.content.Intent
import android.os.Bundle
import org.briarproject.nullsafety.MethodsNotNullByDefault
import org.briarproject.nullsafety.ParametersNotNullByDefault
import ru.itis.meshy.R
import ru.itis.meshy.android.MeshyService.Companion.EXTRA_START_RESULT
import ru.itis.meshy.android.activity.ActivityComponent
import ru.itis.meshy.android.activity.BaseActivity
import ru.itis.meshy.android.fragment.BaseFragment.BaseFragmentListener
import ru.itis.meshy.android.fragment.ErrorFragment
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult

/**
 * Активити, открывающаяся, когда [ru.itis.meshy.android.MeshyService] не
 * смог поднять lifecycle-менеджер. Получает [StartResult] из Intent
 * extras и показывает соответствующий [ErrorFragment].
 *
 * `runOnDbThread` намеренно бросает исключение — здесь нет инициализированной
 * БД, и любая попытка работать с ней — программная ошибка.
 */
@MethodsNotNullByDefault
@ParametersNotNullByDefault
class StartupFailureActivity : BaseActivity(), BaseFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fragment_container)
        handleIntent(intent)
    }

    override fun injectActivity(component: ActivityComponent) {
        component.inject(this)
    }

    private fun handleIntent(i: Intent) {
        @Suppress("DEPRECATION")
        val result = i.getSerializableExtra(EXTRA_START_RESULT) as StartResult

        val errorRes = when (result) {
            StartResult.CLOCK_ERROR -> R.string.startup_failed_clock_error
            StartResult.DATA_TOO_OLD_ERROR -> R.string.startup_failed_data_too_old_error
            StartResult.DATA_TOO_NEW_ERROR -> R.string.startup_failed_data_too_new_error
            StartResult.DB_ERROR -> R.string.startup_failed_db_error
            StartResult.SERVICE_ERROR -> R.string.startup_failed_service_error
            else -> throw IllegalArgumentException("Unexpected start result: $result")
        }
        showInitialFragment(ErrorFragment.newInstance(getString(errorRes)))
    }

    override fun runOnDbThread(runnable: Runnable) {
        throw UnsupportedOperationException()
    }
}