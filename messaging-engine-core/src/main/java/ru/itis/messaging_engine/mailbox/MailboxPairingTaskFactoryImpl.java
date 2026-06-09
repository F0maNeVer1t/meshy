package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.event.EventExecutor;
import ru.itis.messaging_engine.api.mailbox.MailboxPairingTask;
import ru.itis.messaging_engine.api.mailbox.MailboxSettingsManager;
import ru.itis.messaging_engine.api.mailbox.MailboxUpdateManager;
import ru.itis.messaging_engine.api.qrcode.QrCodeClassifier;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class MailboxPairingTaskFactoryImpl implements MailboxPairingTaskFactory {

	private final Executor eventExecutor;
	private final DatabaseComponent db;
	private final CryptoComponent crypto;
	private final Clock clock;
	private final MailboxApi api;
	private final MailboxSettingsManager mailboxSettingsManager;
	private final MailboxUpdateManager mailboxUpdateManager;
	private final QrCodeClassifier qrCodeClassifier;

	@Inject
	MailboxPairingTaskFactoryImpl(
			@EventExecutor Executor eventExecutor,
			DatabaseComponent db,
			CryptoComponent crypto,
			Clock clock,
			MailboxApi api,
			MailboxSettingsManager mailboxSettingsManager,
			MailboxUpdateManager mailboxUpdateManager,
			QrCodeClassifier qrCodeClassifier) {
		this.eventExecutor = eventExecutor;
		this.db = db;
		this.crypto = crypto;
		this.clock = clock;
		this.api = api;
		this.mailboxSettingsManager = mailboxSettingsManager;
		this.mailboxUpdateManager = mailboxUpdateManager;
		this.qrCodeClassifier = qrCodeClassifier;
	}

	@Override
	public MailboxPairingTask createPairingTask(String qrCodePayload) {
		return new MailboxPairingTaskImpl(qrCodePayload, eventExecutor, db,
				crypto, clock, api, mailboxSettingsManager,
				mailboxUpdateManager, qrCodeClassifier);
	}
}
