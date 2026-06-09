package ru.itis.messaging_engine.plugin.bluetooth;

import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public interface BluetoothPlugin extends DuplexPlugin {

	boolean isDiscovering();

	void disablePolling();

	void enablePolling();

	@Nullable
	DuplexTransportConnection discoverAndConnectForSetup(String uuid);

	void stopDiscoverAndConnect();
}
