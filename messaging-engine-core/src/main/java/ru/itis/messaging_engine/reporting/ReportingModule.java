package ru.itis.messaging_engine.reporting;

import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.reporting.DevReporter;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ReportingModule {

	public static class EagerSingletons {
		@Inject
		DevReporter devReporter;
	}

	@Provides
	@Singleton
	DevReporter provideDevReporter(DevReporterImpl devReporter,
			EventBus eventBus) {
		eventBus.addListener(devReporter);
		return devReporter;
	}
}
