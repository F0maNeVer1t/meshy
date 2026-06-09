package ru.itis.meshy.android.hotspot;

import static ru.itis.messaging_engine.util.IoUtils.copyAndClose;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static ru.itis.meshy.BuildConfig.VERSION_NAME;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.R;
import ru.itis.meshy.android.hotspot.HotspotManager.HotspotListener;
import ru.itis.meshy.android.hotspot.HotspotState.HotspotError;
import ru.itis.meshy.android.hotspot.HotspotState.HotspotStarted;
import ru.itis.meshy.android.hotspot.HotspotState.NetworkConfig;
import ru.itis.meshy.android.hotspot.HotspotState.StartingHotspot;
import ru.itis.meshy.android.hotspot.HotspotState.WebsiteConfig;
import ru.itis.meshy.android.hotspot.WebServerManager.WebServerListener;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import ru.itis.meshy.api.android.AndroidNotificationManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
class HotspotViewModel extends DbViewModel
		implements HotspotListener, WebServerListener {

	private static final Logger LOG =
			getLogger(HotspotViewModel.class.getName());

	@IoExecutor
	private final Executor ioExecutor;
	private final AndroidNotificationManager notificationManager;
	private final HotspotManager hotspotManager;
	private final WebServerManager webServerManager;

	private final MutableLiveData<HotspotState> state =
			new MutableLiveData<>();
	private final MutableLiveData<Integer> peersConnected =
			new MutableLiveData<>();
	private final MutableLiveEvent<Uri> savedApkToUri =
			new MutableLiveEvent<>();

	@Nullable
	// Field to temporarily store the network config received via onHotspotStarted()
	// in order to post it along with a HotspotStarted status
	private volatile NetworkConfig networkConfig;

	@Inject
	HotspotViewModel(Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			@IoExecutor Executor ioExecutor,
			HotspotManager hotspotManager,
			WebServerManager webServerManager,
			AndroidNotificationManager notificationManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.ioExecutor = ioExecutor;
		this.notificationManager = notificationManager;
		this.hotspotManager = hotspotManager;
		this.hotspotManager.setHotspotListener(this);
		this.webServerManager = webServerManager;
		this.webServerManager.setListener(this);
	}

	@UiThread
	void startHotspot() {
		HotspotState s = state.getValue();
		if (s instanceof HotspotStarted) {
			// This can happen if the user navigates back to intro fragment and
			// taps 'start sharing' again. In this case, don't try to start the
			// hotspot again. Instead, just create a new, unconsumed HotspotStarted
			// event with the same config.
			HotspotStarted old = (HotspotStarted) s;
			state.setValue(new HotspotStarted(old.getNetworkConfig(),
					old.getWebsiteConfig()));
		} else {
			hotspotManager.startWifiP2pHotspot();
			notificationManager.showHotspotNotification();
		}
	}

	@UiThread
	private void stopHotspot() {
		ioExecutor.execute(webServerManager::stopWebServer);
		hotspotManager.stopWifiP2pHotspot();
		notificationManager.clearHotspotNotification();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		stopHotspot();
	}

	@Override
	public void onStartingHotspot() {
		state.setValue(new StartingHotspot());
	}

	@Override
	@IoExecutor
	public void onHotspotStarted(NetworkConfig networkConfig) {
		this.networkConfig = networkConfig;
		LOG.info("starting webserver");
		webServerManager.startWebServer();
	}

	@UiThread
	@Override
	public void onPeersUpdated(int peers) {
		peersConnected.setValue(peers);
	}

	@Override
	public void onHotspotError(String error) {
		if (LOG.isLoggable(WARNING)) {
			LOG.warning("Hotspot error: " + error);
		}
		state.postValue(new HotspotError(error));
		ioExecutor.execute(webServerManager::stopWebServer);
		notificationManager.clearHotspotNotification();
	}

	@Override
	@IoExecutor
	public void onWebServerStarted(WebsiteConfig websiteConfig) {
		NetworkConfig nc = requireNonNull(networkConfig);
		state.postValue(new HotspotStarted(nc, websiteConfig));
		networkConfig = null;
	}

	@Override
	@IoExecutor
	public void onWebServerError() {
		state.postValue(new HotspotError(getApplication()
				.getString(R.string.hotspot_error_web_server_start)));
		stopHotspot();
	}

	void exportApk(Uri uri) {
		try {
			OutputStream out = getApplication().getContentResolver()
					.openOutputStream(uri, "wt");
			writeApk(out, uri);
		} catch (FileNotFoundException e) {
			handleException(e);
		}
	}

    static String getApkFileName() {
        return "meshy-" + VERSION_NAME + ".apk";
    }

	private void writeApk(OutputStream out, Uri uriToShare) {
		File apk = new File(getApplication().getPackageCodePath());
		ioExecutor.execute(() -> {
			try {
				FileInputStream in = new FileInputStream(apk);
				copyAndClose(in, out);
				savedApkToUri.postEvent(uriToShare);
			} catch (IOException e) {
				handleException(e);
			}
		});
	}

	LiveData<HotspotState> getState() {
		return state;
	}

	LiveData<Integer> getPeersConnectedEvent() {
		return peersConnected;
	}

	LiveEvent<Uri> getSavedApkToUri() {
		return savedApkToUri;
	}

}
