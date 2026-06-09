package ru.itis.messaging_engine.api.cleanup;

import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

/**
 * An interface for registering a hook with the {@link CleanupManager}
 * that will be called when a message's cleanup deadline is reached.
 */
@NotNullByDefault
public interface CleanupHook {

	/**
	 * Called when the cleanup deadlines of one or more messages are reached.
	 * <p>
	 * The callee is not required to delete the messages, but the hook won't be
	 * called again for these messages unless another cleanup timer is set (see
	 * {@link DatabaseComponent#setCleanupTimerDuration(Transaction, MessageId, long)}
	 * and {@link DatabaseComponent#startCleanupTimer(Transaction, MessageId)}).
	 */
	void deleteMessages(Transaction txn, GroupId g,
			Collection<MessageId> messageIds) throws DbException;
}
