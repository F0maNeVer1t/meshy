package ru.itis.meshy.introduction;

import ru.itis.messaging_engine.api.cleanup.CleanupManager;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.introduction.IntroductionManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.meshy.api.introduction.IntroductionManager.CLIENT_ID;
import static ru.itis.meshy.api.introduction.IntroductionManager.MAJOR_VERSION;
import static ru.itis.meshy.api.introduction.IntroductionManager.MINOR_VERSION;

@Module
public class IntroductionModule {

	public static class EagerSingletons {
		@Inject
		IntroductionValidator introductionValidator;
		@Inject
		IntroductionManager introductionManager;
	}

	@Provides
	@Singleton
	IntroductionValidator provideValidator(ValidationManager validationManager,
			MessageEncoder messageEncoder, MetadataEncoder metadataEncoder,
			ClientHelper clientHelper, Clock clock) {
		IntroductionValidator introductionValidator =
				new IntroductionValidator(messageEncoder, clientHelper,
						metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				introductionValidator);
		return introductionValidator;
	}

	@Provides
	@Singleton
	IntroductionManager provideIntroductionManager(
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			IntroductionManagerImpl introductionManager,
			CleanupManager cleanupManager) {
		lifecycleManager.registerOpenDatabaseHook(introductionManager);
		contactManager.registerContactHook(introductionManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID,
				MAJOR_VERSION, introductionManager);
		conversationManager.registerConversationClient(introductionManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, introductionManager);
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				introductionManager);
		return introductionManager;
	}

	@Provides
	MessageParser provideMessageParser(MessageParserImpl messageParser) {
		return messageParser;
	}

	@Provides
	MessageEncoder provideMessageEncoder(MessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}

	@Provides
	SessionParser provideSessionParser(SessionParserImpl sessionParser) {
		return sessionParser;
	}

	@Provides
	SessionEncoder provideSessionEncoder(SessionEncoderImpl sessionEncoder) {
		return sessionEncoder;
	}

	@Provides
	IntroductionCrypto provideIntroductionCrypto(
			IntroductionCryptoImpl introductionCrypto) {
		return introductionCrypto;
	}

}
