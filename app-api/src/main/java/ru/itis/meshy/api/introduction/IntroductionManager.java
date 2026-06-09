package ru.itis.meshy.api.introduction;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.meshy.api.client.SessionId;
import ru.itis.meshy.api.conversation.ConversationManager.ConversationClient;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public interface IntroductionManager extends ConversationClient {

	/**
	 * The unique ID of the introduction client.
	 */
	ClientId CLIENT_ID = new ClientId("org.briarproject.briar.introduction");

	/**
	 * The current major version of the introduction client.
	 */
	int MAJOR_VERSION = 1;

	/**
	 * Returns true if both contacts can be introduced at this moment.
	 */
	boolean canIntroduce(Contact c1, Contact c2) throws DbException;

	/**
	 * Returns true if both contacts can be introduced at this moment.
	 */
	boolean canIntroduce(Transaction txn, Contact c1, Contact c2)
			throws DbException;

	/**
	 * The current minor version of the introduction client.
	 */
	int MINOR_VERSION = 1;

	/**
	 * Sends two initial introduction messages.
	 */
	void makeIntroduction(Contact c1, Contact c2, @Nullable String text)
			throws DbException;

	/**
	 * Sends two initial introduction messages.
	 */
	void makeIntroduction(Transaction txn, Contact c1, Contact c2,
			@Nullable String text) throws DbException;

	/**
	 * Responds to an introduction.
	 */
	void respondToIntroduction(ContactId contactId, SessionId sessionId,
			boolean accept) throws DbException;

	/**
	 * Responds to an introduction.
	 */
	void respondToIntroduction(Transaction txn, ContactId contactId,
			SessionId sessionId, boolean accept) throws DbException;

}
