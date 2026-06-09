package ru.itis.meshy.client;

import ru.itis.meshy.api.client.MessageTracker;

import dagger.Module;
import dagger.Provides;

@Module
public class BriarClientModule {

	@Provides
	MessageTracker provideMessageTracker(MessageTrackerImpl messageTracker) {
		return messageTracker;
	}
}
