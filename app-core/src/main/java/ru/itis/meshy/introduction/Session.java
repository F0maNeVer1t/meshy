package ru.itis.meshy.introduction;

import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.introduction.Role;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
abstract class Session<S extends State> {

	private final SessionId sessionId;
	private final S state;
	private final long requestTimestamp;

	Session(SessionId sessionId, S state, long requestTimestamp) {
		this.sessionId = sessionId;
		this.state = state;
		this.requestTimestamp = requestTimestamp;
	}

	abstract Role getRole();

	public SessionId getSessionId() {
		return sessionId;
	}

	S getState() {
		return state;
	}

	long getRequestTimestamp() {
		return requestTimestamp;
	}

}
