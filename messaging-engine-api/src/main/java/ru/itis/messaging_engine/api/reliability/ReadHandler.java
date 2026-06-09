package ru.itis.messaging_engine.api.reliability;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface ReadHandler {

	void handleRead(byte[] b) throws IOException;
}
