package ru.itis.messaging_engine.plugin.file;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveManager;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveTask;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.transport.KeyManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.plugin.file.RemovableDriveConstants.ID;
import static ru.itis.messaging_engine.api.plugin.file.RemovableDriveConstants.PROP_SUPPORTED;
import static ru.itis.messaging_engine.plugin.file.RemovableDrivePluginFactory.MAX_LATENCY;

@ThreadSafe
@NotNullByDefault
class RemovableDriveManagerImpl
		implements RemovableDriveManager, RemovableDriveTaskRegistry {

	private final Executor ioExecutor;
	private final DatabaseComponent db;
	private final KeyManager keyManager;
	private final TransportPropertyManager transportPropertyManager;
	private final RemovableDriveTaskFactory taskFactory;
	private final Object lock = new Object();

	@GuardedBy("lock")
	@Nullable
	private RemovableDriveTask reader = null;
	@GuardedBy("lock")
	@Nullable
	private RemovableDriveTask writer = null;

	@Inject
	RemovableDriveManagerImpl(
			@IoExecutor Executor ioExecutor,
			DatabaseComponent db,
			KeyManager keyManager,
			TransportPropertyManager transportPropertyManager,
			RemovableDriveTaskFactory taskFactory) {
		this.ioExecutor = ioExecutor;
		this.db = db;
		this.keyManager = keyManager;
		this.transportPropertyManager = transportPropertyManager;
		this.taskFactory = taskFactory;
	}

	@Nullable
	@Override
	public RemovableDriveTask getCurrentReaderTask() {
		synchronized (lock) {
			return reader;
		}
	}

	@Nullable
	@Override
	public RemovableDriveTask getCurrentWriterTask() {
		synchronized (lock) {
			return writer;
		}
	}

	@Override
	public RemovableDriveTask startReaderTask(TransportProperties p) {
		RemovableDriveTask created;
		synchronized (lock) {
			if (reader != null) return reader;
			reader = created = taskFactory.createReader(this, p);
		}
		ioExecutor.execute(created);
		return created;
	}

	@Override
	public RemovableDriveTask startWriterTask(ContactId c,
			TransportProperties p) {
		RemovableDriveTask created;
		synchronized (lock) {
			if (writer != null) return writer;
			writer = created = taskFactory.createWriter(this, c, p);
		}
		ioExecutor.execute(created);
		return created;
	}

	@Override
	public boolean isTransportSupportedByContact(ContactId c)
			throws DbException {
		if (!keyManager.canSendOutgoingStreams(c, ID)) return false;
		TransportProperties p =
				transportPropertyManager.getRemoteProperties(c, ID);
		return "true".equals(p.get(PROP_SUPPORTED));
	}

	@Override
	public boolean isWriterTaskNeeded(ContactId c) throws DbException {
		return db.transactionWithResult(true, txn ->
				db.containsAcksToSend(txn, c) ||
						db.containsMessagesToSend(txn, c, MAX_LATENCY, true));
	}

	@Override
	public void removeReader(RemovableDriveTask task) {
		synchronized (lock) {
			if (reader == task) reader = null;
		}
	}

	@Override
	public void removeWriter(RemovableDriveTask task) {
		synchronized (lock) {
			if (writer == task) writer = null;
		}
	}
}
