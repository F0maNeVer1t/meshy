package ru.itis.messaging_engine.system;

import dagger.Module;
import dagger.Provides;
import ru.itis.messaging_engine.api.system.Clock;

@Module
public class ClockModule {

	@Provides
	Clock provideClock() {
		return new SystemClock();
	}
}
