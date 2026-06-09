package ru.itis.meshy.api.forum.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.ConversationRequest;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.forum.Forum;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Forum>> {

	public ForumInvitationRequestReceivedEvent(ConversationRequest<Forum> request,
			ContactId contactId) {
		super(request, contactId);
	}

}
