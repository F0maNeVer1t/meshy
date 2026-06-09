package ru.itis.messaging_engine.api.plugin.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.plugin.Plugin.State;
import ru.itis.messaging_engine.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when the {@link State state} of a plugin changes.
 */
@Immutable
@NotNullByDefault
public class TransportStateEvent extends Event {

	private final TransportId transportId;
	private final State state;

	public TransportStateEvent(TransportId transportId, State state) {
		this.transportId = transportId;
		this.state = state;
	}

	public TransportId getTransportId() {
		return transportId;
	}

	public State getState() {
		return state;
	}
}
