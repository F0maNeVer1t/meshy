package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventExecutor;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.mailbox.MailboxManager;
import ru.itis.messaging_engine.api.mailbox.MailboxSettingsManager;
import ru.itis.messaging_engine.api.mailbox.MailboxUpdateManager;
import ru.itis.messaging_engine.api.mailbox.MailboxVersion;
import ru.itis.messaging_engine.api.plugin.PluginManager;
import ru.itis.messaging_engine.api.sync.validation.ValidationManager;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.versioning.ClientVersioningManager;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static ru.itis.messaging_engine.api.mailbox.MailboxConstants.CLIENT_SUPPORTS;
import static ru.itis.messaging_engine.api.mailbox.MailboxUpdateManager.CLIENT_ID;
import static ru.itis.messaging_engine.api.mailbox.MailboxUpdateManager.MAJOR_VERSION;
import static ru.itis.messaging_engine.api.mailbox.MailboxUpdateManager.MINOR_VERSION;

@Module
public class MailboxModule {

	public static class EagerSingletons {
		@Inject
		MailboxUpdateValidator mailboxUpdateValidator;
		@Inject
		MailboxUpdateManager mailboxUpdateManager;
		@Inject
		MailboxFileManager mailboxFileManager;
		@Inject
		MailboxClientManager mailboxClientManager;
	}

	@Provides
	@Singleton
	MailboxManager providesMailboxManager(MailboxManagerImpl mailboxManager) {
		return mailboxManager;
	}

	@Provides
	MailboxPairingTaskFactory provideMailboxPairingTaskFactory(
			MailboxPairingTaskFactoryImpl mailboxPairingTaskFactory) {
		return mailboxPairingTaskFactory;
	}

	@Provides
	@Singleton
	MailboxSettingsManager provideMailboxSettingsManager(
			MailboxSettingsManagerImpl mailboxSettingsManager) {
		return mailboxSettingsManager;
	}

	@Provides
	MailboxApi provideMailboxApi(MailboxApiImpl mailboxApi) {
		return mailboxApi;
	}

	@Provides
	@Singleton
	MailboxUpdateValidator provideMailboxUpdateValidator(
			ValidationManager validationManager,
			ClientHelper clientHelper,
			MetadataEncoder metadataEncoder,
			Clock clock) {
		MailboxUpdateValidator validator = new MailboxUpdateValidator(
				clientHelper, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	List<MailboxVersion> provideClientSupports() {
		return CLIENT_SUPPORTS;
	}

	@Provides
	@Singleton
	MailboxUpdateManager provideMailboxUpdateManager(
			LifecycleManager lifecycleManager,
			ValidationManager validationManager, ContactManager contactManager,
			ClientVersioningManager clientVersioningManager,
			MailboxSettingsManager mailboxSettingsManager,
			MailboxUpdateManagerImpl mailboxUpdateManager) {
		lifecycleManager.registerOpenDatabaseHook(mailboxUpdateManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				mailboxUpdateManager);
		contactManager.registerContactHook(mailboxUpdateManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, mailboxUpdateManager);
		mailboxSettingsManager.registerMailboxHook(mailboxUpdateManager);
		return mailboxUpdateManager;
	}

	@Provides
	@Singleton
	MailboxFileManager provideMailboxFileManager(EventBus eventBus,
			MailboxFileManagerImpl mailboxFileManager) {
		eventBus.addListener(mailboxFileManager);
		return mailboxFileManager;
	}

	@Provides
	MailboxWorkerFactory provideMailboxWorkerFactory(
			MailboxWorkerFactoryImpl mailboxWorkerFactory) {
		return mailboxWorkerFactory;
	}

	@Provides
	MailboxClientFactory provideMailboxClientFactory(
			MailboxClientFactoryImpl mailboxClientFactory) {
		return mailboxClientFactory;
	}

	@Provides
	MailboxApiCaller provideMailboxApiCaller(
			MailboxApiCallerImpl mailboxApiCaller) {
		return mailboxApiCaller;
	}

	@Provides
	@Singleton
	TorReachabilityMonitor provideTorReachabilityMonitor(
			TorReachabilityMonitorImpl reachabilityMonitor) {
		return reachabilityMonitor;
	}

	@Provides
	@Singleton
	MailboxClientManager provideMailboxClientManager(
			@EventExecutor Executor eventExecutor,
			@DatabaseExecutor Executor dbExecutor,
			TransactionManager db,
			ContactManager contactManager,
			PluginManager pluginManager,
			MailboxSettingsManager mailboxSettingsManager,
			MailboxUpdateManager mailboxUpdateManager,
			MailboxClientFactory mailboxClientFactory,
			TorReachabilityMonitor reachabilityMonitor,
			LifecycleManager lifecycleManager,
			EventBus eventBus) {
		MailboxClientManager manager = new MailboxClientManager(eventExecutor,
				dbExecutor, db, contactManager, pluginManager,
				mailboxSettingsManager, mailboxUpdateManager,
				mailboxClientFactory, reachabilityMonitor);
		lifecycleManager.registerService(manager);
		eventBus.addListener(manager);
		return manager;
	}
}
