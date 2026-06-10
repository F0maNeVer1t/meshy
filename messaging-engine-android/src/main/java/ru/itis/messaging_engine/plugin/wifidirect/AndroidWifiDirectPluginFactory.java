package ru.itis.messaging_engine.plugin.wifidirect;

import android.app.Application;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.io.TimeoutMonitor;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.plugin.Backoff;
import ru.itis.messaging_engine.api.plugin.BackoffFactory;
import ru.itis.messaging_engine.api.plugin.PluginCallback;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.ID;

@Immutable
@NotNullByDefault
public class AndroidWifiDirectPluginFactory implements DuplexPluginFactory {

	private static final int MAX_LATENCY = 30 * 1000; // 30 seconds
	private static final int MAX_IDLE_TIME = 30 * 1000; // 30 seconds
	private static final int MIN_POLLING_INTERVAL = 20 * 1000; // 20 seconds
	private static final int MAX_POLLING_INTERVAL = 10 * 60 * 1000; // 10 mins
	private static final double BACKOFF_BASE = 1.2;

	private final Executor ioExecutor;
	private final AndroidWakeLockManager wakeLockManager;
	private final Application app;
	private final TimeoutMonitor timeoutMonitor;
	private final BackoffFactory backoffFactory;
	private final EventBus eventBus;

	@Inject
	AndroidWifiDirectPluginFactory(@IoExecutor Executor ioExecutor,
			AndroidWakeLockManager wakeLockManager,
			Application app,
			TimeoutMonitor timeoutMonitor,
			BackoffFactory backoffFactory,
			EventBus eventBus) {
		this.ioExecutor = ioExecutor;
		this.wakeLockManager = wakeLockManager;
		this.app = app;
		this.timeoutMonitor = timeoutMonitor;
		this.backoffFactory = backoffFactory;
		this.eventBus = eventBus;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Override
	public DuplexPlugin createPlugin(PluginCallback callback) {
		AndroidWifiDirectConnectionFactory connectionFactory =
				new AndroidWifiDirectConnectionFactory(wakeLockManager,
						timeoutMonitor);
		Backoff backoff = backoffFactory.createBackoff(MIN_POLLING_INTERVAL,
				MAX_POLLING_INTERVAL, BACKOFF_BASE);
		AndroidWifiDirectPlugin plugin = new AndroidWifiDirectPlugin(
				ioExecutor, app, backoff, callback, connectionFactory,
				MAX_LATENCY, MAX_IDLE_TIME);
		eventBus.addListener(plugin);
		return plugin;
	}
}
