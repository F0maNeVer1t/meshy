package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.mailbox.MailboxManager;
import ru.itis.messaging_engine.api.mailbox.MailboxPairingTask;
import ru.itis.messaging_engine.api.mailbox.MailboxProperties;
import ru.itis.messaging_engine.api.mailbox.MailboxSettingsManager;
import ru.itis.messaging_engine.api.mailbox.MailboxStatus;
import ru.itis.messaging_engine.api.mailbox.MailboxVersion;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.util.Base32;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.messaging_engine.util.StringUtils.ISO_8859_1;

@Immutable
@NotNullByDefault
class MailboxManagerImpl implements MailboxManager {

	private static final String TAG = MailboxManagerImpl.class.getName();
	private final static Logger LOG = getLogger(TAG);

	private final Executor ioExecutor;
	private final MailboxApi api;
	private final TransactionManager db;
	private final MailboxSettingsManager mailboxSettingsManager;
	private final MailboxPairingTaskFactory pairingTaskFactory;
	private final Clock clock;
	private final Object lock = new Object();

	@Nullable
	@GuardedBy("lock")
	private MailboxPairingTask pairingTask = null;

	@Inject
	MailboxManagerImpl(
			@IoExecutor Executor ioExecutor,
			MailboxApi api,
			TransactionManager db,
			MailboxSettingsManager mailboxSettingsManager,
			MailboxPairingTaskFactory pairingTaskFactory,
			Clock clock) {
		this.ioExecutor = ioExecutor;
		this.api = api;
		this.db = db;
		this.mailboxSettingsManager = mailboxSettingsManager;
		this.pairingTaskFactory = pairingTaskFactory;
		this.clock = clock;
	}

	@Override
	public boolean isPaired(Transaction txn) throws DbException {
		return mailboxSettingsManager.getOwnMailboxProperties(txn) != null;
	}

	@Override
	public MailboxStatus getMailboxStatus(Transaction txn) throws DbException {
		return mailboxSettingsManager.getOwnMailboxStatus(txn);
	}

	@Nullable
	@Override
	public MailboxPairingTask getCurrentPairingTask() {
		synchronized (lock) {
			return pairingTask;
		}
	}

	@Override
	public MailboxPairingTask startPairingTask(String payload) {
		MailboxPairingTask created;
		synchronized (lock) {
			if (pairingTask != null) return pairingTask;
			created = pairingTaskFactory.createPairingTask(payload);
			pairingTask = created;
		}
		ioExecutor.execute(() -> {
			created.run();
			synchronized (lock) {
				// remove task after it finished
				pairingTask = null;
			}
		});
		return created;
	}

	@Override
	public String convertBase32Payload(String base32Payload)
			throws FormatException {
		Pattern regex = Pattern.compile("(briar-mailbox://)?([a-z2-7]{104})");
		Matcher matcher = regex.matcher(base32Payload);
		if (!matcher.find()) throw new FormatException();
		String base32 = matcher.group(2);
		byte[] payloadBytes;
		try {
			payloadBytes = Base32.decode(base32, false);
		} catch (IllegalArgumentException e) {
			throw new FormatException();
		}
		return new String(payloadBytes, ISO_8859_1);
	}

	@Override
	public boolean checkConnection() {
		List<MailboxVersion> versions = null;
		try {
			MailboxProperties props = db.transactionWithNullableResult(true,
					mailboxSettingsManager::getOwnMailboxProperties);
			if (props == null) throw new DbException();
			versions = api.getServerSupports(props);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			// we don't treat this is a failure to record
			return false;
		} catch (IOException | MailboxApi.ApiException e) {
			// we record this as a failure
			logException(LOG, WARNING, e);
		}
		try {
			recordCheckResult(versions);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
		}
		return versions != null;
	}

	private void recordCheckResult(@Nullable List<MailboxVersion> versions)
			throws DbException {
		long now = clock.currentTimeMillis();
		db.transaction(false, txn -> {
			if (versions != null) {
				mailboxSettingsManager
						.recordSuccessfulConnection(txn, now, versions);
			} else {
				mailboxSettingsManager.recordFailedConnectionAttempt(txn, now);
			}
		});
	}

	@Override
	public boolean unPair() throws DbException {
		MailboxProperties properties = db.transactionWithNullableResult(true,
				mailboxSettingsManager::getOwnMailboxProperties);
		if (properties == null) {
			// no more mailbox, that's strange but possible if called in quick
			// succession, so let's return true this time
			return true;
		}
		boolean wasWiped;
		try {
			api.wipeMailbox(properties);
			wasWiped = true;
		} catch (IOException | MailboxApi.ApiException e) {
			logException(LOG, WARNING, e);
			wasWiped = false;
		}
		db.transaction(false,
				mailboxSettingsManager::removeOwnMailboxProperties);
		return wasWiped;
	}
}
