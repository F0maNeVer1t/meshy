package ru.itis.meshy.android.contact;

import static ru.itis.messaging_engine.util.LogUtils.logDuration;
import static ru.itis.messaging_engine.util.LogUtils.now;
import static java.util.logging.Logger.getLogger;

import android.app.Application;

import androidx.annotation.UiThread;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.contact.event.ContactAddedEvent;
import ru.itis.messaging_engine.api.contact.event.ContactAliasChangedEvent;
import ru.itis.messaging_engine.api.contact.event.ContactRemovedEvent;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.plugin.event.ContactConnectedEvent;
import ru.itis.messaging_engine.api.plugin.event.ContactDisconnectedEvent;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.android.viewmodel.LiveResult;
import ru.itis.meshy.api.avatar.event.AvatarUpdatedEvent;
import ru.itis.meshy.api.client.MessageTracker;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.conversation.event.ConversationMessageTrackedEvent;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.identity.AuthorManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
public class ContactsViewModel extends DbViewModel implements EventListener {

	private static final Logger LOG =
			getLogger(ContactsViewModel.class.getName());

	protected final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final ConversationManager conversationManager;
	private final ConnectionRegistry connectionRegistry;
	private final EventBus eventBus;

	private final MutableLiveData<LiveResult<List<ContactListItem>>>
			contactListItems = new MutableLiveData<>();

	@Inject
	public ContactsViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.conversationManager = conversationManager;
		this.connectionRegistry = connectionRegistry;
		this.eventBus = eventBus;
		this.eventBus.addListener(this);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		eventBus.removeListener(this);
	}

	protected void loadContacts() {
		loadFromDb(this::loadContacts, contactListItems::setValue);
	}

	private List<ContactListItem> loadContacts(Transaction txn)
			throws DbException {
		long start = now();
		List<ContactListItem> contacts = new ArrayList<>();
		for (Contact c : contactManager.getContacts(txn)) {
			ContactId id = c.getId();
			if (!displayContact(id)) {
				continue;
			}
			AuthorInfo authorInfo = authorManager.getAuthorInfo(txn, c);
			MessageTracker.GroupCount count =
					conversationManager.getGroupCount(txn, id);
			boolean connected = connectionRegistry.isConnected(c.getId());
			contacts.add(new ContactListItem(c, authorInfo, connected, count));
		}
		Collections.sort(contacts);
		logDuration(LOG, "Full load", start);
		return contacts;
	}

	/**
	 * Override this method to display only a subset of contacts.
	 */
	protected boolean displayContact(ContactId contactId) {
		return true;
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactAddedEvent) {
			LOG.info("Contact added, reloading");
			loadContacts();
		} else if (e instanceof ContactConnectedEvent) {
			updateItem(((ContactConnectedEvent) e).getContactId(),
					item -> new ContactListItem(item, true), false);
		} else if (e instanceof ContactDisconnectedEvent) {
			updateItem(((ContactDisconnectedEvent) e).getContactId(),
					item -> new ContactListItem(item, false), false);
		} else if (e instanceof ContactRemovedEvent) {
			LOG.info("Contact removed, removing item");
			removeItem(((ContactRemovedEvent) e).getContactId());
		} else if (e instanceof ConversationMessageTrackedEvent) {
			LOG.info("Conversation message tracked, updating item");
			ConversationMessageTrackedEvent p =
					(ConversationMessageTrackedEvent) e;
			long timestamp = p.getTimestamp();
			boolean read = p.getRead();
			updateItem(p.getContactId(),
					item -> new ContactListItem(item, timestamp, read), true);
		} else if (e instanceof AvatarUpdatedEvent) {
			AvatarUpdatedEvent a = (AvatarUpdatedEvent) e;
			updateItem(a.getContactId(), item -> new ContactListItem(item,
					a.getAttachmentHeader()), false);
		} else if (e instanceof ContactAliasChangedEvent) {
			ContactAliasChangedEvent c = (ContactAliasChangedEvent) e;
			updateItem(c.getContactId(),
					item -> new ContactListItem(item, c.getAlias()), false);
		}
	}

	public LiveData<LiveResult<List<ContactListItem>>> getContactListItems() {
		return contactListItems;
	}

	@UiThread
	private void updateItem(ContactId c,
			Function<ContactListItem, ContactListItem> replacer, boolean sort) {
		List<ContactListItem> list = updateListItems(getList(contactListItems),
				itemToTest -> itemToTest.getContact().getId().equals(c),
				replacer);
		if (list == null) return;
		if (sort) Collections.sort(list);
		contactListItems.setValue(new LiveResult<>(list));
	}

	@UiThread
	private void removeItem(ContactId c) {
		removeAndUpdateListItems(contactListItems,
				itemToTest -> itemToTest.getContact().getId().equals(c));
	}

}
