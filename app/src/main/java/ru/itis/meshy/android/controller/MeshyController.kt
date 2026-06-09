package ru.itis.meshy.android.controller

import ru.itis.messaging_engine.api.system.Wakeful
import ru.itis.meshy.android.controller.handler.ResultHandler

interface MeshyController : ActivityLifecycleController {

	fun startAndBindService()

	fun accountSignedIn(): Boolean

	/**
	 * Returns true via the handler when the app
	 * has entered doze mode without whitelist permission.
	 */
	fun hasDozed(
		handler: ResultHandler<Boolean>
	)

	fun doNotAskAgainForDozeWhiteListing()

	@Wakeful
	fun signOut(
		handler: ResultHandler<Void>,
		deleteAccount: Boolean
	)

	fun deleteAccount()
}