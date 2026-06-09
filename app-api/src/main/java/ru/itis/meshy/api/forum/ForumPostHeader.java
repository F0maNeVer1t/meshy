package ru.itis.meshy.api.forum;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.PostHeader;
import ru.itis.meshy.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumPostHeader extends PostHeader {

	public ForumPostHeader(MessageId id, @Nullable MessageId parentId,
			long timestamp, Author author, AuthorInfo authorInfo,
			boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
	}

}
