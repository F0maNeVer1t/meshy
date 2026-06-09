package ru.itis.meshy.api.forum.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.forum.ForumInvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumInvitationResponseReceivedEvent extends
		ConversationMessageReceivedEvent<ForumInvitationResponse> {

	public ForumInvitationResponseReceivedEvent(
			ForumInvitationResponse response, ContactId contactId) {
		super(response, contactId);
	}

}
