package ru.itis.messaging_engine.contact;

import ru.itis.messaging_engine.api.contact.ContactExchangeManager;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.contact.HandshakeManager;
import ru.itis.messaging_engine.api.event.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContactModule {

	public static class EagerSingletons {
		@Inject
		ContactManager contactManager;
	}

	@Provides
	@Singleton
	ContactManager provideContactManager(EventBus eventBus,
			ContactManagerImpl contactManager) {
		eventBus.addListener(contactManager);
		return contactManager;
	}

	@Provides
	ContactExchangeManager provideContactExchangeManager(
			ContactExchangeManagerImpl contactExchangeManager) {
		return contactExchangeManager;
	}

	@Provides
	PendingContactFactory providePendingContactFactory(
			PendingContactFactoryImpl pendingContactFactory) {
		return pendingContactFactory;
	}

	@Provides
	ContactExchangeCrypto provideContactExchangeCrypto(
			ContactExchangeCryptoImpl contactExchangeCrypto) {
		return contactExchangeCrypto;
	}

	@Provides
	@Singleton
	HandshakeManager provideHandshakeManager(
			HandshakeManagerImpl handshakeManager) {
		return handshakeManager;
	}

	@Provides
	HandshakeCrypto provideHandshakeCrypto(
			HandshakeCryptoImpl handshakeCrypto) {
		return handshakeCrypto;
	}
}
