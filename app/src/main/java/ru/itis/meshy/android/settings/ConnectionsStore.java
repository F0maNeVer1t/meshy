package ru.itis.meshy.android.settings;

import static ru.itis.messaging_engine.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static ru.itis.messaging_engine.api.plugin.TorConstants.PREF_TOR_MOBILE;
import static ru.itis.messaging_engine.api.plugin.TorConstants.PREF_TOR_NETWORK;
import static ru.itis.messaging_engine.api.plugin.TorConstants.PREF_TOR_ONLY_WHEN_CHARGING;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_BLUETOOTH;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_TOR_ENABLE;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_TOR_MOBILE_DATA;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_TOR_NETWORK;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_TOR_ONLY_WHEN_CHARGING;
import static ru.itis.meshy.android.settings.ConnectionsFragment.PREF_KEY_WIFI;

import androidx.annotation.Nullable;

import ru.itis.messaging_engine.api.settings.SettingsManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

@NotNullByDefault
class ConnectionsStore extends SettingsStore {

	ConnectionsStore(
			SettingsManager settingsManager,
			Executor dbExecutor,
			String namespace) {
		super(settingsManager, dbExecutor, namespace);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		String newKey;
		// translate between Android UI pref keys and bramble keys
		switch (key) {
			case PREF_KEY_BLUETOOTH:
			case PREF_KEY_WIFI:
			case PREF_KEY_TOR_ENABLE:
				newKey = PREF_PLUGIN_ENABLE;
				break;
			case PREF_KEY_TOR_MOBILE_DATA:
				newKey = PREF_TOR_MOBILE;
				break;
			case PREF_KEY_TOR_ONLY_WHEN_CHARGING:
				newKey = PREF_TOR_ONLY_WHEN_CHARGING;
				break;
			default:
				throw new AssertionError();
		}
		super.putBoolean(newKey, value);
	}

	@Override
	public void putString(String key, @Nullable String value) {
		// translate between Android UI pref keys and bramble keys
		if (key.equals(PREF_KEY_TOR_NETWORK)) {
			super.putString(PREF_TOR_NETWORK, value);
		} else {
			throw new AssertionError(key);
		}
	}

}
