package ru.itis.messaging_engine.properties;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.properties.TransportPropertyManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.messaging_engine.api.properties.TransportPropertyManager.CLIENT_ID;
import static ru.itis.messaging_engine.api.properties.TransportPropertyManager.MAJOR_VERSION;
import static ru.itis.messaging_engine.api.properties.TransportPropertyManager.MINOR_VERSION;

@Module
public class PropertiesModule {

	public static class EagerSingletons {
		@Inject
		TransportPropertyValidator transportPropertyValidator;
		@Inject
		TransportPropertyManager transportPropertyManager;
	}

	@Provides
	@Singleton
	TransportPropertyValidator getValidator(ValidationManager validationManager,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock) {
		TransportPropertyValidator validator = new TransportPropertyValidator(
				clientHelper, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	@Singleton
	TransportPropertyManager getTransportPropertyManager(
			LifecycleManager lifecycleManager,
			ValidationManager validationManager, ContactManager contactManager,
			ClientVersioningManager clientVersioningManager,
			TransportPropertyManagerImpl transportPropertyManager) {
		lifecycleManager.registerOpenDatabaseHook(transportPropertyManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				transportPropertyManager);
		contactManager.registerContactHook(transportPropertyManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, transportPropertyManager);
		return transportPropertyManager;
	}
}
