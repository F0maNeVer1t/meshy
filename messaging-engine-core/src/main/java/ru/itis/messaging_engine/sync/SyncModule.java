package ru.itis.messaging_engine.sync;

import ru.itis.messaging_engine.api.sync.GroupFactory;
import ru.itis.messaging_engine.api.sync.MessageFactory;
import ru.itis.messaging_engine.api.sync.SyncRecordReaderFactory;
import ru.itis.messaging_engine.api.sync.SyncRecordWriterFactory;
import ru.itis.messaging_engine.api.sync.SyncSessionFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SyncModule {

	@Provides
	GroupFactory provideGroupFactory(GroupFactoryImpl groupFactory) {
		return groupFactory;
	}

	@Provides
	MessageFactory provideMessageFactory(MessageFactoryImpl messageFactory) {
		return messageFactory;
	}

	@Provides
	SyncRecordReaderFactory provideRecordReaderFactory(
			SyncRecordReaderFactoryImpl recordReaderFactory) {
		return recordReaderFactory;
	}

	@Provides
	SyncRecordWriterFactory provideRecordWriterFactory(
			SyncRecordWriterFactoryImpl recordWriterFactory) {
		return recordWriterFactory;
	}

	@Provides
	@Singleton
	SyncSessionFactory provideSyncSessionFactory(
			SyncSessionFactoryImpl syncSessionFactory) {
		return syncSessionFactory;
	}
}
