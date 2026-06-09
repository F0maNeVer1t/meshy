package ru.itis.messaging_engine.plugin;

import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.plugin.BackoffFactory;
import ru.itis.messaging_engine.api.plugin.PluginConfig;
import ru.itis.messaging_engine.api.plugin.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PluginModule {

	public static class EagerSingletons {
		@Inject
		PluginManager pluginManager;
		@Inject
		Poller poller;
	}

	@Provides
	BackoffFactory provideBackoffFactory() {
		return new BackoffFactoryImpl();
	}

	@Provides
	@Singleton
	PluginManager providePluginManager(LifecycleManager lifecycleManager,
			PluginManagerImpl pluginManager) {
		lifecycleManager.registerService(pluginManager);
		return pluginManager;
	}

	@Provides
	@Singleton
	Poller providePoller(PluginConfig config, EventBus eventBus,
			PollerImpl poller) {
		if (config.shouldPoll()) eventBus.addListener(poller);
		return poller;
	}
}
