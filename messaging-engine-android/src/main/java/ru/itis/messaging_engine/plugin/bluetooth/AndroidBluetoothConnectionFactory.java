package ru.itis.messaging_engine.plugin.bluetooth;

import android.bluetooth.BluetoothSocket;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.io.TimeoutMonitor;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
class AndroidBluetoothConnectionFactory
		implements BluetoothConnectionFactory<BluetoothSocket> {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;

	AndroidBluetoothConnectionFactory(
			BluetoothConnectionLimiter connectionLimiter,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor) {
		this.connectionLimiter = connectionLimiter;
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
	}

	@Override
	public DuplexTransportConnection wrapSocket(DuplexPlugin plugin,
			BluetoothSocket s) throws IOException {
		return new AndroidBluetoothTransportConnection(plugin,
				connectionLimiter, wakeLockManager, timeoutMonitor, s);
	}
}
