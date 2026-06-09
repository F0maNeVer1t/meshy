package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.conversation.ConversationRequest;
import ru.itis.meshy.api.sharing.InvitationResponse;
import ru.itis.meshy.api.sharing.Shareable;

public interface InvitationFactory<S extends Shareable, R extends InvitationResponse> {

	ConversationRequest<S> createInvitationRequest(boolean local, boolean sent,
			boolean seen, boolean read, InviteMessage<S> m, ContactId c,
			boolean available, boolean canBeOpened, long autoDeleteTimer);

	R createInvitationResponse(MessageId id, GroupId contactGroupId, long time,
			boolean local, boolean sent, boolean seen, boolean read,
			boolean accept, GroupId shareableId, long autoDeleteTimer,
			boolean isAutoDecline);

}
