package ru.itis.messaging_engine.api.plugin.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ConnectionOpenedEvent extends Event {

	private final ContactId contactId;
	private final TransportId transportId;
	private final boolean incoming;

	public ConnectionOpenedEvent(ContactId contactId, TransportId transportId,
			boolean incoming) {
		this.contactId = contactId;
		this.transportId = transportId;
		this.incoming = incoming;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public TransportId getTransportId() {
		return transportId;
	}

	public boolean isIncoming() {
		return incoming;
	}
}
