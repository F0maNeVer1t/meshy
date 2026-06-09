package ru.itis.messaging_engine.connection;

import ru.itis.messaging_engine.api.connection.ConnectionManager;
import ru.itis.messaging_engine.api.connection.ConnectionRegistry;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ConnectionModule {

	@Provides
	ConnectionManager provideConnectionManager(
			ConnectionManagerImpl connectionManager) {
		return connectionManager;
	}

	@Provides
	@Singleton
	ConnectionRegistry provideConnectionRegistry(
			ConnectionRegistryImpl connectionRegistry) {
		return connectionRegistry;
	}
}
