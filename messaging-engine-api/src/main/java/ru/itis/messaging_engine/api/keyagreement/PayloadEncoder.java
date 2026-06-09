package ru.itis.messaging_engine.api.keyagreement;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PayloadEncoder {

	byte[] encode(Payload p);
}
