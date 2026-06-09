package ru.itis.messaging_engine.mailbox;

import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.mailbox.MailboxProperties;
import ru.itis.messaging_engine.api.system.TaskScheduler;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Provider;

@NotNullByDefault
class MailboxClientFactoryImpl implements MailboxClientFactory {

	private final MailboxWorkerFactory workerFactory;
	private final TaskScheduler taskScheduler;
	private final Executor ioExecutor;
	private final Provider<ContactMailboxConnectivityChecker>
			contactCheckerProvider;
	private final Provider<OwnMailboxConnectivityChecker> ownCheckerProvider;

	@Inject
	MailboxClientFactoryImpl(MailboxWorkerFactory workerFactory,
			TaskScheduler taskScheduler,
			@IoExecutor Executor ioExecutor,
			Provider<ContactMailboxConnectivityChecker> contactCheckerProvider,
			Provider<OwnMailboxConnectivityChecker> ownCheckerProvider) {
		this.workerFactory = workerFactory;
		this.taskScheduler = taskScheduler;
		this.ioExecutor = ioExecutor;
		this.contactCheckerProvider = contactCheckerProvider;
		this.ownCheckerProvider = ownCheckerProvider;
	}

	@Override
	public MailboxClient createContactMailboxClient(
			TorReachabilityMonitor reachabilityMonitor) {
		ConnectivityChecker connectivityChecker = contactCheckerProvider.get();
		return new ContactMailboxClient(workerFactory, connectivityChecker,
				reachabilityMonitor);
	}

	@Override
	public MailboxClient createOwnMailboxClient(
			TorReachabilityMonitor reachabilityMonitor,
			MailboxProperties properties) {
		ConnectivityChecker connectivityChecker = ownCheckerProvider.get();
		return new OwnMailboxClient(workerFactory, connectivityChecker,
				reachabilityMonitor, taskScheduler, ioExecutor, properties);
	}
}
