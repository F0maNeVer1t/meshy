package ru.itis.messaging_engine.plugin.bluetooth;

import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
interface BluetoothConnectionFactory<S> {

	DuplexTransportConnection wrapSocket(DuplexPlugin plugin, S socket)
			throws IOException;
}
