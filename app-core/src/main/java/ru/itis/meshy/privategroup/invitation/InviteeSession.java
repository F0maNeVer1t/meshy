package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import static ru.itis.meshy.privategroup.invitation.InviteeState.START;
import static ru.itis.meshy.privategroup.invitation.Role.INVITEE;

@Immutable
@NotNullByDefault
class InviteeSession extends Session<InviteeState> {

	private final InviteeState state;

	InviteeSession(GroupId contactGroupId, GroupId privateGroupId,
			@Nullable MessageId lastLocalMessageId,
			@Nullable MessageId lastRemoteMessageId, long localTimestamp,
			long inviteTimestamp, InviteeState state) {
		super(contactGroupId, privateGroupId, lastLocalMessageId,
				lastRemoteMessageId, localTimestamp, inviteTimestamp);
		this.state = state;
	}

	InviteeSession(GroupId contactGroupId, GroupId privateGroupId) {
		this(contactGroupId, privateGroupId, null, null, 0, 0, START);
	}

	@Override
	Role getRole() {
		return INVITEE;
	}

	@Override
	InviteeState getState() {
		return state;
	}
}
