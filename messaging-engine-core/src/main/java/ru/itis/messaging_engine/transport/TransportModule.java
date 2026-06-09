package ru.itis.messaging_engine.transport;

import ru.itis.messaging_engine.api.crypto.StreamDecrypterFactory;
import ru.itis.messaging_engine.api.crypto.StreamEncrypterFactory;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.transport.KeyManager;
import ru.itis.messaging_engine.api.transport.StreamReaderFactory;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TransportModule {

	public static class EagerSingletons {
		@Inject
		KeyManager keyManager;
	}

	@Provides
	StreamReaderFactory provideStreamReaderFactory(
			StreamDecrypterFactory streamDecrypterFactory) {
		return new StreamReaderFactoryImpl(streamDecrypterFactory);
	}

	@Provides
	StreamWriterFactory provideStreamWriterFactory(
			StreamEncrypterFactory streamEncrypterFactory) {
		return new StreamWriterFactoryImpl(streamEncrypterFactory);
	}

	@Provides
	TransportKeyManagerFactory provideTransportKeyManagerFactory(
			TransportKeyManagerFactoryImpl transportKeyManagerFactory) {
		return transportKeyManagerFactory;
	}

	@Provides
	@Singleton
	KeyManager provideKeyManager(LifecycleManager lifecycleManager,
			EventBus eventBus, KeyManagerImpl keyManager) {
		lifecycleManager.registerService(keyManager);
		eventBus.addListener(keyManager);
		return keyManager;
	}
}
