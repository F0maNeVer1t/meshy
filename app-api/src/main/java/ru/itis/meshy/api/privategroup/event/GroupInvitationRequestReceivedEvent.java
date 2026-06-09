package ru.itis.meshy.api.privategroup.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<GroupInvitationRequest> {

	public GroupInvitationRequestReceivedEvent(GroupInvitationRequest request,
			ContactId contactId) {
		super(request, contactId);
	}

}
