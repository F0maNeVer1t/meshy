package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.sync.Priority;
import ru.itis.messaging_engine.api.sync.PriorityHandler;
import ru.itis.messaging_engine.api.sync.SyncSession;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.Executor;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.api.sync.SyncConstants.PRIORITY_NONCE_BYTES;
import static ru.itis.messaging_engine.util.LogUtils.logException;

@NotNullByDefault
class OutgoingDuplexSyncConnection extends DuplexSyncConnection
		implements Runnable {

	private final SecureRandom secureRandom;
	private final ContactId contactId;

	OutgoingDuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, SecureRandom secureRandom, ContactId contactId,
			TransportId transportId, DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager, ioExecutor, transportId, connection);
		this.secureRandom = secureRandom;
		this.contactId = contactId;
	}

	@Override
	public void run() {
		// Allocate a stream context
		StreamContext ctx = allocateStreamContext(contactId, transportId);
		if (ctx == null) {
			LOG.warning("Could not allocate stream context");
			onWriteError();
			return;
		}
		if (ctx.isHandshakeMode()) {
			// TODO: Support handshake mode for contacts
			LOG.warning("Cannot use handshake mode stream context");
			onWriteError();
			return;
		}
		// Start the incoming session on another thread
		Priority priority = generatePriority();
		ioExecutor.execute(() -> runIncomingSession(priority));
		try {
			// Create and run the outgoing session
			SyncSession out =
					createDuplexOutgoingSession(ctx, writer, priority);
			setOutgoingSession(out);
			out.run();
			writer.dispose(false);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onWriteError();
		}
	}

	private void runIncomingSession(Priority priority) {
		// Read and recognise the tag
		StreamContext ctx = recogniseTag(reader, transportId);
		// Unrecognised tags are suspicious in this case
		if (ctx == null) {
			LOG.warning("Unrecognised tag for returning stream");
			onReadError();
			return;
		}
		// Check that the stream comes from the expected contact
		ContactId inContactId = ctx.getContactId();
		if (inContactId == null) {
			LOG.warning("Expected contact tag, got rendezvous tag");
			onReadError();
			return;
		}
		if (!contactId.equals(inContactId)) {
			LOG.warning("Wrong contact ID for returning stream");
			onReadError();
			return;
		}
		if (ctx.isHandshakeMode()) {
			// TODO: Support handshake mode for contacts
			LOG.warning("Received handshake tag, expected rotation mode");
			onReadError();
			return;
		}
		connectionRegistry.registerOutgoingConnection(contactId, transportId,
				this, priority);
		try {
			// Store any transport properties discovered from the connection
			transportPropertyManager.addRemotePropertiesFromConnection(
					contactId, transportId, remote);
			// We don't expect to receive a priority for this connection
			PriorityHandler handler = p ->
					LOG.info("Ignoring priority for outgoing connection");
			// Create and run the incoming session
			createIncomingSession(ctx, reader, handler).run();
			reader.dispose(false, true);
			interruptOutgoingSession();
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, false, false);
		} catch (DbException | IOException e) {
			logException(LOG, WARNING, e);
			onReadError();
			connectionRegistry.unregisterConnection(contactId, transportId,
					this, false, true);
		}
	}

	private void onReadError() {
		// 'Recognised' is always true for outgoing connections
		onReadError(true);
	}

	private Priority generatePriority() {
		byte[] nonce = new byte[PRIORITY_NONCE_BYTES];
		secureRandom.nextBytes(nonce);
		return new Priority(nonce);
	}
}
