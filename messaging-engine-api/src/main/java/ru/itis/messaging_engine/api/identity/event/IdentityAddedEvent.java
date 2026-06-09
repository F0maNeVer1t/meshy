package ru.itis.messaging_engine.api.identity.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.identity.AuthorId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when an identity is added.
 */
@Immutable
@NotNullByDefault
public class IdentityAddedEvent extends Event {

	private final AuthorId authorId;

	public IdentityAddedEvent(AuthorId authorId) {
		this.authorId = authorId;
	}

	public AuthorId getAuthorId() {
		return authorId;
	}
}
