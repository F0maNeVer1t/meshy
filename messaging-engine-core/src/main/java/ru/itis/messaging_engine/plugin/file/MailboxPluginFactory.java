package ru.itis.messaging_engine.plugin.file;

import ru.itis.messaging_engine.api.plugin.PluginCallback;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.simplex.SimplexPlugin;
import ru.itis.messaging_engine.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.mailbox.MailboxConstants.ID;
import static ru.itis.messaging_engine.api.mailbox.MailboxConstants.MAX_LATENCY;

@NotNullByDefault
public class MailboxPluginFactory implements SimplexPluginFactory {

	@Inject
	MailboxPluginFactory() {
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
		return new MailboxPlugin(callback, MAX_LATENCY);
	}
}
