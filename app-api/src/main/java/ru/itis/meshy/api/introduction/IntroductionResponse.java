package ru.itis.meshy.api.introduction;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationMessageVisitor;
import ru.itis.meshy.api.conversation.ConversationResponse;
import ru.itis.meshy.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static ru.itis.meshy.api.introduction.Role.INTRODUCER;

@Immutable
@NotNullByDefault
public class IntroductionResponse extends ConversationResponse {

	private final Author introducedAuthor;
	private final AuthorInfo introducedAuthorInfo;
	private final Role ourRole;
	private final boolean canSucceed;

	public IntroductionResponse(MessageId messageId, GroupId groupId, long time,
			boolean local, boolean read, boolean sent, boolean seen,
			SessionId sessionId, boolean accepted, Author author,
			AuthorInfo introducedAuthorInfo, Role role, boolean canSucceed,
			long autoDeleteTimer, boolean isAutoDecline) {
		super(messageId, groupId, time, local, read, sent, seen, sessionId,
				accepted, autoDeleteTimer, isAutoDecline);
		this.introducedAuthor = author;
		this.introducedAuthorInfo = introducedAuthorInfo;
		this.ourRole = role;
		this.canSucceed = canSucceed;
	}

	public Author getIntroducedAuthor() {
		return introducedAuthor;
	}

	public AuthorInfo getIntroducedAuthorInfo() {
		return introducedAuthorInfo;
	}

	public boolean canSucceed() {
		return canSucceed;
	}

	public boolean isIntroducer() {
		return ourRole == INTRODUCER;
	}

	@Override
	public <T> T accept(ConversationMessageVisitor<T> v) {
		return v.visitIntroductionResponse(this);
	}

}
