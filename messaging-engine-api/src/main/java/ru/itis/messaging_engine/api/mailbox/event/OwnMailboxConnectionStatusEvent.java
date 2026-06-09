package ru.itis.messaging_engine.api.mailbox.event;

import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.mailbox.MailboxSettingsManager;
import ru.itis.messaging_engine.api.mailbox.MailboxStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast by {@link MailboxSettingsManager} when
 * recording the connection status of own Mailbox.
 */
@Immutable
@NotNullByDefault
public class OwnMailboxConnectionStatusEvent extends Event {

	private final MailboxStatus status;

	public OwnMailboxConnectionStatusEvent(MailboxStatus status) {
		this.status = status;
	}

	public MailboxStatus getStatus() {
		return status;
	}
}
