package ru.itis.meshy.android.introduction;

import static android.widget.Toast.LENGTH_SHORT;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.meshy.R;
import ru.itis.messaging_engine.api.connection.ConnectionRegistry;
import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.event.EventBus;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.contact.ContactItem;
import ru.itis.meshy.android.contact.ContactsViewModel;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import ru.itis.meshy.api.conversation.ConversationManager;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.identity.AuthorManager;
import ru.itis.meshy.api.introduction.IntroductionManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
class IntroductionViewModel extends ContactsViewModel {

	private static final Logger LOG =
			getLogger(IntroductionViewModel.class.getName());

	private final ContactManager contactManager;
	private final AuthorManager authorManager;
	private final IntroductionManager introductionManager;

	@Inject
	IntroductionViewModel(Application application,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, TransactionManager db,
			AndroidExecutor androidExecutor, ContactManager contactManager,
			AuthorManager authorManager,
			ConversationManager conversationManager,
			ConnectionRegistry connectionRegistry, EventBus eventBus,
			IntroductionManager introductionManager) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor,
				contactManager, authorManager, conversationManager,
				connectionRegistry, eventBus);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
		this.introductionManager = introductionManager;
	}

	/*
	 * This is the contact from whose conversation we started the introduction
	 * using the menu item.
	 */
	@Nullable
	private ContactId firstContactId;
	/*
	 * This is the contact we selected from the list of contacts as a second
	 * contact for the introduction.
	 */
	@Nullable
	private ContactId secondContactId;

	private final MutableLiveEvent<Boolean> secondContactSelected =
			new MutableLiveEvent<>();

	private final MutableLiveData<IntroductionInfo> introductionInfo =
			new MutableLiveData<>();

	void setFirstContactId(ContactId contactId) {
		this.firstContactId = contactId;
		loadContacts();
	}

	@Nullable
	ContactId getSecondContactId() {
		return secondContactId;
	}

	void setSecondContactId(ContactId contactId) {
		secondContactId = contactId;
		// Setting this to null here so that IntroductionMessageFragment can
		// tell whether the correct value has been loaded from the database when
		// selecting a second contact repeatedly.
		introductionInfo.setValue(null);
		loadIntroductionInfo();
	}

	/**
	 * Trigger the event that the second contact has been selected from the
	 * contact list by the user.
	 */
	void triggerContactSelected() {
		secondContactSelected.setEvent(true);
	}

	/**
	 * This event will be triggered once the second contact has been selected
	 * from the list of contacts displayed. It is not fired when the second
	 * contact gets restored from the saved instance state.
	 */
	LiveEvent<Boolean> getSecondContactSelected() {
		return secondContactSelected;
	}

	/**
	 * Holder for the introduction info object with data about both contacts
	 * and whether the introduction is possible. May wrap null if the data
	 * is not available yet. This happens when it is reset by selecting a
	 * contact with the same view model instance more than once.
	 */
	LiveData<IntroductionInfo> getIntroductionInfo() {
		return introductionInfo;
	}

	@Override
	protected boolean displayContact(ContactId contactId) {
		return !requireNonNull(firstContactId).equals(contactId);
	}

	private void loadIntroductionInfo() {
		final ContactId firstContactId = requireNonNull(this.firstContactId);
		final ContactId secondContactId = requireNonNull(this.secondContactId);
		runOnDbThread(() -> {
			try {
				Contact firstContact =
						contactManager.getContact(firstContactId);
				Contact secondContact =
						contactManager.getContact(secondContactId);
				AuthorInfo a1 = authorManager.getAuthorInfo(firstContact);
				AuthorInfo a2 = authorManager.getAuthorInfo(secondContact);
				boolean possible = introductionManager
						.canIntroduce(firstContact, secondContact);
				ContactItem c1 = new ContactItem(firstContact, a1);
				ContactItem c2 = new ContactItem(secondContact, a2);
				introductionInfo.postValue(
						new IntroductionInfo(c1, c2, possible));
			} catch (DbException e) {
				handleException(e);
			}
		});
	}

	void makeIntroduction(@Nullable String text) {
		final IntroductionInfo info =
				requireNonNull(introductionInfo.getValue());
		runOnDbThread(() -> {
			// actually make the introduction
			try {
				introductionManager.makeIntroduction(
						info.getContact1().getContact(),
						info.getContact2().getContact(), text);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				androidExecutor.runOnUiThread(() -> Toast.makeText(
						getApplication(), R.string.introduction_error,
						LENGTH_SHORT).show());
			}
		});
	}

}
