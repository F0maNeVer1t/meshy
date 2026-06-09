package ru.itis.meshy.android.controller

import ru.itis.messaging_engine.api.db.DatabaseExecutor
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager
import java.util.concurrent.Executor
import java.util.logging.Logger
import javax.inject.Inject

@Deprecated(
	message = "Use structured executors or coroutines instead"
)
open class DbControllerImpl @Inject constructor(
	@DatabaseExecutor
	protected val dbExecutor: Executor,
	private val lifecycleManager: LifecycleManager
) : DbController {

	override fun runOnDbThread(
		task: Runnable
	) {

		dbExecutor.execute {

			try {

				lifecycleManager.waitForDatabase()

				task.run()

			} catch (e: InterruptedException) {

				LOG.warning(
					"Interrupted while waiting for database"
				)

				Thread.currentThread().interrupt()
			}
		}
	}

	companion object {

		private val LOG: Logger =
			Logger.getLogger(
				DbControllerImpl::class.java.name
			)
	}
}