package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class AbortMessage extends GroupInvitationMessage {

	AbortMessage(MessageId id, GroupId contactGroupId, GroupId privateGroupId,
			long timestamp) {
		super(id, contactGroupId, privateGroupId, timestamp);
	}
}
