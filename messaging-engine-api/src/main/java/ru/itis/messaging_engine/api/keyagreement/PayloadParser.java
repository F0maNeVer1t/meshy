package ru.itis.messaging_engine.api.keyagreement;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface PayloadParser {

	Payload parse(String payload) throws IOException;
}
