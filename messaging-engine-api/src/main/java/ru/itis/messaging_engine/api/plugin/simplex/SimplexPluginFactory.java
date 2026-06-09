package ru.itis.messaging_engine.api.plugin.simplex;

import ru.itis.messaging_engine.api.plugin.PluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

/**
 * Factory for creating a plugin for a simplex transport.
 */
@NotNullByDefault
public interface SimplexPluginFactory extends PluginFactory<SimplexPlugin> {
}
