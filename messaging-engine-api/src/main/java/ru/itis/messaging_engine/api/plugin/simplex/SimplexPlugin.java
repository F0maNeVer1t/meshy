package ru.itis.messaging_engine.api.plugin.simplex;

import ru.itis.messaging_engine.api.plugin.Plugin;
import ru.itis.messaging_engine.api.plugin.TransportConnectionReader;
import ru.itis.messaging_engine.api.plugin.TransportConnectionWriter;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import ru.itis.messaging_engine.api.system.Wakeful;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

/**
 * An interface for transport plugins that support simplex communication.
 */
@NotNullByDefault
public interface SimplexPlugin extends Plugin {

	/**
	 * Returns true if the transport is likely to lose streams and the cost of
	 * transmitting redundant copies of data is cheap.
	 */
	boolean isLossyAndCheap();

	/**
	 * Attempts to create and return a reader for the given transport
	 * properties. Returns null if a reader cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionReader createReader(TransportProperties p);

	/**
	 * Attempts to create and return a writer for the given transport
	 * properties. Returns null if a writer cannot be created.
	 */
	@Wakeful
	@Nullable
	TransportConnectionWriter createWriter(TransportProperties p);
}
