package ru.itis.messaging_engine.api.network;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface NetworkManager {

	NetworkStatus getNetworkStatus();
}
