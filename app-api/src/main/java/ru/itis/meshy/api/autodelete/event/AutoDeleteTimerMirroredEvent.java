package ru.itis.meshy.api.autodelete.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class AutoDeleteTimerMirroredEvent extends Event {

	private final ContactId contactId;
	private final long newTimer;

	public AutoDeleteTimerMirroredEvent(ContactId contactId, long newTimer) {
		this.contactId = contactId;
		this.newTimer = newTimer;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public long getNewTimer() {
		return newTimer;
	}
}
