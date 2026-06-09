package ru.itis.meshy.api.blog.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.blog.Blog;
import ru.itis.meshy.api.conversation.ConversationRequest;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Blog>> {

	public BlogInvitationRequestReceivedEvent(ConversationRequest<Blog> request,
			ContactId contactId) {
		super(request, contactId);
	}

}
