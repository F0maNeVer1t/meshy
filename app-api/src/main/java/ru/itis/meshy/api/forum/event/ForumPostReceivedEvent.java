package ru.itis.meshy.api.forum.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.forum.ForumPostHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new forum post is received.
 */
@Immutable
@NotNullByDefault
public class ForumPostReceivedEvent extends Event {

	private final GroupId groupId;
	private final ForumPostHeader header;
	private final String text;

	public ForumPostReceivedEvent(GroupId groupId, ForumPostHeader header,
			String text) {
		this.groupId = groupId;
		this.header = header;
		this.text = text;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public ForumPostHeader getHeader() {
		return header;
	}

	public String getText() {
		return text;
	}
}
