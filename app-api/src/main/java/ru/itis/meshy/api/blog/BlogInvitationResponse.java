package ru.itis.meshy.api.blog;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationMessageVisitor;
import ru.itis.meshy.api.sharing.InvitationResponse;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public class BlogInvitationResponse extends InvitationResponse {

	public BlogInvitationResponse(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accept, GroupId shareableId,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(id, groupId, time, local, read, sent, seen, sessionId,
				accept, shareableId, autoDeleteTimer, isAutoDecline);
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitBlogInvitationResponse(this);
	}
}
