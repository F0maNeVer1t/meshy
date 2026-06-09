package ru.itis.messaging_engine.event;

import ru.itis.messaging_engine.api.event.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EventModule {

	@Provides
	@Singleton
	EventBus provideEventBus(EventBusImpl eventBus) {
		return eventBus;
	}
}
