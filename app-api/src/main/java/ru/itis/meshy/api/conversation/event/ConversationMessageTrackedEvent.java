package ru.itis.meshy.api.conversation.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new conversation message is tracked.
 * Allows the UI to update the conversation's group count.
 */
@Immutable
@NotNullByDefault
public class ConversationMessageTrackedEvent extends Event {

	private final long timestamp;
	private final boolean read;
	private final ContactId contactId;

	public ConversationMessageTrackedEvent(long timestamp,
			boolean read, ContactId contactId) {
		this.timestamp = timestamp;
		this.read = read;
		this.contactId = contactId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean getRead() {
		return read;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
