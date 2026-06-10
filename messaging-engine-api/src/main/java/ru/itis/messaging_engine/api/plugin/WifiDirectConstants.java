package ru.itis.messaging_engine.api.plugin;

public interface WifiDirectConstants {

	TransportId ID = new TransportId("ru.itis.meshy.wifidirect");

	// Transport properties
	String PROP_DEVICE_ADDRESS = "deviceAddress";
	String PROP_GROUP_OWNER_ADDRESS = "goAddress";
	String PROP_GROUP_OWNER_PORT = "goPort";

	// Local settings
	String PREF_EVER_CONNECTED = "wifiDirectEverConnected";

	// Defaults
	boolean DEFAULT_PREF_PLUGIN_ENABLE = false;
	boolean DEFAULT_PREF_EVER_CONNECTED = false;

	int SERVER_PORT = 8730;
}
