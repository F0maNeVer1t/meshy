package ru.itis.messaging_engine.sync;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.sync.OutgoingSessionRecord;
import ru.itis.messaging_engine.api.sync.Priority;
import ru.itis.messaging_engine.api.sync.PriorityHandler;
import ru.itis.messaging_engine.api.sync.SyncRecordReader;
import ru.itis.messaging_engine.api.sync.SyncRecordReaderFactory;
import ru.itis.messaging_engine.api.sync.SyncRecordWriter;
import ru.itis.messaging_engine.api.sync.SyncRecordWriterFactory;
import ru.itis.messaging_engine.api.sync.SyncSession;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.transport.StreamWriter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.mailbox.MailboxConstants.MAX_FILE_PAYLOAD_BYTES;

@Immutable
@NotNullByDefault
class SyncSessionFactoryImpl implements SyncSessionFactory {

	private final DatabaseComponent db;
	private final Executor dbExecutor;
	private final EventBus eventBus;
	private final Clock clock;
	private final SyncRecordReaderFactory recordReaderFactory;
	private final SyncRecordWriterFactory recordWriterFactory;

	@Inject
	SyncSessionFactoryImpl(DatabaseComponent db,
			@DatabaseExecutor Executor dbExecutor, EventBus eventBus,
			Clock clock, SyncRecordReaderFactory recordReaderFactory,
			SyncRecordWriterFactory recordWriterFactory) {
		this.db = db;
		this.dbExecutor = dbExecutor;
		this.eventBus = eventBus;
		this.clock = clock;
		this.recordReaderFactory = recordReaderFactory;
		this.recordWriterFactory = recordWriterFactory;
	}

	@Override
	public SyncSession createIncomingSession(ContactId c, InputStream in,
			PriorityHandler handler) {
		SyncRecordReader recordReader =
				recordReaderFactory.createRecordReader(in);
		return new IncomingSession(db, dbExecutor, eventBus, c, recordReader,
				handler);
	}

	@Override
	public SyncSession createSimplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, boolean eager, StreamWriter streamWriter) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		if (eager) {
			return new EagerSimplexOutgoingSession(db, eventBus, c, t,
					maxLatency, streamWriter, recordWriter);
		} else {
			return new SimplexOutgoingSession(db, eventBus, c, t,
					maxLatency, streamWriter, recordWriter);
		}
	}

	@Override
	public SyncSession createSimplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, StreamWriter streamWriter,
			OutgoingSessionRecord sessionRecord) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		return new MailboxOutgoingSession(db, eventBus, c, t, maxLatency,
				streamWriter, recordWriter, sessionRecord,
				MAX_FILE_PAYLOAD_BYTES);
	}

	@Override
	public SyncSession createDuplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, int maxIdleTime, StreamWriter streamWriter,
			@Nullable Priority priority) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		return new DuplexOutgoingSession(db, dbExecutor, eventBus, clock, c, t,
				maxLatency, maxIdleTime, streamWriter, recordWriter, priority);
	}
}
