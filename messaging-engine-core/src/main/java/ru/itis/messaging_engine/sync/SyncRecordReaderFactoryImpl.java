package ru.itis.messaging_engine.sync;

import ru.itis.messaging_engine.api.record.RecordReader;
import ru.itis.messaging_engine.api.record.RecordReaderFactory;
import ru.itis.messaging_engine.api.sync.MessageFactory;
import ru.itis.messaging_engine.api.sync.SyncRecordReader;
import ru.itis.messaging_engine.api.sync.SyncRecordReaderFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class SyncRecordReaderFactoryImpl implements SyncRecordReaderFactory {

	private final MessageFactory messageFactory;
	private final RecordReaderFactory recordReaderFactory;

	@Inject
	SyncRecordReaderFactoryImpl(MessageFactory messageFactory,
			RecordReaderFactory recordReaderFactory) {
		this.messageFactory = messageFactory;
		this.recordReaderFactory = recordReaderFactory;
	}

	@Override
	public SyncRecordReader createRecordReader(InputStream in) {
		RecordReader reader = recordReaderFactory.createRecordReader(in);
		return new SyncRecordReaderImpl(messageFactory, reader);
	}
}
