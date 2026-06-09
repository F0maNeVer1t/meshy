package ru.itis.messaging_engine.api.cleanup;

import ru.itis.messaging_engine.api.cleanup.event.CleanupTimerStartedEvent;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * The CleanupManager is responsible for tracking the cleanup deadlines of
 * messages and passing them to their respective
 * {@link CleanupHook CleanupHooks} when the deadlines are reached.
 * <p>
 * The CleanupManager responds to
 * {@link CleanupTimerStartedEvent CleanupTimerStartedEvents} broadcast by the
 * {@link DatabaseComponent}.
 * <p>
 * See {@link DatabaseComponent#setCleanupTimerDuration(Transaction, MessageId, long)},
 * {@link DatabaseComponent#startCleanupTimer(Transaction, MessageId)},
 * {@link DatabaseComponent#stopCleanupTimer(Transaction, MessageId)}.
 */
@NotNullByDefault
public interface CleanupManager {

	/**
	 * When scheduling a cleanup task we overshoot the deadline by this many
	 * milliseconds to reduce the number of tasks that need to be scheduled
	 * when messages have cleanup deadlines that are close together.
	 */
	long BATCH_DELAY_MS = 1000;

	/**
	 * Registers a hook to be called when messages are due for cleanup.
	 * This method should be called before
	 * {@link LifecycleManager#startServices(SecretKey)}.
	 */
	void registerCleanupHook(ClientId c, int majorVersion,
			CleanupHook hook);
}
