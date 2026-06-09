package ru.itis.meshy.api.messaging.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.messaging.PrivateMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new private message is received.
 */
@Immutable
@NotNullByDefault
public class PrivateMessageReceivedEvent
		extends ConversationMessageReceivedEvent<PrivateMessageHeader> {

	public PrivateMessageReceivedEvent(PrivateMessageHeader messageHeader,
			ContactId contactId) {
		super(messageHeader, contactId);
	}

}
