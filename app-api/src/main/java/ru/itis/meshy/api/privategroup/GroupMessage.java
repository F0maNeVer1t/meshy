package ru.itis.meshy.api.privategroup;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.ThreadedMessage;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupMessage extends ThreadedMessage {

	public GroupMessage(Message message, @Nullable MessageId parent,
			Author member) {
		super(message, parent, member);
	}

	public Author getMember() {
		return super.getAuthor();
	}

}
