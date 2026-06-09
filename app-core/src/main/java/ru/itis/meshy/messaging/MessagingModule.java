package ru.itis.meshy.messaging;

import ru.itis.messaging_engine.api.FeatureFlags;
import ru.itis.messaging_engine.api.cleanup.CleanupManager;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.BdfReaderFactory;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.messaging.MessagingManager;
import ru.itis.meshy.api.messaging.PrivateMessageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.meshy.api.messaging.MessagingManager.CLIENT_ID;
import static ru.itis.meshy.api.messaging.MessagingManager.MAJOR_VERSION;
import static ru.itis.meshy.api.messaging.MessagingManager.MINOR_VERSION;

@Module
public class MessagingModule {

	public static class EagerSingletons {
		@Inject
		MessagingManager messagingManager;
		@Inject
		PrivateMessageValidator privateMessageValidator;
	}

	@Provides
	PrivateMessageFactory providePrivateMessageFactory(
			PrivateMessageFactoryImpl privateMessageFactory) {
		return privateMessageFactory;
	}

	@Provides
	@Singleton
	PrivateMessageValidator getValidator(ValidationManager validationManager,
			BdfReaderFactory bdfReaderFactory, MetadataEncoder metadataEncoder,
			Clock clock) {
		PrivateMessageValidator validator = new PrivateMessageValidator(
				bdfReaderFactory, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	@Singleton
	MessagingManager getMessagingManager(LifecycleManager lifecycleManager,
			ContactManager contactManager, ValidationManager validationManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags,
			MessagingManagerImpl messagingManager) {
		lifecycleManager.registerOpenDatabaseHook(messagingManager);
		contactManager.registerContactHook(messagingManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		conversationManager.registerConversationClient(messagingManager);
		// Don't advertise support for image attachments or disappearing
		// messages unless the respective feature flags are enabled
		boolean images = featureFlags.shouldEnableImageAttachments();
		boolean disappear = featureFlags.shouldEnableDisappearingMessages();
		int minorVersion = images ? (disappear ? MINOR_VERSION : 2) : 0;
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				minorVersion, messagingManager);
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		return messagingManager;
	}
}
