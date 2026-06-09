package ru.itis.messaging_engine.api.contact;

import ru.itis.messaging_engine.api.UniqueId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Type-safe wrapper for a byte array that uniquely identifies a
 * {@link PendingContact}.
 */
@ThreadSafe
@NotNullByDefault
public class PendingContactId extends UniqueId {

	public PendingContactId(byte[] id) {
		super(id);
	}
}
