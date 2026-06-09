package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.sync.PriorityHandler;
import ru.itis.messaging_engine.api.sync.SyncSession;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.concurrent.Executor;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.util.LogUtils.logException;

@NotNullByDefault
class IncomingDuplexSyncConnection extends DuplexSyncConnection
		implements Runnable {

	IncomingDuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, TransportId transportId,
			DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager, ioExecutor, transportId, connection);
	}

	@Override
	public void run() {
		// Read and recognise the tag
		StreamContext ctx = recogniseTag(reader, transportId);
		if (ctx == null) {
			LOG.info("Unrecognised tag");
			onReadError(false);
			return;
		}
		ContactId contactId = ctx.getContactId();
		if (contactId == null) {
			LOG.warning("Expected contact tag, got rendezvous tag");
			onReadError(true);
			return;
		}
		if (ctx.isHandshakeMode()) {
			// TODO: Support handshake mode for contacts
			LOG.warning("Received handshake tag, expected rotation mode");
			onReadError(true);
			return;
		}
		connectionRegistry.registerIncomingConnection(contactId, transportId,
				this);
		// Start the outgoing session on another thread
		ioExecutor.execute(() -> runOutgoingSession(contactId));
		try {
			// Store any transport properties discovered from the connection
			transportPropertyManager.addRemotePropertiesFromConnection(
					contactId, transportId, remote);
			// Update the connection registry when we receive our priority
			PriorityHandler handler = p -> connectionRegistry.setPriority(
					contactId, transportId, this, p);
			// Create and run the incoming session
			createIncomingSession(ctx, reader, handler).run();
			reader.dispose(false, true);
			interruptOutgoingSession();
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, true, false);
		} catch (DbException | IOException e) {
			logException(LOG, WARNING, e);
			onReadError(true);
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, true, true);
		}
	}

	private void runOutgoingSession(ContactId contactId) {
		// Allocate a stream context
		StreamContext ctx = allocateStreamContext(contactId, transportId);
		if (ctx == null) {
			LOG.warning("Could not allocate stream context");
			onWriteError();
			return;
		}
		try {
			// Create and run the outgoing session
			SyncSession out = createDuplexOutgoingSession(ctx, writer, null);
			setOutgoingSession(out);
			out.run();
			writer.dispose(false);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onWriteError();
		}
	}
}

