package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionManager.TagController;
import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportConnectionReader;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.sync.PriorityHandler;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.util.LogUtils.logException;

@NotNullByDefault
class IncomingSimplexSyncConnection extends SyncConnection implements Runnable {

	private final TransportId transportId;
	private final TransportConnectionReader reader;
	@Nullable
	private final TagController tagController;

	IncomingSimplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			TransportId transportId,
			TransportConnectionReader reader,
			@Nullable TagController tagController) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager);
		this.transportId = transportId;
		this.reader = reader;
		this.tagController = tagController;
	}

	@Override
	public void run() {
		// Read and recognise the tag
		byte[] tag;
		StreamContext ctx;
		try {
			tag = readTag(reader.getInputStream());
			// If we have a tag controller, defer marking the tag as recognised
			if (tagController == null) {
				ctx = keyManager.getStreamContext(transportId, tag);
			} else {
				ctx = keyManager.getStreamContextOnly(transportId, tag);
			}
		} catch (IOException | DbException e) {
			logException(LOG, WARNING, e);
			onError();
			return;
		}
		if (ctx == null) {
			LOG.info("Unrecognised tag");
			onError();
			return;
		}
		ContactId contactId = ctx.getContactId();
		if (contactId == null) {
			LOG.warning("Received rendezvous stream, expected contact");
			onError(tag);
			return;
		}
		if (ctx.isHandshakeMode()) {
			// TODO: Support handshake mode for contacts
			LOG.warning("Received handshake tag, expected rotation mode");
			onError(tag);
			return;
		}
		try {
			// We don't expect to receive a priority for this connection
			PriorityHandler handler = p ->
					LOG.info("Ignoring priority for simplex connection");
			// Create and run the incoming session
			createIncomingSession(ctx, reader, handler).run();
			// Success
			markTagAsRecognisedIfRequired(false, tag);
			reader.dispose(false, true);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onError(tag);
		}
	}

	private void onError() {
		disposeOnError(reader, false);
	}

	private void onError(byte[] tag) {
		markTagAsRecognisedIfRequired(true, tag);
		disposeOnError(reader, true);
	}

	private void markTagAsRecognisedIfRequired(boolean exception, byte[] tag) {
		if (tagController != null &&
				tagController.shouldMarkTagAsRecognised(exception)) {
			try {
				keyManager.markTagAsRecognised(transportId, tag);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		}
	}
}

