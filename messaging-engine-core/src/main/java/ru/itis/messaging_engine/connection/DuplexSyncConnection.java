package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.connection.InterruptibleConnection;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.plugin.TransportConnectionReader;
import ru.itis.messaging_engine.api.plugin.TransportConnectionWriter;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.sync.Priority;
import ru.itis.messaging_engine.api.sync.SyncSession;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriter;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
abstract class DuplexSyncConnection extends SyncConnection
		implements InterruptibleConnection {

	final Executor ioExecutor;
	final TransportId transportId;
	final TransportConnectionReader reader;
	final TransportConnectionWriter writer;
	final TransportProperties remote;

	private final Object interruptLock = new Object();

	@GuardedBy("interruptLock")
	@Nullable
	private SyncSession outgoingSession = null;
	@GuardedBy("interruptLock")
	private boolean interruptWaiting = false;

	@Override
	public void interruptOutgoingSession() {
		SyncSession out = null;
		synchronized (interruptLock) {
			if (outgoingSession == null) interruptWaiting = true;
			else out = outgoingSession;
		}
		if (out != null) out.interrupt();
	}

	void setOutgoingSession(SyncSession outgoingSession) {
		boolean interruptWasWaiting = false;
		synchronized (interruptLock) {
			this.outgoingSession = outgoingSession;
			if (interruptWaiting) {
				interruptWasWaiting = true;
				interruptWaiting = false;
			}
		}
		if (interruptWasWaiting) outgoingSession.interrupt();
	}

	DuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, TransportId transportId,
			DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager);
		this.ioExecutor = ioExecutor;
		this.transportId = transportId;
		reader = connection.getReader();
		writer = connection.getWriter();
		remote = connection.getRemoteProperties();
	}

	void onReadError(boolean recognised) {
		disposeOnError(reader, recognised);
		disposeOnError(writer);
		interruptOutgoingSession();
	}

	void onWriteError() {
		disposeOnError(reader, true);
		disposeOnError(writer);
	}

	SyncSession createDuplexOutgoingSession(StreamContext ctx,
			TransportConnectionWriter w, @Nullable Priority priority)
			throws IOException {
		StreamWriter streamWriter = streamWriterFactory.createStreamWriter(
				w.getOutputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory.createDuplexOutgoingSession(c,
				ctx.getTransportId(), w.getMaxLatency(), w.getMaxIdleTime(),
				streamWriter, priority);
	}
}
