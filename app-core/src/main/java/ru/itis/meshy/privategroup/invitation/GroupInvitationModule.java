package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.FeatureFlags;
import ru.itis.messaging_engine.api.cleanup.CleanupManager;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.privategroup.PrivateGroupFactory;
import ru.itis.meshy.api.privategroup.PrivateGroupManager;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationFactory;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;
import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationManager.MAJOR_VERSION;
import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationManager.MINOR_VERSION;

@Module
public class GroupInvitationModule {

	public static class EagerSingletons {
		@Inject
		GroupInvitationValidator groupInvitationValidator;
		@Inject
		GroupInvitationManager groupInvitationManager;
	}

	@Provides
	@Singleton
	GroupInvitationManager provideGroupInvitationManager(
			GroupInvitationManagerImpl groupInvitationManager,
			LifecycleManager lifecycleManager,
			ValidationManager validationManager, ContactManager contactManager,
			PrivateGroupManager privateGroupManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnablePrivateGroupsInCore()) {
			return groupInvitationManager;
		}
		lifecycleManager.registerOpenDatabaseHook(groupInvitationManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				groupInvitationManager);
		contactManager.registerContactHook(groupInvitationManager);
		privateGroupManager.registerPrivateGroupHook(groupInvitationManager);
		conversationManager.registerConversationClient(groupInvitationManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, groupInvitationManager);
		// The group invitation manager handles client visibility changes for
		// the private group manager
		clientVersioningManager.registerClient(PrivateGroupManager.CLIENT_ID,
				PrivateGroupManager.MAJOR_VERSION,
				PrivateGroupManager.MINOR_VERSION,
				groupInvitationManager.getPrivateGroupClientVersioningHook());
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				groupInvitationManager);
		return groupInvitationManager;
	}

	@Provides
	@Singleton
	GroupInvitationValidator provideGroupInvitationValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, PrivateGroupFactory privateGroupFactory,
			MessageEncoder messageEncoder,
			ValidationManager validationManager,
			FeatureFlags featureFlags) {
		GroupInvitationValidator validator = new GroupInvitationValidator(
				clientHelper, metadataEncoder, clock, privateGroupFactory,
				messageEncoder);
		if (featureFlags.shouldEnablePrivateGroupsInCore()) {
			validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
					validator);
		}
		return validator;
	}

	@Provides
	GroupInvitationFactory provideGroupInvitationFactory(
			GroupInvitationFactoryImpl groupInvitationFactory) {
		return groupInvitationFactory;
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
	ProtocolEngineFactory provideProtocolEngineFactory(
			ProtocolEngineFactoryImpl protocolEngineFactory) {
		return protocolEngineFactory;
	}
}
