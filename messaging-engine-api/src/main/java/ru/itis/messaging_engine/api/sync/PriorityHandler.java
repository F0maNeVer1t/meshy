package ru.itis.messaging_engine.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

/**
 * An interface for handling a {@link Priority} record received by an
 * incoming {@link SyncSession}.
 */
@NotNullByDefault
public interface PriorityHandler {

	void handle(Priority p);
}
