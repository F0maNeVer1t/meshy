package ru.itis.meshy.android.contact.add.remote;

import static ru.itis.messaging_engine.api.contact.PendingContactState.OFFLINE;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.Pair;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.contact.PendingContact;
import ru.itis.messaging_engine.api.contact.PendingContactId;
import ru.itis.messaging_engine.api.contact.PendingContactState;
import ru.itis.messaging_engine.api.contact.event.PendingContactRemovedEvent;
import ru.itis.messaging_engine.api.contact.event.PendingContactStateChangedEvent;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.rendezvous.RendezvousPoller;
import ru.itis.messaging_engine.api.rendezvous.event.RendezvousPollEvent;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
public class PendingContactListViewModel extends DbViewModel
		implements EventListener {

	private final ContactManager contactManager;
	private final RendezvousPoller rendezvousPoller;
	private final EventBus eventBus;

	private final MutableLiveData<Collection<PendingContactItem>>
			pendingContacts = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasInternetConnection =
			new MutableLiveData<>();

	@Inject
	PendingContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor,
			ContactManager contactManager,
			RendezvousPoller rendezvousPoller,
			EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
		this.rendezvousPoller = rendezvousPoller;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}

	void onCreate() {
		if (pendingContacts.getValue() == null) loadPendingContacts();
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactStateChangedEvent ||
				e instanceof PendingContactRemovedEvent ||
				e instanceof RendezvousPollEvent) {
			loadPendingContacts();
		}
	}

	private void loadPendingContacts() {
		runOnDbThread(() -> {
			try {
				Collection<Pair<PendingContact, PendingContactState>> pairs =
						contactManager.getPendingContacts();
				List<PendingContactItem> items = new ArrayList<>(pairs.size());
				boolean online = pairs.isEmpty();
				for (Pair<PendingContact, PendingContactState> pair : pairs) {
					PendingContact p = pair.getFirst();
					PendingContactState state = pair.getSecond();
					long lastPoll = rendezvousPoller.getLastPollTime(p.getId());
					items.add(new PendingContactItem(p, state, lastPoll));
					online = online || state != OFFLINE;
				}
				pendingContacts.postValue(items);
				hasInternetConnection.postValue(online);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Collection<PendingContactItem>> getPendingContacts() {
		return pendingContacts;
	}

	void removePendingContact(PendingContactId id) {
		runOnDbThread(() -> {
			try {
				contactManager.removePendingContact(id);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	LiveData<Boolean> getHasInternetConnection() {
		return hasInternetConnection;
	}

}
