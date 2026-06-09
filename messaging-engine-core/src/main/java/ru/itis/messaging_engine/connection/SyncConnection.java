package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportConnectionReader;
import ru.itis.messaging_engine.api.plugin.TransportId;
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
import java.io.InputStream;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class SyncConnection extends Connection {

	final SyncSessionFactory syncSessionFactory;
	final TransportPropertyManager transportPropertyManager;

	SyncConnection(KeyManager keyManager, ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory);
		this.syncSessionFactory = syncSessionFactory;
		this.transportPropertyManager = transportPropertyManager;
	}

	@Nullable
	StreamContext allocateStreamContext(ContactId contactId,
			TransportId transportId) {
		try {
			return keyManager.getStreamContext(contactId, transportId);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	SyncSession createIncomingSession(StreamContext ctx,
			TransportConnectionReader r, PriorityHandler handler)
			throws IOException {
		InputStream streamReader = streamReaderFactory.createStreamReader(
				r.getInputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory
				.createIncomingSession(c, streamReader, handler);
	}
}
