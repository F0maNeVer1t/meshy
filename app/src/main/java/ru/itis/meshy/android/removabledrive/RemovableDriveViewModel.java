package ru.itis.meshy.android.removabledrive;

import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;
import static ru.itis.messaging_engine.api.plugin.file.RemovableDriveConstants.PROP_URI;
import static java.util.Locale.US;
import static java.util.Objects.requireNonNull;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.UiThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.Consumer;
import ru.itis.messaging_engine.api.account.AccountManager;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveManager;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveTask;
import ru.itis.messaging_engine.api.plugin.file.RemovableDriveTask.State;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.inject.Inject;

@UiThread
@NotNullByDefault
class RemovableDriveViewModel extends DbViewModel {

	enum Action {SEND, RECEIVE}

	private final AccountManager accountManager;
	private final RemovableDriveManager manager;

	private final MutableLiveEvent<Action> action = new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> oldTaskResumed =
			new MutableLiveEvent<>();
	private final MutableLiveData<TransferDataState> state =
			new MutableLiveData<>();
	@Nullable
	private ContactId contactId = null;
	@Nullable
	private RemovableDriveTask task = null;
	@Nullable
	private Consumer<State> taskObserver = null;

	@Inject
	RemovableDriveViewModel(
			Application app,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			AccountManager accountManager,
			RemovableDriveManager removableDriveManager) {
		super(app, dbExecutor, lifecycleManager, db, androidExecutor);
		this.accountManager = accountManager;
		this.manager = removableDriveManager;
	}

	@Override
	protected void onCleared() {
		if (task != null) {
			// when we have a task, we must have an observer for it
			Consumer<State> observer = requireNonNull(taskObserver);
			task.removeObserver(observer);
		}
	}

	@UiThread
	boolean hasNoState() {
		return action.getLastValue() == null && state.getValue() == null &&
				task == null;
	}

	/**
	 * Set this as soon as it becomes available.
	 */
	void setContactId(ContactId contactId) {
		this.contactId = contactId;
	}

	@UiThread
	void startSendData() {
		action.setEvent(Action.SEND);

		// check if there is already a send/write task
		task = manager.getCurrentWriterTask();
		if (task == null) {
			// check if there's even something to send
			ContactId c = requireNonNull(contactId);
			runOnDbThread(() -> {
				try {
					if (!manager.isTransportSupportedByContact(c)) {
						state.postValue(new TransferDataState.NotSupported());
					} else if (manager.isWriterTaskNeeded(c)) {
						state.postValue(new TransferDataState.Ready());
					} else {
						state.postValue(new TransferDataState.NoDataToSend());
					}
				} catch (DbException e) {
					handleException(e);
				}
			});
		} else {
			// observe old task
			taskObserver =
					s -> state.setValue(new TransferDataState.TaskAvailable(s));
			task.addObserver(taskObserver);
			oldTaskResumed.setEvent(true);
		}
	}

	@UiThread
	void startReceiveData() {
		action.setEvent(Action.RECEIVE);

		// check if there is already a receive/read task
		task = manager.getCurrentReaderTask();
		if (task == null) {
			state.setValue(new TransferDataState.Ready());
		} else {
			// observe old task
			taskObserver =
					s -> state.setValue(new TransferDataState.TaskAvailable(s));
			task.addObserver(taskObserver);
			oldTaskResumed.setEvent(true);
		}
	}

	String getFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", US);
		return sdf.format(new Date());
	}

	/**
	 * Call this only when in {@link TransferDataState.Ready}.
	 */
	@UiThread
	void exportData(Uri uri) {
		// starting an action more than once is not supported for simplicity
		if (task != null) throw new IllegalStateException();

		// from now on, we are not re-usable
		// (because gets a state update right away on the UiThread)
		taskObserver =
				s -> state.setValue(new TransferDataState.TaskAvailable(s));

		// start the writer task for this contact and observe it
		TransportProperties p = new TransportProperties();
		p.put(PROP_URI, uri.toString());
		ContactId c = requireNonNull(contactId);
		task = manager.startWriterTask(c, p);
		task.addObserver(taskObserver);
	}

	/**
	 * Call this only when in {@link TransferDataState.Ready}.
	 */
	@UiThread
	void importData(Uri uri) {
		// starting an action more than once is not supported for simplicity
		if (task != null) throw new IllegalStateException();

		// from now on, we are not re-usable
		// (because gets a state update right away on the UiThread)
		taskObserver =
				s -> state.setValue(new TransferDataState.TaskAvailable(s));

		TransportProperties p = new TransportProperties();
		p.put(PROP_URI, uri.toString());
		task = manager.startReaderTask(p);
		task.addObserver(taskObserver);
	}

	LiveEvent<Action> getActionEvent() {
		return action;
	}

	LiveEvent<Boolean> getOldTaskResumedEvent() {
		return oldTaskResumed;
	}

	LiveData<TransferDataState> getState() {
		return state;
	}

	boolean isAccountSignedIn() {
		return accountManager.hasDatabaseKey() &&
				lifecycleManager.getLifecycleState().isAfter(STARTING_SERVICES);
	}
}
