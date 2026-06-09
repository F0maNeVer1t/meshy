package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.sync.OutgoingSessionRecord;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface MailboxFileManager {

	/**
	 * Creates an empty file for storing a download.
	 */
	File createTempFileForDownload() throws IOException;

	/**
	 * Creates a file to be uploaded to the given contact and writes any
	 * waiting data to the file. The IDs of any messages sent or acked will
	 * be added to the given {@link OutgoingSessionRecord}.
	 */
	File createAndWriteTempFileForUpload(ContactId contactId,
			OutgoingSessionRecord sessionRecord) throws IOException;

	/**
	 * Handles a file that has been downloaded. The file should be created
	 * with {@link #createTempFileForDownload()}.
	 */
	void handleDownloadedFile(File f);
}
