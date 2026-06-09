package ru.itis.meshy.api.sharing;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationResponse;

public abstract class InvitationResponse extends ConversationResponse {

	private final GroupId shareableId;

	public InvitationResponse(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accepted, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(id, groupId, time, local, read, sent, seen, sessionId, accepted,
				autoDeleteTimer, isAutoDecline);
		this.shareableId = shareableId;
	}

	public GroupId getShareableId() {
		return shareableId;
	}

}
