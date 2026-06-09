package ru.itis.messaging_engine.system;

import ru.itis.messaging_engine.api.lifecycle.IoExecutor;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;
import ru.itis.messaging_engine.api.system.WakefulIoExecutor;

/**
 * Provides a default implementation of {@link WakefulIoExecutor} for systems
 * without wake locks.
 */
@Module
public class DefaultWakefulIoExecutorModule {

	@Provides
	@WakefulIoExecutor
	Executor provideWakefulIoExecutor(@IoExecutor Executor ioExecutor) {
		return ioExecutor;
	}
}
