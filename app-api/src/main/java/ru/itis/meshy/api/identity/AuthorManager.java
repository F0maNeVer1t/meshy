package ru.itis.meshy.api.identity;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.identity.AuthorId;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface AuthorManager {

	/**
	 * Returns the {@link AuthorInfo} for the given author.
	 */
	AuthorInfo getAuthorInfo(AuthorId a) throws DbException;

	/**
	 * Returns the {@link AuthorInfo} for the given author.
	 */
	AuthorInfo getAuthorInfo(Transaction txn, AuthorId a) throws DbException;

	/**
	 * Returns the {@link AuthorInfo} for the given contact.
	 */
	AuthorInfo getAuthorInfo(Contact c) throws DbException;

	/**
	 * Returns the {@link AuthorInfo} for the given contact.
	 */
	AuthorInfo getAuthorInfo(Transaction txn, Contact c)
			throws DbException;

	/**
	 * Returns the {@link AuthorInfo} for the {@link LocalAuthor}.
	 */
	AuthorInfo getMyAuthorInfo() throws DbException;

	/**
	 * Returns the {@link AuthorInfo} for the {@link LocalAuthor}.
	 */
	AuthorInfo getMyAuthorInfo(Transaction txn) throws DbException;
}
