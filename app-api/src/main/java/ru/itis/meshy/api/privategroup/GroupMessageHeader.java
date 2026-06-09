package ru.itis.meshy.api.privategroup;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.client.PostHeader;
import ru.itis.meshy.api.identity.AuthorInfo;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupMessageHeader extends PostHeader {

	private final GroupId groupId;

	public GroupMessageHeader(GroupId groupId, MessageId id,
			@Nullable MessageId parentId, long timestamp,
			Author author, AuthorInfo authorInfo, boolean read) {
		super(id, parentId, timestamp, author, authorInfo, read);
		this.groupId = groupId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

}
