package ru.itis.meshy.introduction;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class RequestMessage extends AbstractIntroductionMessage {

	private final Author author;
	@Nullable
	private final String text;

	RequestMessage(MessageId messageId, GroupId groupId, long timestamp,
			@Nullable MessageId previousMessageId, Author author,
			@Nullable String text, long autoDeleteTimer) {
		super(messageId, groupId, timestamp, previousMessageId,
				autoDeleteTimer);
		this.author = author;
		this.text = text;
	}

	public Author getAuthor() {
		return author;
	}

	@Nullable
	public String getText() {
		return text;
	}

}
