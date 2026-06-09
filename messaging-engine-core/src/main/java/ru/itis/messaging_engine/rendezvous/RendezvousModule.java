package ru.itis.messaging_engine.rendezvous;

import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.rendezvous.RendezvousPoller;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RendezvousModule {

	public static class EagerSingletons {
		@Inject
		RendezvousPoller rendezvousPoller;
	}

	@Provides
	RendezvousCrypto provideRendezvousCrypto(
			RendezvousCryptoImpl rendezvousCrypto) {
		return rendezvousCrypto;
	}

	@Provides
	@Singleton
	RendezvousPoller provideRendezvousPoller(LifecycleManager lifecycleManager,
			EventBus eventBus, RendezvousPollerImpl rendezvousPoller) {
		lifecycleManager.registerService(rendezvousPoller);
		eventBus.addListener(rendezvousPoller);
		return rendezvousPoller;
	}
}
