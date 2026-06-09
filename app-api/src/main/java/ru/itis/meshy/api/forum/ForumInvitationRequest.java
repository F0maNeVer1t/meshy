package ru.itis.meshy.api.forum;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationMessageVisitor;
import ru.itis.meshy.api.sharing.InvitationRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumInvitationRequest extends InvitationRequest<Forum> {

	public ForumInvitationRequest(MessageId id, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, Forum forum, @Nullable String text,
			boolean available, boolean canBeOpened, long autoDeleteTimer) {
		super(id, groupId, time, local, read, sent, seen, sessionId, forum,
				text, available, canBeOpened, autoDeleteTimer);
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitForumInvitationRequest(this);
	}
}
