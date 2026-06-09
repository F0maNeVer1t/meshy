package ru.itis.meshy.api.conversation.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.meshy.api.conversation.ConversationMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new conversation message is received.
 */
@Immutable
@NotNullByDefault
public abstract class ConversationMessageReceivedEvent<H extends ConversationMessageHeader>
		extends Event {

	private final H messageHeader;
	private final ContactId contactId;

	public ConversationMessageReceivedEvent(H messageHeader,
			ContactId contactId) {
		this.messageHeader = messageHeader;
		this.contactId = contactId;
	}

	public H getMessageHeader() {
		return messageHeader;
	}

	public ContactId getContactId() {
		return contactId;
	}
}
