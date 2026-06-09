package ru.itis.messaging_engine.sync;

import ru.itis.messaging_engine.api.record.Record;
import ru.itis.messaging_engine.api.record.RecordWriter;
import ru.itis.messaging_engine.api.sync.Ack;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageFactory;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.sync.Offer;
import ru.itis.messaging_engine.api.sync.Priority;
import ru.itis.messaging_engine.api.sync.Request;
import ru.itis.messaging_engine.api.sync.SyncRecordWriter;
import ru.itis.messaging_engine.api.sync.Versions;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import static ru.itis.messaging_engine.api.sync.RecordTypes.ACK;
import static ru.itis.messaging_engine.api.sync.RecordTypes.MESSAGE;
import static ru.itis.messaging_engine.api.sync.RecordTypes.OFFER;
import static ru.itis.messaging_engine.api.sync.RecordTypes.PRIORITY;
import static ru.itis.messaging_engine.api.sync.RecordTypes.REQUEST;
import static ru.itis.messaging_engine.api.sync.RecordTypes.VERSIONS;
import static ru.itis.messaging_engine.api.sync.SyncConstants.PROTOCOL_VERSION;

@NotThreadSafe
@NotNullByDefault
class SyncRecordWriterImpl implements SyncRecordWriter {

	private final MessageFactory messageFactory;
	private final RecordWriter writer;
	private final ByteArrayOutputStream payload = new ByteArrayOutputStream();

	SyncRecordWriterImpl(MessageFactory messageFactory, RecordWriter writer) {
		this.messageFactory = messageFactory;
		this.writer = writer;
	}

	private void writeRecord(byte recordType) throws IOException {
		writer.writeRecord(new Record(PROTOCOL_VERSION, recordType,
				payload.toByteArray()));
		payload.reset();
	}

	@Override
	public void writeAck(Ack a) throws IOException {
		for (MessageId m : a.getMessageIds()) payload.write(m.getBytes());
		writeRecord(ACK);
	}

	@Override
	public void writeMessage(Message m) throws IOException {
		byte[] raw = messageFactory.getRawMessage(m);
		writer.writeRecord(new Record(PROTOCOL_VERSION, MESSAGE, raw));
	}

	@Override
	public void writeOffer(Offer o) throws IOException {
		for (MessageId m : o.getMessageIds()) payload.write(m.getBytes());
		writeRecord(OFFER);
	}

	@Override
	public void writeRequest(Request r) throws IOException {
		for (MessageId m : r.getMessageIds()) payload.write(m.getBytes());
		writeRecord(REQUEST);
	}

	@Override
	public void writeVersions(Versions v) throws IOException {
		for (byte b : v.getSupportedVersions()) payload.write(b);
		writeRecord(VERSIONS);
	}

	@Override
	public void writePriority(Priority p) throws IOException {
		writer.writeRecord(
				new Record(PROTOCOL_VERSION, PRIORITY, p.getNonce()));
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public long getBytesWritten() {
		return writer.getBytesWritten();
	}
}
