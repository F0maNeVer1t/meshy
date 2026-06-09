package ru.itis.meshy.android.activity

import android.app.Activity
import dagger.Module
import dagger.Provides
import ru.itis.meshy.android.MeshyService.MeshyServiceConnection
import ru.itis.meshy.android.controller.DbController
import ru.itis.meshy.android.controller.DbControllerImpl
import ru.itis.meshy.android.controller.MeshyController
import ru.itis.meshy.android.controller.MeshyControllerImpl

@Module
class ActivityModule(
	private val activity: BaseActivity
) {

	@ActivityScope
	@Provides
	fun provideBaseActivity(): BaseActivity {
		return activity
	}

	@ActivityScope
	@Provides
	fun provideActivity(): Activity {
		return activity
	}

	@ActivityScope
	@Provides
	protected fun provideMeshyController(
		meshyController: MeshyControllerImpl
	): MeshyController {

		activity.addLifecycleController(meshyController)

		return meshyController
	}

	@ActivityScope
	@Provides
	fun provideDbController(
		dbController: DbControllerImpl
	): DbController {
		return dbController
	}

	@ActivityScope
	@Provides
	fun provideMeshyServiceConnection():
		MeshyServiceConnection {

		return MeshyServiceConnection()
	}
}