package ru.itis.messaging_engine.contact;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.UnsupportedVersionException;
import ru.itis.messaging_engine.api.contact.PendingContact;
import ru.itis.messaging_engine.api.crypto.PublicKey;

interface PendingContactFactory {

	/**
	 * Creates a {@link PendingContact} from the given handshake link and alias.
	 *
	 * @throws UnsupportedVersionException If the link uses a format version
	 * that is not supported
	 * @throws FormatException If the link is invalid
	 */
	PendingContact createPendingContact(String link, String alias)
			throws FormatException;

	/**
	 * Creates a handshake link from the given public key.
	 */
	String createHandshakeLink(PublicKey k);
}
