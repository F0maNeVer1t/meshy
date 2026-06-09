package ru.itis.meshy.api.forum;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.ThreadedMessage;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumPost extends ThreadedMessage {

	public ForumPost(Message message, @Nullable MessageId parent,
			Author author) {
		super(message, parent, author);
	}

}
