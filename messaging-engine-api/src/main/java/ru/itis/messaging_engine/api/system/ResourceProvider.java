package ru.itis.messaging_engine.api.system;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

@NotNullByDefault
public interface ResourceProvider {

	InputStream getResourceInputStream(String name, String extension);
}
