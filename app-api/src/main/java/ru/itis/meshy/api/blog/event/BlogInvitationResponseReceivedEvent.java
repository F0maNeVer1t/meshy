package ru.itis.meshy.api.blog.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.blog.BlogInvitationResponse;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogInvitationResponseReceivedEvent
		extends ConversationMessageReceivedEvent<BlogInvitationResponse> {

	public BlogInvitationResponseReceivedEvent(BlogInvitationResponse response,
			ContactId contactId) {
		super(response, contactId);
	}

}
