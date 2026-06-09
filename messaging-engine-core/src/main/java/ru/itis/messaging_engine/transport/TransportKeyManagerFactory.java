package ru.itis.messaging_engine.transport;

import ru.itis.messaging_engine.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface TransportKeyManagerFactory {

	TransportKeyManager createTransportKeyManager(TransportId transportId,
			long maxLatency);

}
