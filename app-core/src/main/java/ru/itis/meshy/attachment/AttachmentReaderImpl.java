package ru.itis.meshy.attachment;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.NoSuchMessageException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.attachment.Attachment;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import ru.itis.meshy.api.attachment.AttachmentReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.inject.Inject;

import static ru.itis.meshy.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static ru.itis.meshy.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;

public class AttachmentReaderImpl implements AttachmentReader {

	private final TransactionManager db;
	private final ClientHelper clientHelper;

	@Inject
	public AttachmentReaderImpl(TransactionManager db,
			ClientHelper clientHelper) {
		this.db = db;
		this.clientHelper = clientHelper;
	}

	@Override
	public Attachment getAttachment(AttachmentHeader h) throws DbException {
		return db.transactionWithResult(true, txn -> getAttachment(txn, h));
	}

	@Override
	public Attachment getAttachment(Transaction txn, AttachmentHeader h)
			throws DbException {
		// TODO: Support large messages
		MessageId m = h.getMessageId();
		Message message = clientHelper.getMessage(txn, m);
		// Check that the message is in the expected group, to prevent it from
		// being loaded in the context of a different group
		if (!message.getGroupId().equals(h.getGroupId())) {
			throw new NoSuchMessageException();
		}
		byte[] body = message.getBody();
		try {
			BdfDictionary meta =
					clientHelper.getMessageMetadataAsDictionary(txn, m);
			String contentType = meta.getString(MSG_KEY_CONTENT_TYPE);
			if (!contentType.equals(h.getContentType()))
				throw new NoSuchMessageException();
			int offset = meta.getInt(MSG_KEY_DESCRIPTOR_LENGTH);
			InputStream stream = new ByteArrayInputStream(body, offset,
					body.length - offset);
			return new Attachment(h, stream);
		} catch (FormatException e) {
			throw new NoSuchMessageException();
		}
	}

}
