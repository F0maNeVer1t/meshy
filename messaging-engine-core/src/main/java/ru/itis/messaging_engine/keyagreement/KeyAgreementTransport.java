package ru.itis.messaging_engine.keyagreement;

import ru.itis.messaging_engine.api.keyagreement.KeyAgreementConnection;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.record.Record;
import ru.itis.messaging_engine.api.record.RecordReader;
import ru.itis.messaging_engine.api.record.RecordReader.RecordPredicate;
import ru.itis.messaging_engine.api.record.RecordReaderFactory;
import ru.itis.messaging_engine.api.record.RecordWriter;
import ru.itis.messaging_engine.api.record.RecordWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static ru.itis.messaging_engine.api.keyagreement.KeyAgreementConstants.PROTOCOL_VERSION;
import static ru.itis.messaging_engine.api.keyagreement.RecordTypes.ABORT;
import static ru.itis.messaging_engine.api.keyagreement.RecordTypes.CONFIRM;
import static ru.itis.messaging_engine.api.keyagreement.RecordTypes.KEY;
import static ru.itis.messaging_engine.util.LogUtils.logException;

/**
 * Handles the sending and receiving of BQP records.
 */
@NotNullByDefault
class KeyAgreementTransport {

	private static final Logger LOG =
			Logger.getLogger(KeyAgreementTransport.class.getName());

	// Accept records with current protocol version, known record type
	private static final RecordPredicate ACCEPT = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					isKnownRecordType(r.getRecordType());

	// Ignore records with current protocol version, unknown record type
	private static final RecordPredicate IGNORE = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					!isKnownRecordType(r.getRecordType());

	private static boolean isKnownRecordType(byte type) {
		return type == KEY || type == CONFIRM || type == ABORT;
	}

	private final KeyAgreementConnection kac;
	private final RecordReader reader;
	private final RecordWriter writer;

	KeyAgreementTransport(RecordReaderFactory recordReaderFactory,
			RecordWriterFactory recordWriterFactory, KeyAgreementConnection kac)
			throws IOException {
		this.kac = kac;
		InputStream in = kac.getConnection().getReader().getInputStream();
		reader = recordReaderFactory.createRecordReader(in);
		OutputStream out = kac.getConnection().getWriter().getOutputStream();
		writer = recordWriterFactory.createRecordWriter(out);
	}

	public DuplexTransportConnection getConnection() {
		return kac.getConnection();
	}

	public TransportId getTransportId() {
		return kac.getTransportId();
	}

	void sendKey(byte[] key) throws IOException {
		writeRecord(KEY, key);
	}

	byte[] receiveKey() throws AbortException {
		return readRecord(KEY);
	}

	void sendConfirm(byte[] confirm) throws IOException {
		writeRecord(CONFIRM, confirm);
	}

	byte[] receiveConfirm() throws AbortException {
		return readRecord(CONFIRM);
	}

	void sendAbort(boolean exception) {
		try {
			writeRecord(ABORT, new byte[0]);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			exception = true;
		}
		tryToClose(exception);
	}

	private void tryToClose(boolean exception) {
		try {
			kac.getConnection().getReader().dispose(exception, true);
			kac.getConnection().getWriter().dispose(exception);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}

	private void writeRecord(byte type, byte[] payload) throws IOException {
		writer.writeRecord(new Record(PROTOCOL_VERSION, type, payload));
		writer.flush();
	}

	private byte[] readRecord(byte expectedType) throws AbortException {
		try {
			Record record = reader.readRecord(ACCEPT, IGNORE);
			if (record == null) throw new AbortException(new EOFException());
			byte type = record.getRecordType();
			if (type == ABORT) throw new AbortException(true);
			if (type != expectedType) throw new AbortException(false);
			return record.getPayload();
		} catch (IOException e) {
			throw new AbortException(e);
		}
	}
}
