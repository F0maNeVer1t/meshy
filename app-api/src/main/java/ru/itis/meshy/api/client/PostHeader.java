package ru.itis.meshy.api.client;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.identity.AuthorInfo.Status;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class PostHeader {

	private final MessageId id;
	@Nullable
	private final MessageId parentId;
	private final long timestamp;
	private final Author author;
	private final AuthorInfo authorInfo;
	private final boolean read;

	public PostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, AuthorInfo authorInfo, boolean read) {
		this.id = id;
		this.parentId = parentId;
		this.timestamp = timestamp;
		this.author = author;
		this.authorInfo = authorInfo;
		this.read = read;
	}

	public MessageId getId() {
		return id;
	}

	public Author getAuthor() {
		return author;
	}

	public Status getAuthorStatus() {
		return authorInfo.getStatus();
	}

	public AuthorInfo getAuthorInfo() {
		return authorInfo;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isRead() {
		return read;
	}

	@Nullable
	public MessageId getParentId() {
		return parentId;
	}
}
