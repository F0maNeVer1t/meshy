package ru.itis.messaging_engine.versioning;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.messaging_engine.api.versioning.ClientVersioningManager.CLIENT_ID;
import static ru.itis.messaging_engine.api.versioning.ClientVersioningManager.MAJOR_VERSION;

@Module
public class VersioningModule {

	public static class EagerSingletons {
		@Inject
		ClientVersioningManager clientVersioningManager;
		@Inject
		ClientVersioningValidator clientVersioningValidator;
	}


	@Provides
	@Singleton
	ClientVersioningManager provideClientVersioningManager(
			ClientVersioningManagerImpl clientVersioningManager,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager) {
		lifecycleManager.registerOpenDatabaseHook(clientVersioningManager);
		lifecycleManager.registerService(clientVersioningManager);
		contactManager.registerContactHook(clientVersioningManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				clientVersioningManager);
		return clientVersioningManager;
	}

	@Provides
	@Singleton
	ClientVersioningValidator provideClientVersioningValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ValidationManager validationManager) {
		ClientVersioningValidator validator = new ClientVersioningValidator(
				clientHelper, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}
}
