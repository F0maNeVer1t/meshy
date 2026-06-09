package ru.itis.meshy.android.settings;

import static android.widget.Toast.LENGTH_LONG;
import static ru.itis.messaging_engine.util.AndroidUtils.getSupportedImageContentTypes;
import static ru.itis.messaging_engine.util.LogUtils.logDuration;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static ru.itis.messaging_engine.util.LogUtils.now;
import static ru.itis.meshy.android.settings.SecurityFragment.PREF_SCREEN_LOCK;
import static ru.itis.meshy.android.settings.SecurityFragment.PREF_SCREEN_LOCK_TIMEOUT;
import static ru.itis.meshy.android.settings.SettingsFragment.SETTINGS_NAMESPACE;
import static java.util.Arrays.asList;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.AnyThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.FeatureFlags;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.identity.IdentityManager;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.plugin.BluetoothConstants;
import ru.itis.messaging_engine.api.plugin.LanTcpConstants;
import ru.itis.messaging_engine.api.plugin.TorConstants;
import ru.itis.messaging_engine.api.settings.Settings;
import ru.itis.messaging_engine.api.settings.SettingsManager;
import ru.itis.messaging_engine.api.settings.event.SettingsUpdatedEvent;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.R;
import ru.itis.meshy.android.attachment.UnsupportedMimeTypeException;
import ru.itis.meshy.android.attachment.media.ImageCompressor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.api.avatar.AvatarManager;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.identity.AuthorManager;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;
import org.briarproject.onionwrapper.CircumventionProvider;
import org.briarproject.onionwrapper.LocationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class SettingsViewModel extends DbViewModel implements EventListener {

	private final static Logger LOG =
			getLogger(SettingsViewModel.class.getName());

	static final String BT_NAMESPACE =
			BluetoothConstants.ID.getString();
	static final String WIFI_NAMESPACE = LanTcpConstants.ID.getString();
	static final String TOR_NAMESPACE = TorConstants.ID.getString();

	private final SettingsManager settingsManager;
	private final IdentityManager identityManager;
	private final EventBus eventBus;
	private final AvatarManager avatarManager;
	private final AuthorManager authorManager;
	private final ImageCompressor imageCompressor;
	private final Executor ioExecutor;
	private final FeatureFlags featureFlags;

	final SettingsStore settingsStore;
	final TorSummaryProvider torSummaryProvider;
	final ConnectionsManager connectionsManager;
	final NotificationsManager notificationsManager;

	private volatile Settings settings;

	private final MutableLiveData<OwnIdentityInfo> ownIdentityInfo =
			new MutableLiveData<>();
	private final MutableLiveData<Boolean> screenLockEnabled =
			new MutableLiveData<>();
	private final MutableLiveData<String> screenLockTimeout =
			new MutableLiveData<>();

	@Inject
	SettingsViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager,
			IdentityManager identityManager,
			EventBus eventBus,
			AvatarManager avatarManager,
			AuthorManager authorManager,
			ImageCompressor imageCompressor,
			LocationUtils locationUtils,
			CircumventionProvider circumventionProvider,
			@IoExecutor Executor ioExecutor,
			FeatureFlags featureFlags) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.settingsManager = settingsManager;
		this.identityManager = identityManager;
		this.eventBus = eventBus;
		this.imageCompressor = imageCompressor;
		this.avatarManager = avatarManager;
		this.authorManager = authorManager;
		this.ioExecutor = ioExecutor;
		this.featureFlags = featureFlags;
		settingsStore = new SettingsStore(settingsManager, dbExecutor,
				SETTINGS_NAMESPACE);
		torSummaryProvider = new TorSummaryProvider(getApplication(),
				locationUtils, circumventionProvider);
		connectionsManager =
				new ConnectionsManager(settingsManager, dbExecutor);
		notificationsManager = new NotificationsManager(getApplication(),
				settingsManager, dbExecutor);

		eventBus.addListener(this);
		loadSettings();
		if (shouldEnableProfilePictures()) loadOwnIdentityInfo();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	private void loadSettings() {
		runOnDbThread(() -> {
			try {
				long start = now();
				settings = settingsManager.getSettings(SETTINGS_NAMESPACE);
				updateSettings(settings);
				connectionsManager.updateBtSetting(
						settingsManager.getSettings(BT_NAMESPACE));
				connectionsManager.updateWifiSettings(
						settingsManager.getSettings(WIFI_NAMESPACE));
				connectionsManager.updateTorSettings(
						settingsManager.getSettings(TOR_NAMESPACE));
				logDuration(LOG, "Loading settings", start);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	boolean shouldEnableProfilePictures() {
		return featureFlags.shouldEnableProfilePictures();
	}

	private void loadOwnIdentityInfo() {
		runOnDbThread(() -> {
			try {
				LocalAuthor localAuthor = identityManager.getLocalAuthor();
				AuthorInfo authorInfo = authorManager.getMyAuthorInfo();
				ownIdentityInfo.postValue(
						new OwnIdentityInfo(localAuthor, authorInfo));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			String namespace = s.getNamespace();
			if (namespace.equals(SETTINGS_NAMESPACE)) {
				LOG.info("Settings updated");
				settings = s.getSettings();
				updateSettings(settings);
			} else if (namespace.equals(BT_NAMESPACE)) {
				LOG.info("Bluetooth settings updated");
				connectionsManager.updateBtSetting(s.getSettings());
			} else if (namespace.equals(WIFI_NAMESPACE)) {
				LOG.info("Wifi settings updated");
				connectionsManager.updateWifiSettings(s.getSettings());
			} else if (namespace.equals(TOR_NAMESPACE)) {
				LOG.info("Tor settings updated");
				connectionsManager.updateTorSettings(s.getSettings());
			}
		}
	}

	@AnyThread
	private void updateSettings(Settings settings) {
		screenLockEnabled.postValue(settings.getBoolean(PREF_SCREEN_LOCK,
				false));
		int defaultTimeout = Integer.parseInt(getApplication()
				.getString(R.string.pref_lock_timeout_value_default));
		screenLockTimeout.postValue(String.valueOf(
				settings.getInt(PREF_SCREEN_LOCK_TIMEOUT, defaultTimeout)
		));
		notificationsManager.updateSettings(settings);
	}

	void setAvatar(Uri uri) {
		ioExecutor.execute(() -> {
			try {
				trySetAvatar(uri);
			} catch (IOException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}

	private void trySetAvatar(Uri uri) throws IOException {
		ContentResolver contentResolver =
				getApplication().getContentResolver();
		String contentType = contentResolver.getType(uri);
		if (contentType == null) throw new IOException("null content type");
		if (!asList(getSupportedImageContentTypes()).contains(contentType)) {
			throw new UnsupportedMimeTypeException(contentType, uri);
		}
		InputStream is;
		try {
			is = contentResolver.openInputStream(uri);
			if (is == null) throw new IOException(
					"ContentResolver returned null when opening InputStream");
		} catch (SecurityException e) {
			throw new IOException(e);
		}
		InputStream compressed = imageCompressor.compressImage(is, contentType);

		runOnDbThread(() -> {
			try {
				avatarManager.addAvatar(ImageCompressor.MIME_TYPE, compressed);
				loadOwnIdentityInfo();
			} catch (IOException | DbException e) {
				logException(LOG, WARNING, e);
				onSetAvatarFailed();
			}
		});
	}

	@AnyThread
	private void onSetAvatarFailed() {
		androidExecutor.runOnUiThread(() -> Toast.makeText(getApplication(),
				R.string.change_profile_picture_failed_message, LENGTH_LONG)
				.show());
	}

	LiveData<OwnIdentityInfo> getOwnIdentityInfo() {
		return ownIdentityInfo;
	}

	LiveData<Boolean> getScreenLockEnabled() {
		return screenLockEnabled;
	}

	LiveData<String> getScreenLockTimeout() {
		return screenLockTimeout;
	}

}
