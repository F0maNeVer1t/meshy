package ru.itis.messaging_engine.system;

import android.app.Application;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.itis.messaging_engine.api.system.AlarmListener;
import ru.itis.messaging_engine.api.system.TaskScheduler;

@Module
public class AndroidTaskSchedulerModule {

	public static class EagerSingletons {
		@Inject
		AndroidTaskScheduler scheduler;
	}

	@Provides
	@Singleton
	AndroidTaskScheduler provideAndroidTaskScheduler(
			LifecycleManager lifecycleManager, Application app,
			AndroidWakeLockManager wakeLockManager,
			ScheduledExecutorService scheduledExecutorService) {
		AndroidTaskScheduler scheduler = new AndroidTaskScheduler(app,
				wakeLockManager, scheduledExecutorService);
		lifecycleManager.registerService(scheduler);
		return scheduler;
	}

	@Provides
	@Singleton
	AlarmListener provideAlarmListener(AndroidTaskScheduler scheduler) {
		return scheduler;
	}

	@Provides
	@Singleton
	TaskScheduler provideTaskScheduler(AndroidTaskScheduler scheduler) {
		return scheduler;
	}
}
