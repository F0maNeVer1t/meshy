package ru.itis.messaging_engine.system;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;
import ru.itis.messaging_engine.api.system.WakefulIoExecutor;

@Module
public
class AndroidWakefulIoExecutorModule {

	@Provides
	@WakefulIoExecutor
	Executor provideWakefulIoExecutor(@IoExecutor Executor ioExecutor,
			AndroidWakeLockManager wakeLockManager) {
		return r -> wakeLockManager.executeWakefully(r, ioExecutor,
				"WakefulIoExecutor");
	}
}
