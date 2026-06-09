package ru.itis.meshy.android.attachment;

import static ru.itis.messaging_engine.util.AndroidUtils.getSupportedImageContentTypes;
import static ru.itis.messaging_engine.util.IoUtils.tryToClose;
import static ru.itis.messaging_engine.util.LogUtils.logDuration;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.messaging_engine.util.LogUtils.now;
import static ru.itis.meshy.android.attachment.media.ImageCompressor.MIME_TYPE;
import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.Nullable;

import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.android.attachment.media.ImageCompressor;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import ru.itis.meshy.api.messaging.MessagingManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;

@NotNullByDefault
class AttachmentCreationTask {

	private static final Logger LOG =
			getLogger(AttachmentCreationTask.class.getName());

	private final MessagingManager messagingManager;
	private final ContentResolver contentResolver;
	private final ImageCompressor imageCompressor;
	private final GroupId groupId;
	private final Collection<Uri> uris;
	private final boolean needsSize;
	@Nullable
	private volatile AttachmentCreator attachmentCreator;

	private volatile boolean canceled = false;

	AttachmentCreationTask(MessagingManager messagingManager,
			ContentResolver contentResolver,
			AttachmentCreator attachmentCreator,
			ImageCompressor imageCompressor,
			GroupId groupId, Collection<Uri> uris, boolean needsSize) {
		this.messagingManager = messagingManager;
		this.contentResolver = contentResolver;
		this.imageCompressor = imageCompressor;
		this.groupId = groupId;
		this.uris = uris;
		this.needsSize = needsSize;
		this.attachmentCreator = attachmentCreator;
	}

	void cancel() {
		canceled = true;
		attachmentCreator = null;
	}

	@IoExecutor
	void storeAttachments() {
		for (Uri uri : uris) processUri(uri);
		AttachmentCreator attachmentCreator = this.attachmentCreator;
		if (!canceled && attachmentCreator != null)
			attachmentCreator.onAttachmentCreationFinished();
		this.attachmentCreator = null;
	}

	@IoExecutor
	private void processUri(Uri uri) {
		if (canceled) return;
		try {
			AttachmentHeader h = storeAttachment(uri);
			AttachmentCreator attachmentCreator = this.attachmentCreator;
			if (attachmentCreator != null) {
				attachmentCreator.onAttachmentHeaderReceived(uri, h, needsSize);
			}
		} catch (DbException | IOException e) {
			logException(LOG, WARNING, e);
			AttachmentCreator attachmentCreator = this.attachmentCreator;
			if (attachmentCreator != null) {
				attachmentCreator.onAttachmentError(uri, e);
			}
			canceled = true;
		}
	}

	@IoExecutor
	private AttachmentHeader storeAttachment(Uri uri)
			throws IOException, DbException {
		long start = now();
		String contentType = contentResolver.getType(uri);
		if (contentType == null) throw new IOException("null content type");
		if (!asList(getSupportedImageContentTypes()).contains(contentType)) {
			throw new UnsupportedMimeTypeException(contentType, uri);
		}
		InputStream is;
		try {
			is = contentResolver.openInputStream(uri);
			if (is == null) throw new IOException();
		} catch (SecurityException e) {
			throw new IOException(e);
		}
		is = imageCompressor.compressImage(is, contentType);
		long timestamp = System.currentTimeMillis();
		AttachmentHeader h = messagingManager.addLocalAttachment(groupId,
				timestamp, MIME_TYPE, is);
		tryToClose(is, LOG, WARNING);
		logDuration(LOG, "Storing attachment", start);
		return h;
	}

}
