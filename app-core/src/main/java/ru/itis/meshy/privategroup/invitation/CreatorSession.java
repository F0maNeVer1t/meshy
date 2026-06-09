package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static ru.itis.meshy.privategroup.invitation.CreatorState.START;
import static ru.itis.meshy.privategroup.invitation.Role.CREATOR;

@Immutable
@NotNullByDefault
class CreatorSession extends Session<CreatorState> {

	private final CreatorState state;

	CreatorSession(GroupId contactGroupId, GroupId privateGroupId,
			@Nullable MessageId lastLocalMessageId,
			@Nullable MessageId lastRemoteMessageId, long localTimestamp,
			long inviteTimestamp, CreatorState state) {
		super(contactGroupId, privateGroupId, lastLocalMessageId,
				lastRemoteMessageId, localTimestamp, inviteTimestamp);
		this.state = state;
	}

	CreatorSession(GroupId contactGroupId, GroupId privateGroupId) {
		this(contactGroupId, privateGroupId, null, null, 0, 0, START);
	}

	@Override
	Role getRole() {
		return CREATOR;
	}

	@Override
	CreatorState getState() {
		return state;
	}
}
