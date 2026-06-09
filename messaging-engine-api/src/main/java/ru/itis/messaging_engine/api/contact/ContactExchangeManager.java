package ru.itis.messaging_engine.api.contact;

import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.db.ContactExistsException;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface ContactExchangeManager {

	/**
	 * Exchanges contact information with a remote peer and adds the peer
	 * as a contact.
	 *
	 * @param alice Whether the local peer takes the role of Alice
	 * @return The newly added contact
	 * @throws ContactExistsException If the contact already exists
	 */
	Contact exchangeContacts(DuplexTransportConnection conn,
			SecretKey masterKey, boolean alice, boolean verified)
			throws IOException, DbException;

	/**
	 * Exchanges contact information with a remote peer and adds the peer
	 * as a contact, replacing the given pending contact.
	 *
	 * @param alice Whether the local peer takes the role of Alice
	 * @return The newly added contact
	 * @throws ContactExistsException If the contact already exists
	 */
	Contact exchangeContacts(PendingContactId p, DuplexTransportConnection conn,
			SecretKey masterKey, boolean alice, boolean verified)
			throws IOException, DbException;
}
