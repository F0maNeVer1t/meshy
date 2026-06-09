package ru.itis.meshy.android.fragment

import ru.itis.messaging_engine.api.event.EventBus
import ru.itis.messaging_engine.api.event.EventListener
import javax.inject.Inject

abstract class BaseEventFragment : BaseFragment(), EventListener {

	@Inject
    lateinit var eventBus: EventBus

	override fun onStart() {
		super.onStart()
		eventBus.addListener(this)
	}

	override fun onStop() {
		super.onStop()
		eventBus.removeListener(this)
	}
}