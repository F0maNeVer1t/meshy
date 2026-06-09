package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class AcceptMessage extends DeletableSharingMessage {

	AcceptMessage(MessageId id, @Nullable MessageId previousMessageId,
			GroupId contactGroupId, GroupId shareableId, long timestamp,
			long autoDeleteTimer) {
		super(id, contactGroupId, shareableId, timestamp, previousMessageId,
				autoDeleteTimer);
	}

}
