package ru.itis.messaging_engine.api.plugin.duplex;

import ru.itis.messaging_engine.api.plugin.PluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a duplex transport.
 */
@NotNullByDefault
public interface DuplexPluginFactory extends PluginFactory<DuplexPlugin> {
}
