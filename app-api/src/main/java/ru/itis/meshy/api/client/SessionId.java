package ru.itis.meshy.api.client;

import ru.itis.messaging_engine.api.UniqueId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a protocol
 * session.
 */
@ThreadSafe
@NotNullByDefault
public class SessionId extends UniqueId {

	public SessionId(byte[] id) {
		super(id);
	}
}
