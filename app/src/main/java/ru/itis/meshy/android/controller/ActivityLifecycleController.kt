package ru.itis.meshy.android.controller

import android.app.Activity

interface ActivityLifecycleController {

	fun onActivityCreate(
		activity: Activity
	)

	fun onActivityStart()

	fun onActivityStop()

	fun onActivityDestroy()
}