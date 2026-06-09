package ru.itis.messaging_engine.plugin.file;

import ru.itis.messaging_engine.api.connection.ConnectionManager;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DatabaseComponent;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventExecutor;
import ru.itis.messaging_engine.api.plugin.PluginManager;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveTask;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class RemovableDriveTaskFactoryImpl implements RemovableDriveTaskFactory {

	private final DatabaseComponent db;
	private final Executor eventExecutor;
	private final PluginManager pluginManager;
	private final ConnectionManager connectionManager;
	private final EventBus eventBus;

	@Inject
	RemovableDriveTaskFactoryImpl(
			DatabaseComponent db,
			@EventExecutor Executor eventExecutor,
			PluginManager pluginManager,
			ConnectionManager connectionManager,
			EventBus eventBus) {
		this.db = db;
		this.eventExecutor = eventExecutor;
		this.pluginManager = pluginManager;
		this.connectionManager = connectionManager;
		this.eventBus = eventBus;
	}

	@Override
	public RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p) {
		return new RemovableDriveReaderTask(eventExecutor, pluginManager,
				connectionManager, eventBus, registry, p);
	}

	@Override
	public RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p) {
		return new RemovableDriveWriterTask(db, eventExecutor, pluginManager,
				connectionManager, eventBus, registry, c, p);
	}
}
