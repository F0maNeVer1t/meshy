package ru.itis.messaging_engine.api.properties.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when {@link TransportProperties} are received
 * from a contact.
 */
@Immutable
@NotNullByDefault
public class RemoteTransportPropertiesUpdatedEvent extends Event {

	private final TransportId transportId;

	public RemoteTransportPropertiesUpdatedEvent(TransportId transportId) {
		this.transportId = transportId;
	}

	public TransportId getTransportId() {
		return transportId;
	}
}
