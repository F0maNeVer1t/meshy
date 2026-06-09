package ru.itis.messaging_engine.record;

import ru.itis.messaging_engine.api.record.RecordWriter;
import ru.itis.messaging_engine.api.record.RecordWriterFactory;

import java.io.OutputStream;

class RecordWriterFactoryImpl implements RecordWriterFactory {

	@Override
	public RecordWriter createRecordWriter(OutputStream out) {
		return new RecordWriterImpl(out);
	}
}
