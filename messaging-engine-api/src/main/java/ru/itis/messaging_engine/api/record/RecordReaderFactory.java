package ru.itis.messaging_engine.api.record;

import java.io.InputStream;

public interface RecordReaderFactory {

	RecordReader createRecordReader(InputStream in);
}
