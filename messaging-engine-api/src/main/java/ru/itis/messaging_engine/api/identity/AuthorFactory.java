package ru.itis.messaging_engine.api.identity;

import ru.itis.messaging_engine.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AuthorFactory {

	/**
	 * Creates an author with the current format version and the given name and
	 * public key.
	 */
	Author createAuthor(String name, PublicKey publicKey);

	/**
	 * Creates an author with the given format version, name and public key.
	 */
	Author createAuthor(int formatVersion, String name, PublicKey publicKey);

	/**
	 * Creates a local author with the current format version and the given
	 * name.
	 */
	LocalAuthor createLocalAuthor(String name);
}
