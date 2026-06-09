package ru.itis.messaging_engine.lifecycle;

import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.db.DataTooNewException;
import ru.itis.messaging_engine.api.db.DataTooOldException;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.MigrationListener;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.lifecycle.Service;
import ru.itis.messaging_engine.api.lifecycle.ServiceException;
import ru.itis.messaging_engine.api.lifecycle.event.LifecycleEvent;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.COMPACTING_DATABASE;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.CREATED;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.MIGRATING_DATABASE;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.RUNNING;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STARTING;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STOPPED;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STOPPING;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.ALREADY_RUNNING;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.CLOCK_ERROR;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.DATA_TOO_NEW_ERROR;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.DATA_TOO_OLD_ERROR;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.DB_ERROR;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.SERVICE_ERROR;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.StartResult.SUCCESS;
import static ru.itis.messaging_engine.api.system.Clock.MAX_REASONABLE_TIME_MS;
import static ru.itis.messaging_engine.api.system.Clock.MIN_REASONABLE_TIME_MS;
import static ru.itis.messaging_engine.util.LogUtils.logDuration;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.messaging_engine.util.LogUtils.now;

@ThreadSafe
@NotNullByDefault
class LifecycleManagerImpl implements LifecycleManager, MigrationListener {

	private static final Logger LOG =
			getLogger(LifecycleManagerImpl.class.getName());

	private final DatabaseComponent db;
	private final EventBus eventBus;
	private final Clock clock;
	private final List<Service> services;
	private final List<OpenDatabaseHook> openDatabaseHooks;
	private final List<ExecutorService> executors;
	private final CountDownLatch dbLatch = new CountDownLatch(1);
	private final CountDownLatch startupLatch = new CountDownLatch(1);
	private final CountDownLatch shutdownLatch = new CountDownLatch(1);
	private final AtomicReference<LifecycleState> state =
			new AtomicReference<>(CREATED);

	@Inject
	LifecycleManagerImpl(DatabaseComponent db, EventBus eventBus,
			Clock clock) {
		this.db = db;
		this.eventBus = eventBus;
		this.clock = clock;
		services = new CopyOnWriteArrayList<>();
		openDatabaseHooks = new CopyOnWriteArrayList<>();
		executors = new CopyOnWriteArrayList<>();
	}

	@Override
	public void registerService(Service s) {
		if (LOG.isLoggable(INFO))
			LOG.info("Registering service " + s.getClass().getSimpleName());
		services.add(s);
	}

	@Override
	public void registerOpenDatabaseHook(OpenDatabaseHook hook) {
		if (LOG.isLoggable(INFO)) {
			LOG.info("Registering open database hook "
					+ hook.getClass().getSimpleName());
		}
		openDatabaseHooks.add(hook);
	}

	@Override
	public void registerForShutdown(ExecutorService e) {
		LOG.info("Registering executor " + e.getClass().getSimpleName());
		executors.add(e);
	}

	@Override
	public StartResult startServices(SecretKey dbKey) {
		if (!state.compareAndSet(CREATED, STARTING)) {
			LOG.warning("Already running");
			return ALREADY_RUNNING;
		}
		long now = clock.currentTimeMillis();
		if (now < MIN_REASONABLE_TIME_MS || now > MAX_REASONABLE_TIME_MS) {
			if (LOG.isLoggable(WARNING)) {
				LOG.warning("System clock is unreasonable: " + now);
			}
			return CLOCK_ERROR;
		}
		try {
			LOG.info("Opening database");
			long start = now();
			boolean reopened = db.open(dbKey, this);
			if (reopened) logDuration(LOG, "Reopening database", start);
			else logDuration(LOG, "Creating database", start);

			db.transaction(false, txn -> {
				long start1 = now();
				db.removeTemporaryMessages(txn);
				logDuration(LOG, "Removing temporary messages", start1);
				for (OpenDatabaseHook hook : openDatabaseHooks) {
					start1 = now();
					hook.onDatabaseOpened(txn);
					if (LOG.isLoggable(FINE)) {
						logDuration(LOG, "Calling open database hook "
								+ hook.getClass().getSimpleName(), start1);
					}
				}
			});

			LOG.info("Starting services");
			state.set(STARTING_SERVICES);
			dbLatch.countDown();
			eventBus.broadcast(new LifecycleEvent(STARTING_SERVICES));

			for (Service s : services) {
				start = now();
				s.startService();
				if (LOG.isLoggable(FINE)) {
					logDuration(LOG, "Starting service "
							+ s.getClass().getSimpleName(), start);
				}
			}

			state.set(RUNNING);
			startupLatch.countDown();
			eventBus.broadcast(new LifecycleEvent(RUNNING));
			return SUCCESS;
		} catch (DataTooOldException e) {
			logException(LOG, WARNING, e);
			return DATA_TOO_OLD_ERROR;
		} catch (DataTooNewException e) {
			logException(LOG, WARNING, e);
			return DATA_TOO_NEW_ERROR;
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			return DB_ERROR;
		} catch (ServiceException e) {
			logException(LOG, WARNING, e);
			return SERVICE_ERROR;
		}
	}

	@Override
	public void onDatabaseMigration() {
		state.set(MIGRATING_DATABASE);
		eventBus.broadcast(new LifecycleEvent(MIGRATING_DATABASE));
	}

	@Override
	public void onDatabaseCompaction() {
		state.set(COMPACTING_DATABASE);
		eventBus.broadcast(new LifecycleEvent(COMPACTING_DATABASE));
	}

	@Override
	public void stopServices() {
		if (!state.compareAndSet(RUNNING, STOPPING)) {
			LOG.warning("Not running");
			return;
		}
		LOG.info("Stopping services");
		eventBus.broadcast(new LifecycleEvent(STOPPING));
		for (Service s : services) {
			try {
				long start = now();
				s.stopService();
				if (LOG.isLoggable(FINE)) {
					logDuration(LOG, "Stopping service "
							+ s.getClass().getSimpleName(), start);
				}
			} catch (ServiceException e) {
				logException(LOG, WARNING, e);
			}
		}
		for (ExecutorService e : executors) {
			if (LOG.isLoggable(FINE)) {
				LOG.fine("Stopping executor "
						+ e.getClass().getSimpleName());
			}
			e.shutdownNow();
		}
		try {
			long start = now();
			db.close();
			logDuration(LOG, "Closing database", start);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
		}
		state.set(STOPPED);
		shutdownLatch.countDown();
		eventBus.broadcast(new LifecycleEvent(STOPPED));
	}

	@Override
	public void waitForDatabase() throws InterruptedException {
		dbLatch.await();
	}

	@Override
	public void waitForStartup() throws InterruptedException {
		startupLatch.await();
	}

	@Override
	public void waitForShutdown() throws InterruptedException {
		shutdownLatch.await();
	}

	@Override
	public LifecycleState getLifecycleState() {
		return state.get();
	}
}
