package ru.itis.messaging_engine.transport;

import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.crypto.StreamEncrypterFactory;
import ru.itis.messaging_engine.api.transport.StreamContext;
import ru.itis.messaging_engine.api.transport.StreamWriter;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class StreamWriterFactoryImpl implements StreamWriterFactory {

	private final StreamEncrypterFactory streamEncrypterFactory;

	@Inject
	StreamWriterFactoryImpl(StreamEncrypterFactory streamEncrypterFactory) {
		this.streamEncrypterFactory = streamEncrypterFactory;
	}

	@Override
	public StreamWriter createStreamWriter(OutputStream out,
			StreamContext ctx) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createStreamEncrypter(out, ctx));
	}

	@Override
	public StreamWriter createContactExchangeStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createContactExchangeStreamEncrypter(out, headerKey));
	}

	@Override
	public StreamWriter createLogStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createLogStreamEncrypter(out, headerKey));
	}
}
