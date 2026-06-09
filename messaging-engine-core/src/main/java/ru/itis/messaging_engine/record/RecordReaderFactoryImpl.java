package ru.itis.messaging_engine.record;

import ru.itis.messaging_engine.api.record.RecordReader;
import ru.itis.messaging_engine.api.record.RecordReaderFactory;

import java.io.InputStream;

class RecordReaderFactoryImpl implements RecordReaderFactory {

	@Override
	public RecordReader createRecordReader(InputStream in) {
		return new RecordReaderImpl(in);
	}
}
