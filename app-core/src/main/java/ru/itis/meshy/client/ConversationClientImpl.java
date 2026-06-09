package ru.itis.meshy.client;

import ru.itis.messaging_engine.api.client.BdfIncomingMessageHook;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.data.MetadataParser;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.client.MessageTracker;
import ru.itis.meshy.api.client.MessageTracker.GroupCount;
import ru.itis.meshy.api.conversation.ConversationManager.ConversationClient;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class ConversationClientImpl extends BdfIncomingMessageHook
		implements ConversationClient {

	protected final MessageTracker messageTracker;

	protected ConversationClientImpl(DatabaseComponent db,
			ClientHelper clientHelper, MetadataParser metadataParser,
			MessageTracker messageTracker) {
		super(db, clientHelper, metadataParser);
		this.messageTracker = messageTracker;
	}

	@Override
	public GroupCount getGroupCount(Transaction txn, ContactId contactId)
			throws DbException {
		Contact contact = db.getContact(txn, contactId);
		GroupId groupId = getContactGroup(contact).getId();
		return messageTracker.getGroupCount(txn, groupId);
	}

}
