package ru.itis.messaging_engine;

import ru.itis.messaging_engine.battery.AndroidBatteryModule;
import ru.itis.messaging_engine.network.AndroidNetworkModule;
import ru.itis.messaging_engine.reporting.ReportingModule;

public interface MessagingEngineAndroidEagerSingletons {

	void inject(AndroidBatteryModule.EagerSingletons init);

	void inject(AndroidNetworkModule.EagerSingletons init);

	void inject(ReportingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				MessagingEngineAndroidEagerSingletons c) {
			c.inject(new AndroidBatteryModule.EagerSingletons());
			c.inject(new AndroidNetworkModule.EagerSingletons());
			c.inject(new ReportingModule.EagerSingletons());
		}
	}
}
