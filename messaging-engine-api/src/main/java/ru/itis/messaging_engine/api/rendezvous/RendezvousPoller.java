package ru.itis.messaging_engine.api.rendezvous;

import ru.itis.messaging_engine.api.contact.PendingContactId;

/**
 * Interface for the poller that makes rendezvous connections to pending
 * contacts.
 */
public interface RendezvousPoller {

	long getLastPollTime(PendingContactId p);
}
