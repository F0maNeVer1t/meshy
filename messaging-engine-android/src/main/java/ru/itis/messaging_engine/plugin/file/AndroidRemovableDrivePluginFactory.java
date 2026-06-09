package ru.itis.messaging_engine.plugin.file;

import android.app.Application;

import ru.itis.messaging_engine.api.plugin.PluginCallback;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.simplex.SimplexPlugin;
import ru.itis.messaging_engine.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.concurrent.TimeUnit.DAYS;
import static ru.itis.messaging_engine.api.plugin.file.RemovableDriveConstants.ID;

@Immutable
@NotNullByDefault
public class AndroidRemovableDrivePluginFactory implements
		SimplexPluginFactory {

	private static final long MAX_LATENCY = DAYS.toMillis(28);

	private final Application app;

	@Inject
	AndroidRemovableDrivePluginFactory(Application app) {
		this.app = app;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Nullable
	@Override
	public SimplexPlugin createPlugin(PluginCallback callback) {
		return new AndroidRemovableDrivePlugin(app, callback, MAX_LATENCY);
	}
}
