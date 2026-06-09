package ru.itis.meshy.android.mailbox;

import static ru.itis.messaging_engine.api.plugin.Plugin.State.ACTIVE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

import android.app.Application;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.zxing.Result;

import ru.itis.messaging_engine.api.Consumer;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.mailbox.MailboxManager;
import ru.itis.messaging_engine.api.mailbox.MailboxPairingState;
import ru.itis.messaging_engine.api.mailbox.MailboxPairingState.Paired;
import ru.itis.messaging_engine.api.mailbox.MailboxPairingTask;
import ru.itis.messaging_engine.api.mailbox.MailboxStatus;
import ru.itis.messaging_engine.api.mailbox.event.OwnMailboxConnectionStatusEvent;
import ru.itis.messaging_engine.api.plugin.Plugin;
import ru.itis.messaging_engine.api.plugin.PluginManager;
import ru.itis.messaging_engine.api.plugin.TorConstants;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.event.TransportInactiveEvent;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.mailbox.MailboxState.CameraError;
import ru.itis.meshy.android.mailbox.MailboxState.IsPaired;
import ru.itis.meshy.android.mailbox.MailboxState.NotSetup;
import ru.itis.meshy.android.mailbox.MailboxState.OfflineWhenPairing;
import ru.itis.meshy.android.mailbox.MailboxState.Pairing;
import ru.itis.meshy.android.mailbox.MailboxState.ScanningQrCode;
import ru.itis.meshy.android.mailbox.MailboxState.ShowDownload;
import ru.itis.meshy.android.mailbox.MailboxState.WasUnpaired;
import ru.itis.meshy.android.qrcode.QrCodeDecoder;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import ru.itis.meshy.api.android.AndroidNotificationManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
class MailboxViewModel extends DbViewModel
		implements QrCodeDecoder.ResultCallback, Consumer<MailboxPairingState>,
		EventListener {

	private static final Logger LOG =
			getLogger(MailboxViewModel.class.getName());

	private final EventBus eventBus;
	private final Executor ioExecutor;
	private final QrCodeDecoder qrCodeDecoder;
	private final PluginManager pluginManager;
	private final MailboxManager mailboxManager;
	private final AndroidNotificationManager notificationManager;

	private final MutableLiveEvent<MailboxState> pairingState =
			new MutableLiveEvent<>();
	private final MutableLiveData<MailboxStatus> status =
			new MutableLiveData<>();
	@Nullable
	private MailboxPairingTask pairingTask = null;

	@Inject
	MailboxViewModel(
			Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			EventBus eventBus,
			@IoExecutor Executor ioExecutor,
			PluginManager pluginManager,
			MailboxManager mailboxManager,
			AndroidNotificationManager notificationManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.eventBus = eventBus;
		this.ioExecutor = ioExecutor;
		this.pluginManager = pluginManager;
		this.mailboxManager = mailboxManager;
		this.notificationManager = notificationManager;
		qrCodeDecoder = new QrCodeDecoder(androidExecutor, ioExecutor, this);
		eventBus.addListener(this);
		checkIfSetup();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
		MailboxPairingTask task = pairingTask;
		if (task != null) {
			task.removeObserver(this);
			pairingTask = null;
		}
	}

	@UiThread
	private void checkIfSetup() {
		MailboxPairingTask task = mailboxManager.getCurrentPairingTask();
		if (task == null) {
			runOnDbThread(true, txn -> {
				boolean isPaired = mailboxManager.isPaired(txn);
				if (isPaired) {
					MailboxStatus mailboxStatus =
							mailboxManager.getMailboxStatus(txn);
					boolean isOnline = isTorActive();
					pairingState.postEvent(new IsPaired(isOnline));
					status.postValue(mailboxStatus);
				} else {
					pairingState.postEvent(new NotSetup());
				}
			}, this::handleException);
		} else {
			task.addObserver(this);
			pairingTask = task;
		}
	}

	@UiThread
	@Override
	public void eventOccurred(Event e) {
		if (e instanceof OwnMailboxConnectionStatusEvent) {
			MailboxStatus status =
					((OwnMailboxConnectionStatusEvent) e).getStatus();
			this.status.setValue(status);
		} else if (e instanceof TransportInactiveEvent) {
			TransportId id = ((TransportInactiveEvent) e).getTransportId();
			if (!TorConstants.ID.equals(id)) return;
			onTorInactive();
		}
	}

	@UiThread
	private void onTorInactive() {
		MailboxState lastState = pairingState.getLastValue();
		if (lastState instanceof IsPaired) {
			// we are already paired, so use IsPaired state
			pairingState.setEvent(new IsPaired(false));
		} else if (lastState instanceof Pairing) {
			Pairing p = (Pairing) lastState;
			// check that we not just finished pairing (showing success screen)
			if (!(p.pairingState instanceof Paired)) {
				pairingState.setEvent(new OfflineWhenPairing());
			}
			// else ignore offline event as user will be leaving UI flow anyway
		}
	}

	@UiThread
	void onScanButtonClicked() {
		if (isTorActive()) {
			pairingState.setEvent(new ScanningQrCode());
		} else {
			pairingState.setEvent(new OfflineWhenPairing());
		}
	}

	@UiThread
	void onCameraError() {
		pairingState.setEvent(new CameraError());
	}

	@Override
	@IoExecutor
	public void onQrCodeDecoded(Result result) {
		LOG.info("Got result from decoder");
		onQrCodePayloadReceived(result.getText());
	}

	@AnyThread
	private void onQrCodePayloadReceived(String qrCodePayload) {
		if (isTorActive()) {
			pairingTask = mailboxManager.startPairingTask(qrCodePayload);
			pairingTask.addObserver(this);
		} else {
			pairingState.postEvent(new OfflineWhenPairing());
		}
	}

	@UiThread
	@Override
	public void accept(MailboxPairingState mailboxPairingState) {
		if (LOG.isLoggable(INFO)) {
			LOG.info("New pairing state: " +
					mailboxPairingState.getClass().getSimpleName());
		}
		pairingState.setEvent(new Pairing(mailboxPairingState));
	}

	private boolean isTorActive() {
		Plugin plugin = pluginManager.getPlugin(TorConstants.ID);
		return plugin != null && plugin.getState() == ACTIVE;
	}

	@UiThread
	void showDownloadFragment() {
		pairingState.setEvent(new ShowDownload());
	}

	@UiThread
	QrCodeDecoder getQrCodeDecoder() {
		return qrCodeDecoder;
	}

	@UiThread
	void checkIfOnlineWhenPaired() {
		boolean isOnline = isTorActive();
		pairingState.setEvent(new IsPaired(isOnline));
	}

	LiveData<Boolean> checkConnection() {
		MutableLiveData<Boolean> liveData = new MutableLiveData<>();
		checkConnection(liveData::postValue);
		return liveData;
	}

	void checkConnectionFromWizard() {
		checkConnection(success -> {
			boolean isOnline = isTorActive();
			// make UI move back to status fragment by changing pairingState
			pairingState.postEvent(new IsPaired(isOnline));
		});
	}

	private void checkConnection(@IoExecutor Consumer<Boolean> consumer) {
		ioExecutor.execute(() -> {
			boolean success = mailboxManager.checkConnection();
			if (LOG.isLoggable(INFO)) {
				LOG.info("Got result from connection check: " + success);
			}
			consumer.accept(success);
		});
	}

	@UiThread
	void unlink() {
		ioExecutor.execute(() -> {
			try {
				boolean wasWiped = mailboxManager.unPair();
				pairingState.postEvent(new WasUnpaired(!wasWiped));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	void clearProblemNotification() {
		notificationManager.clearMailboxProblemNotification();
	}

	@UiThread
	LiveEvent<MailboxState> getPairingState() {
		return pairingState;
	}

	@UiThread
	LiveData<MailboxStatus> getStatus() {
		return status;
	}
}
