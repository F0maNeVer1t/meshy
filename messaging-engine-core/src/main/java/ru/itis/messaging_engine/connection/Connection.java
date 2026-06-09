package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.plugin.TransportConnectionReader;
import ru.itis.messaging_engine.api.plugin.TransportConnectionWriter;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static ru.itis.messaging_engine.api.transport.TransportConstants.TAG_LENGTH;
import static ru.itis.messaging_engine.util.IoUtils.read;
import static ru.itis.messaging_engine.util.LogUtils.logException;

@NotNullByDefault
abstract class Connection {

	protected static final Logger LOG = getLogger(Connection.class.getName());

	final KeyManager keyManager;
	final ConnectionRegistry connectionRegistry;
	final StreamReaderFactory streamReaderFactory;
	final StreamWriterFactory streamWriterFactory;

	Connection(KeyManager keyManager, ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory) {
		this.keyManager = keyManager;
		this.connectionRegistry = connectionRegistry;
		this.streamReaderFactory = streamReaderFactory;
		this.streamWriterFactory = streamWriterFactory;
	}

	@Nullable
	StreamContext recogniseTag(TransportConnectionReader reader,
			TransportId transportId) {
		try {
			byte[] tag = readTag(reader.getInputStream());
			return keyManager.getStreamContext(transportId, tag);
		} catch (IOException | DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	byte[] readTag(InputStream in) throws IOException {
		byte[] tag = new byte[TAG_LENGTH];
		read(in, tag);
		return tag;
	}

	void disposeOnError(TransportConnectionReader reader, boolean recognised) {
		try {
			reader.dispose(true, recognised);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}

	void disposeOnError(TransportConnectionWriter writer) {
		try {
			writer.dispose(true);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}
}
