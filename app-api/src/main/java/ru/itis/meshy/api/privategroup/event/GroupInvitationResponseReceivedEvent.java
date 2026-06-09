package ru.itis.meshy.api.privategroup.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.meshy.api.conversation.event.ConversationMessageReceivedEvent;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupInvitationResponseReceivedEvent
		extends ConversationMessageReceivedEvent<GroupInvitationResponse> {

	public GroupInvitationResponseReceivedEvent(
			GroupInvitationResponse response, ContactId contactId) {
		super(response, contactId);
	}
}
