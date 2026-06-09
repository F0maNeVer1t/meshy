package ru.itis.meshy.android.login;

import static ru.itis.messaging_engine.api.crypto.DecryptionResult.SUCCESS;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.COMPACTING_DATABASE;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.MIGRATING_DATABASE;
import static ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState.STARTING_SERVICES;

import android.app.Application;

import androidx.annotation.UiThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.account.AccountManager;
import ru.itis.messaging_engine.api.crypto.DecryptionException;
import ru.itis.messaging_engine.api.crypto.DecryptionResult;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager.LifecycleState;
import ru.itis.messaging_engine.api.lifecycle.event.LifecycleEvent;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import ru.itis.meshy.api.android.AndroidNotificationManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
public class StartupViewModel extends AndroidViewModel
		implements EventListener {

	enum State {SIGNED_OUT, SIGNED_IN, STARTING, MIGRATING, COMPACTING, STARTED}

	private final AccountManager accountManager;
	private final AndroidNotificationManager notificationManager;
	private final EventBus eventBus;
	@IoExecutor
	private final Executor ioExecutor;

	private final MutableLiveEvent<DecryptionResult> passwordValidated =
			new MutableLiveEvent<>();
	private final MutableLiveEvent<Boolean> accountDeleted =
			new MutableLiveEvent<>();
	private final MutableLiveData<State> state = new MutableLiveData<>();

	@Inject
	StartupViewModel(Application app,
			AccountManager accountManager,
			LifecycleManager lifecycleManager,
			AndroidNotificationManager notificationManager,
			EventBus eventBus,
			@IoExecutor Executor ioExecutor) {
		super(app);
		this.accountManager = accountManager;
		this.notificationManager = notificationManager;
		this.eventBus = eventBus;
		this.ioExecutor = ioExecutor;

		updateState(lifecycleManager.getLifecycleState());
		eventBus.addListener(this);
	}

	@Override
	protected void onCleared() {
		eventBus.removeListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof LifecycleEvent) {
			LifecycleState s = ((LifecycleEvent) e).getLifecycleState();
			updateState(s);
		}
	}

	@UiThread
	private void updateState(LifecycleState s) {
		if (accountManager.hasDatabaseKey()) {
			if (s.isAfter(STARTING_SERVICES)) state.setValue(State.STARTED);
			else if (s == MIGRATING_DATABASE) state.setValue(State.MIGRATING);
			else if (s == COMPACTING_DATABASE) state.setValue(State.COMPACTING);
			else state.setValue(State.STARTING);
		} else {
			state.setValue(State.SIGNED_OUT);
		}
	}

	boolean accountExists() {
		return accountManager.accountExists();
	}

	void clearSignInNotification() {
		notificationManager.blockSignInNotification();
		notificationManager.clearSignInNotification();
	}

	void validatePassword(String password) {
		ioExecutor.execute(() -> {
			try {
				accountManager.signIn(password);
				passwordValidated.postEvent(SUCCESS);
				state.postValue(State.SIGNED_IN);
			} catch (DecryptionException e) {
				passwordValidated.postEvent(e.getDecryptionResult());
			}
		});
	}

	LiveEvent<DecryptionResult> getPasswordValidated() {
		return passwordValidated;
	}

	LiveEvent<Boolean> getAccountDeleted() {
		return accountDeleted;
	}

	LiveData<State> getState() {
		return state;
	}

	@UiThread
	void deleteAccount() {
		accountManager.deleteAccount();
		accountDeleted.setEvent(true);
	}

}
