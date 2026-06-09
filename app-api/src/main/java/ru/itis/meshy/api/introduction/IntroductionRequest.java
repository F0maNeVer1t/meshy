package ru.itis.meshy.api.introduction;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationMessageVisitor;
import ru.itis.meshy.api.conversation.ConversationRequest;
import ru.itis.meshy.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionRequest extends ConversationRequest<Author> {

	private final AuthorInfo authorInfo;

	public IntroductionRequest(MessageId messageId, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, Author author, @Nullable String text,
			boolean answered, AuthorInfo authorInfo, long autoDeleteTimer) {
		super(messageId, groupId, time, local, read, sent, seen, sessionId,
				author, text, answered, autoDeleteTimer);
		this.authorInfo = authorInfo;
	}

	@Nullable
	public String getAlias() {
		return authorInfo.getAlias();
	}

	public boolean isContact() {
		return authorInfo.getStatus().isContact();
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitIntroductionRequest(this);
	}
}
