package ru.itis.messaging_engine.api.keyagreement.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.keyagreement.KeyAgreementResult;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a BQP protocol completes.
 */
@Immutable
@NotNullByDefault
public class KeyAgreementFinishedEvent extends Event {

	private final KeyAgreementResult result;

	public KeyAgreementFinishedEvent(KeyAgreementResult result) {
		this.result = result;
	}

	public KeyAgreementResult getResult() {
		return result;
	}
}
