package ru.itis.messaging_engine.transport.agreement;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.transport.agreement.TransportKeyAgreementManager;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.messaging_engine.api.transport.agreement.TransportKeyAgreementManager.CLIENT_ID;
import static ru.itis.messaging_engine.api.transport.agreement.TransportKeyAgreementManager.MAJOR_VERSION;
import static ru.itis.messaging_engine.api.transport.agreement.TransportKeyAgreementManager.MINOR_VERSION;

@Module
public class TransportKeyAgreementModule {

	public static class EagerSingletons {
		@Inject
		TransportKeyAgreementManager transportKeyAgreementManager;
		@Inject
		TransportKeyAgreementValidator transportKeyAgreementValidator;
	}

	@Provides
	@Singleton
	TransportKeyAgreementManager provideTransportKeyAgreementManager(
			LifecycleManager lifecycleManager,
			ValidationManager validationManager,
			ContactManager contactManager,
			ClientVersioningManager clientVersioningManager,
			TransportKeyAgreementManagerImpl transportKeyAgreementManager) {
		lifecycleManager.registerOpenDatabaseHook(transportKeyAgreementManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID,
				MAJOR_VERSION, transportKeyAgreementManager);
		contactManager.registerContactHook(transportKeyAgreementManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, transportKeyAgreementManager);
		return transportKeyAgreementManager;
	}

	@Provides
	@Singleton
	TransportKeyAgreementValidator provideTransportKeyAgreementValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, MessageEncoder messageEncoder,
			ValidationManager validationManager) {
		TransportKeyAgreementValidator validator =
				new TransportKeyAgreementValidator(clientHelper,
						metadataEncoder, clock, messageEncoder);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	MessageEncoder provideMessageEncoder(MessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}

	@Provides
	SessionEncoder provideSessionEncoder(SessionEncoderImpl sessionEncoder) {
		return sessionEncoder;
	}

	@Provides
	SessionParser provideSessionParser(SessionParserImpl sessionParser) {
		return sessionParser;
	}

	@Provides
	TransportKeyAgreementCrypto provideTransportKeyAgreementCrypto(
			TransportKeyAgreementCryptoImpl transportKeyAgreementCrypto) {
		return transportKeyAgreementCrypto;
	}
}
