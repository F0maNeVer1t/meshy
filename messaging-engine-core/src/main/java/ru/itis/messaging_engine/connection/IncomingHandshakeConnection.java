package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionManager;
import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactExchangeManager;
import ru.itis.messaging_engine.api.contact.HandshakeManager;
import ru.itis.messaging_engine.api.contact.HandshakeManager.HandshakeResult;
import ru.itis.messaging_engine.api.contact.PendingContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriter;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.util.LogUtils.logException;

@NotNullByDefault
class IncomingHandshakeConnection extends HandshakeConnection
		implements Runnable {

	IncomingHandshakeConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			HandshakeManager handshakeManager,
			ContactExchangeManager contactExchangeManager,
			ConnectionManager connectionManager,
			PendingContactId pendingContactId,
			TransportId transportId, DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, handshakeManager, contactExchangeManager,
				connectionManager, pendingContactId, transportId, connection);
	}

	@Override
	public void run() {
		// Read and recognise the tag
		StreamContext ctxIn = recogniseTag(reader, transportId);
		if (ctxIn == null) {
			LOG.info("Unrecognised tag");
			onError(false);
			return;
		}
		PendingContactId inPendingContactId = ctxIn.getPendingContactId();
		if (inPendingContactId == null) {
			LOG.warning("Expected rendezvous tag, got contact tag");
			onError(true);
			return;
		}
		// Allocate the outgoing stream context
		StreamContext ctxOut =
				allocateStreamContext(pendingContactId, transportId);
		if (ctxOut == null) {
			LOG.warning("Could not allocate stream context");
			onError(true);
			return;
		}
		// Close the connection if it's redundant
		if (!connectionRegistry.registerConnection(pendingContactId)) {
			LOG.info("Redundant rendezvous connection");
			onError(true);
			return;
		}
		// Handshake and exchange contacts
		try {
			InputStream in = streamReaderFactory.createStreamReader(
					reader.getInputStream(), ctxIn);
			// Flush the output stream to send the outgoing stream header
			StreamWriter out = streamWriterFactory.createStreamWriter(
					writer.getOutputStream(), ctxOut);
			out.getOutputStream().flush();
			HandshakeResult result =
					handshakeManager.handshake(pendingContactId, in, out);
			contactExchangeManager.exchangeContacts(pendingContactId,
					connection, result.getMasterKey(), result.isAlice(), false);
			connectionRegistry.unregisterConnection(pendingContactId, true);
			// Reuse the connection as a transport connection
			connectionManager.manageIncomingConnection(transportId, connection);
		} catch (IOException | DbException e) {
			logException(LOG, WARNING, e);
			onError(true);
			connectionRegistry.unregisterConnection(pendingContactId, false);
		}
	}
}
