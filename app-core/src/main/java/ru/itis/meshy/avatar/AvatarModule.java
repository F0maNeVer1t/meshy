package ru.itis.meshy.avatar;

import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.BdfReaderFactory;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.avatar.AvatarManager;
import ru.itis.meshy.api.avatar.AvatarMessageEncoder;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.meshy.api.avatar.AvatarManager.CLIENT_ID;
import static ru.itis.meshy.api.avatar.AvatarManager.MAJOR_VERSION;
import static ru.itis.meshy.api.avatar.AvatarManager.MINOR_VERSION;

@Module
public class AvatarModule {

	public static class EagerSingletons {
		@Inject
		AvatarValidator avatarValidator;
		@Inject
		AvatarManager avatarManager;
	}

	@Provides
	@Singleton
	AvatarValidator provideAvatarValidator(ValidationManager validationManager,
			BdfReaderFactory bdfReaderFactory, MetadataEncoder metadataEncoder,
			Clock clock) {
		AvatarValidator avatarValidator =
				new AvatarValidator(bdfReaderFactory, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				avatarValidator);
		return avatarValidator;
	}

	@Provides
	@Singleton
	AvatarMessageEncoder provideMessageEncoder(
			AvatarMessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}

	@Provides
	@Singleton
	AvatarManager provideAvatarManager(
			LifecycleManager lifecycleManager,
			ContactManager contactManager,
			ValidationManager validationManager,
			ClientVersioningManager clientVersioningManager,
			AvatarManagerImpl avatarManager) {
		lifecycleManager.registerOpenDatabaseHook(avatarManager);
		contactManager.registerContactHook(avatarManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID,
				MAJOR_VERSION, avatarManager);
		clientVersioningManager.registerClient(CLIENT_ID,
				MAJOR_VERSION, MINOR_VERSION, avatarManager);
		return avatarManager;
	}

}
