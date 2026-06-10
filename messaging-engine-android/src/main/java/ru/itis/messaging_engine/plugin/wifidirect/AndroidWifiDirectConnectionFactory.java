package ru.itis.messaging_engine.plugin.wifidirect;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.io.TimeoutMonitor;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.net.Socket;

@NotNullByDefault
class AndroidWifiDirectConnectionFactory {

	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;

	AndroidWifiDirectConnectionFactory(
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor) {
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
	}

	DuplexTransportConnection wrapSocket(DuplexPlugin plugin, Socket socket)
			throws IOException {
		return new AndroidWifiDirectTransportConnection(plugin,
				wakeLockManager, timeoutMonitor, socket);
	}
}
