package ru.itis.messaging_engine.api.keyagreement.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.keyagreement.Payload;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a BQP task is listening.
 */
@Immutable
@NotNullByDefault
public class KeyAgreementListeningEvent extends Event {

	private final Payload localPayload;

	public KeyAgreementListeningEvent(Payload localPayload) {
		this.localPayload = localPayload;
	}

	public Payload getLocalPayload() {
		return localPayload;
	}
}
