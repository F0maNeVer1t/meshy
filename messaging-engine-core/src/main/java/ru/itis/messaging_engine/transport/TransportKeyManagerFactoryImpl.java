package ru.itis.messaging_engine.transport;

import ru.itis.messaging_engine.api.crypto.TransportCrypto;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.api.system.TaskScheduler;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class TransportKeyManagerFactoryImpl implements
		TransportKeyManagerFactory {

	private final DatabaseComponent db;
	private final TransportCrypto transportCrypto;
	private final Executor dbExecutor;
	private final TaskScheduler scheduler;
	private final Clock clock;

	@Inject
	TransportKeyManagerFactoryImpl(DatabaseComponent db,
			TransportCrypto transportCrypto,
			@DatabaseExecutor Executor dbExecutor,
			TaskScheduler scheduler,
			Clock clock) {
		this.db = db;
		this.transportCrypto = transportCrypto;
		this.dbExecutor = dbExecutor;
		this.scheduler = scheduler;
		this.clock = clock;
	}

	@Override
	public TransportKeyManager createTransportKeyManager(
			TransportId transportId, long maxLatency) {
		return new TransportKeyManagerImpl(db, transportCrypto, dbExecutor,
				scheduler, clock, transportId, maxLatency);
	}

}
