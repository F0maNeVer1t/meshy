package ru.itis.messaging_engine;

import ru.itis.messaging_engine.cleanup.CleanupModule;
import ru.itis.messaging_engine.client.ClientModule;
import ru.itis.messaging_engine.connection.ConnectionModule;
import ru.itis.messaging_engine.contact.ContactModule;
import ru.itis.messaging_engine.crypto.CryptoExecutorModule;
import ru.itis.messaging_engine.crypto.CryptoModule;
import ru.itis.messaging_engine.data.DataModule;
import ru.itis.messaging_engine.db.DatabaseExecutorModule;
import ru.itis.messaging_engine.db.DatabaseModule;
import ru.itis.messaging_engine.event.EventModule;
import ru.itis.messaging_engine.identity.IdentityModule;
import ru.itis.messaging_engine.io.IoModule;
import ru.itis.messaging_engine.keyagreement.KeyAgreementModule;
import ru.itis.messaging_engine.lifecycle.LifecycleModule;
import ru.itis.messaging_engine.mailbox.MailboxModule;
import ru.itis.messaging_engine.plugin.PluginModule;
import ru.itis.messaging_engine.properties.PropertiesModule;
import ru.itis.messaging_engine.qrcode.QrCodeModule;
import ru.itis.messaging_engine.record.RecordModule;
import ru.itis.messaging_engine.reliability.ReliabilityModule;
import ru.itis.messaging_engine.rendezvous.RendezvousModule;
import ru.itis.messaging_engine.settings.SettingsModule;
import ru.itis.messaging_engine.sync.SyncModule;
import ru.itis.messaging_engine.sync.validation.ValidationModule;
import ru.itis.messaging_engine.transport.TransportModule;
import ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementModule;
import ru.itis.messaging_engine.versioning.VersioningModule;

import dagger.Module;

@Module(includes = {
		CleanupModule.class,
		ClientModule.class,
		ConnectionModule.class,
		ContactModule.class,
		CryptoModule.class,
		CryptoExecutorModule.class,
		DataModule.class,
		DatabaseModule.class,
		DatabaseExecutorModule.class,
		EventModule.class,
		IdentityModule.class,
		IoModule.class,
		KeyAgreementModule.class,
		LifecycleModule.class,
		MailboxModule.class,
		PluginModule.class,
		PropertiesModule.class,
		QrCodeModule.class,
		RecordModule.class,
		ReliabilityModule.class,
		RendezvousModule.class,
		SettingsModule.class,
		SyncModule.class,
		TransportKeyAgreementModule.class,
		TransportModule.class,
		ValidationModule.class,
		VersioningModule.class
})
public class BrambleCoreModule {
}
