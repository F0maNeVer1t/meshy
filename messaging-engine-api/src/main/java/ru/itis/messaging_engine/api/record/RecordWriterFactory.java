package ru.itis.messaging_engine.api.record;

import java.io.OutputStream;

public interface RecordWriterFactory {

	RecordWriter createRecordWriter(OutputStream out);
}
