package ru.itis.meshy.android.controller

@Deprecated(
	message = "Use structured executors or coroutines instead"
)
interface DbController {

	fun runOnDbThread(
		task: Runnable
	)
}