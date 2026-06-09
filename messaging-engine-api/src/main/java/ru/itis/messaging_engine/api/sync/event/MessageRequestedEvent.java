package ru.itis.messaging_engine.api.sync.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a message is requested by a contact.
 */
@Immutable
@NotNullByDefault
public class MessageRequestedEvent extends Event {

	private final ContactId contactId;

	public MessageRequestedEvent(ContactId contactId) {
		this.contactId = contactId;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
