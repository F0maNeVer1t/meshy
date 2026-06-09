package ru.itis.meshy.api.privategroup.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a private group is dissolved by a remote
 * creator.
 */
@Immutable
@NotNullByDefault
public class GroupDissolvedEvent extends Event {

	private final GroupId groupId;

	public GroupDissolvedEvent(GroupId groupId) {
		this.groupId = groupId;
	}

	public GroupId getGroupId() {
		return groupId;
	}

}
