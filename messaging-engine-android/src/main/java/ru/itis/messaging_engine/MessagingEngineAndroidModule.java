package ru.itis.messaging_engine;

import ru.itis.messaging_engine.battery.AndroidBatteryModule;
import ru.itis.messaging_engine.io.DnsModule;
import ru.itis.messaging_engine.network.AndroidNetworkModule;
import ru.itis.messaging_engine.plugin.tor.CircumventionModule;
import ru.itis.messaging_engine.reporting.ReportingModule;
import ru.itis.messaging_engine.socks.SocksModule;
import ru.itis.messaging_engine.system.AndroidSystemModule;
import ru.itis.messaging_engine.system.AndroidTaskSchedulerModule;
import ru.itis.messaging_engine.system.AndroidWakeLockModule;
import ru.itis.messaging_engine.system.AndroidWakefulIoExecutorModule;
import ru.itis.messaging_engine.system.DefaultThreadFactoryModule;

import dagger.Module;

@Module(includes = {
		AndroidBatteryModule.class,
		AndroidNetworkModule.class,
		AndroidSystemModule.class,
		AndroidTaskSchedulerModule.class,
		AndroidWakefulIoExecutorModule.class,
		AndroidWakeLockModule.class,
		DefaultThreadFactoryModule.class,
		CircumventionModule.class,
		DnsModule.class,
		ReportingModule.class,
		SocksModule.class
})
public class MessagingEngineAndroidModule {
}
