package ru.itis.messaging_engine.settings;

import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.settings.SettingsManager;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsModule {

	@Provides
	SettingsManager provideSettingsManager(DatabaseComponent db) {
		return new SettingsManagerImpl(db);
	}

}
