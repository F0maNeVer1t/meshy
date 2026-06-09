package ru.itis.messaging_engine;

import ru.itis.messaging_engine.cleanup.CleanupModule;
import ru.itis.messaging_engine.contact.ContactModule;
import ru.itis.messaging_engine.crypto.CryptoExecutorModule;
import ru.itis.messaging_engine.db.DatabaseExecutorModule;
import ru.itis.messaging_engine.identity.IdentityModule;
import ru.itis.messaging_engine.lifecycle.LifecycleModule;
import ru.itis.messaging_engine.mailbox.MailboxModule;
import ru.itis.messaging_engine.plugin.PluginModule;
import ru.itis.messaging_engine.properties.PropertiesModule;
import ru.itis.messaging_engine.rendezvous.RendezvousModule;
import ru.itis.messaging_engine.sync.validation.ValidationModule;
import ru.itis.messaging_engine.transport.TransportModule;
import ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementModule;
import ru.itis.messaging_engine.versioning.VersioningModule;

public interface BrambleCoreEagerSingletons {

	void inject(CleanupModule.EagerSingletons init);

	void inject(ContactModule.EagerSingletons init);

	void inject(CryptoExecutorModule.EagerSingletons init);

	void inject(DatabaseExecutorModule.EagerSingletons init);

	void inject(IdentityModule.EagerSingletons init);

	void inject(LifecycleModule.EagerSingletons init);

	void inject(MailboxModule.EagerSingletons init);

	void inject(PluginModule.EagerSingletons init);

	void inject(PropertiesModule.EagerSingletons init);

	void inject(RendezvousModule.EagerSingletons init);

	void inject(TransportKeyAgreementModule.EagerSingletons init);

	void inject(TransportModule.EagerSingletons init);

	void inject(ValidationModule.EagerSingletons init);

	void inject(VersioningModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(BrambleCoreEagerSingletons c) {
			c.inject(new CleanupModule.EagerSingletons());
			c.inject(new ContactModule.EagerSingletons());
			c.inject(new CryptoExecutorModule.EagerSingletons());
			c.inject(new DatabaseExecutorModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new LifecycleModule.EagerSingletons());
			c.inject(new MailboxModule.EagerSingletons());
			c.inject(new RendezvousModule.EagerSingletons());
			c.inject(new PluginModule.EagerSingletons());
			c.inject(new PropertiesModule.EagerSingletons());
			c.inject(new TransportKeyAgreementModule.EagerSingletons());
			c.inject(new TransportModule.EagerSingletons());
			c.inject(new ValidationModule.EagerSingletons());
			c.inject(new VersioningModule.EagerSingletons());
		}
	}
}
