package ru.itis.meshy.api.autodelete.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when one or more messages
 * in the private conversation with a contact have been deleted.
 */
@Immutable
@NotNullByDefault
public class ConversationMessagesDeletedEvent extends Event {

	private final ContactId contactId;
	private final Collection<MessageId> messageIds;

	public ConversationMessagesDeletedEvent(ContactId contactId,
			Collection<MessageId> messageIds) {
		this.contactId = contactId;
		this.messageIds = messageIds;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public Collection<MessageId> getMessageIds() {
		return messageIds;
	}
}
