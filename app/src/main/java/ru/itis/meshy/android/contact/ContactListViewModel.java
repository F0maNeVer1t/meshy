package ru.itis.meshy.android.contact;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.contact.event.PendingContactAddedEvent;
import ru.itis.messaging_engine.api.contact.event.PendingContactRemovedEvent;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.api.android.AndroidNotificationManager;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.identity.AuthorManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
class ContactListViewModel extends ContactsViewModel {

	private final AndroidNotificationManager notificationManager;

	private final MutableLiveData<Boolean> hasPendingContacts =
			new MutableLiveData<>();

	@Inject
	ContactListViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus,
			AndroidNotificationManager notificationManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				contactManager, authorManager, conversationManager,
				connectionRegistry, eventBus);
		this.notificationManager = notificationManager;
	}

	@Override
	public void eventOccurred(Event e) {
		super.eventOccurred(e);
		if (e instanceof PendingContactAddedEvent ||
				e instanceof PendingContactRemovedEvent) {
			checkForPendingContacts();
		}
	}

	LiveData<Boolean> getHasPendingContacts() {
		return hasPendingContacts;
	}

	void checkForPendingContacts() {
		runOnDbThread(() -> {
			try {
				boolean hasPending =
						!contactManager.getPendingContacts().isEmpty();
				hasPendingContacts.postValue(hasPending);
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	void clearAllContactNotifications() {
		notificationManager.clearAllContactNotifications();
	}

	void clearAllContactAddedNotifications() {
		notificationManager.clearAllContactAddedNotifications();
	}

}
