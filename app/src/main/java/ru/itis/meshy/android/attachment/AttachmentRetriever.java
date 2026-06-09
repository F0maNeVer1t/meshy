package ru.itis.meshy.android.attachment;

import androidx.lifecycle.LiveData;

import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.attachment.Attachment;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import ru.itis.meshy.api.messaging.PrivateMessageHeader;
import ru.itis.meshy.api.messaging.event.AttachmentReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;
import java.util.List;


@NotNullByDefault
public interface AttachmentRetriever {

	@DatabaseExecutor
	Attachment getMessageAttachment(AttachmentHeader h) throws DbException;

	/**
	 * Returns a list of observable {@link LiveData}
	 * that get updated as the state of their {@link AttachmentItem}s changes.
	 */
	List<LiveData<AttachmentItem>> getAttachmentItems(
			PrivateMessageHeader messageHeader);

	/**
	 * Retrieves item size and adds the item to the cache, if available.
	 * <p>
	 * Use this to eagerly load the attachment size before it gets displayed.
	 * This is needed for messages containing a single attachment.
	 * Messages with more than one attachment use a standard size.
	 */
	@DatabaseExecutor
	void cacheAttachmentItemWithSize(MessageId conversationMessageId,
			AttachmentHeader h) throws DbException;

	/**
	 * Creates an {@link AttachmentItem} from the {@link Attachment}'s
	 * {@link InputStream} which will be closed when this method returns.
	 */
	AttachmentItem createAttachmentItem(Attachment a, boolean needsSize);

	/**
	 * Loads an {@link AttachmentItem}
	 * that arrived via an {@link AttachmentReceivedEvent}
	 * and notifies the associated {@link LiveData}.
	 * <p>
	 * Note that you need to call {@link #getAttachmentItems(PrivateMessageHeader)}
	 * first to get the LiveData.
	 * <p>
	 * It is possible that no LiveData is available,
	 * because the message of the AttachmentItem did not arrive, yet.
	 * In this case, the load wil be deferred until the message arrives.
	 */
	@DatabaseExecutor
	void loadAttachmentItem(MessageId attachmentId);

}
