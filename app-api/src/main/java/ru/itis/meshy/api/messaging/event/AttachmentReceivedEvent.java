package ru.itis.meshy.api.messaging.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new attachment is received.
 */
@Immutable
@NotNullByDefault
public class AttachmentReceivedEvent extends Event {

	private final MessageId messageId;
	private final ContactId contactId;

	public AttachmentReceivedEvent(MessageId messageId, ContactId contactId) {
		this.messageId = messageId;
		this.contactId = contactId;
	}

	public MessageId getMessageId() {
		return messageId;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
