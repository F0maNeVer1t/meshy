package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.mailbox.MailboxFolderId;
import ru.itis.messaging_engine.api.mailbox.MailboxProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
interface MailboxWorkerFactory {

	MailboxWorker createUploadWorker(ConnectivityChecker connectivityChecker,
			MailboxProperties properties, MailboxFolderId folderId,
			ContactId contactId);

	MailboxWorker createDownloadWorkerForContactMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createDownloadWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties);

	MailboxWorker createContactListWorkerForOwnMailbox(
			ConnectivityChecker connectivityChecker,
			MailboxProperties properties);
}
