package ru.itis.meshy.android.contact.add.remote;

import static ru.itis.messaging_engine.api.contact.HandshakeLinkConstants.LINK_REGEX;
import static ru.itis.messaging_engine.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.UnsupportedVersionException;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.contact.PendingContact;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.NoSuchPendingContactException;
import ru.itis.messaging_engine.api.db.TransactionManager;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.system.AndroidExecutor;
import ru.itis.meshy.android.viewmodel.DbViewModel;
import ru.itis.meshy.android.viewmodel.LiveEvent;
import ru.itis.meshy.android.viewmodel.LiveResult;
import ru.itis.meshy.android.viewmodel.MutableLiveEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
public class AddContactViewModel extends DbViewModel {

	private final static Logger LOG =
			getLogger(AddContactViewModel.class.getName());

	private final ContactManager contactManager;

	private final MutableLiveData<String> handshakeLink =
			new MutableLiveData<>();
	private final MutableLiveEvent<Boolean> remoteLinkEntered =
			new MutableLiveEvent<>();
	private final MutableLiveData<LiveResult<Boolean>> addContactResult =
			new MutableLiveData<>();
	@Nullable
	private String remoteHandshakeLink;

	@Inject
	AddContactViewModel(Application application,
			ContactManager contactManager,
			@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager,
			TransactionManager db,
			AndroidExecutor androidExecutor) {
		super(application, dbExecutor, lifecycleManager, db, androidExecutor);
		this.contactManager = contactManager;
	}

	void onCreate() {
		if (handshakeLink.getValue() == null) loadHandshakeLink();
	}

	private void loadHandshakeLink() {
		runOnDbThread(() -> {
			try {
				handshakeLink.postValue(contactManager.getHandshakeLink());
			} catch (DbException e) {
				handleException(e);
				// the UI should stay disabled in this case,
				// leaving the user unable to proceed
			}
		});
	}

	LiveData<String> getHandshakeLink() {
		return handshakeLink;
	}

	@Nullable
	String getRemoteHandshakeLink() {
		return remoteHandshakeLink;
	}

	void setRemoteHandshakeLink(String link) {
		remoteHandshakeLink = link;
	}

	boolean isValidRemoteContactLink(@Nullable CharSequence link) {
		return link != null && LINK_REGEX.matcher(link).find();
	}

	LiveEvent<Boolean> getRemoteLinkEntered() {
		return remoteLinkEntered;
	}

	void onRemoteLinkEntered() {
		if (remoteHandshakeLink == null) throw new IllegalStateException();
		remoteLinkEntered.setEvent(true);
	}

	void addContact(String nickname) {
		if (remoteHandshakeLink == null) throw new IllegalStateException();
		runOnDbThread(() -> {
			try {
				contactManager.addPendingContact(remoteHandshakeLink, nickname);
				addContactResult.postValue(new LiveResult<>(true));
			} catch (UnsupportedVersionException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			} catch (DbException | FormatException
					| GeneralSecurityException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			}
		});
	}

	LiveData<LiveResult<Boolean>> getAddContactResult() {
		return addContactResult;
	}

	void updatePendingContact(String name, PendingContact p) {
		runOnDbThread(() -> {
			try {
				contactManager.removePendingContact(p.getId());
				addContact(name);
			} catch (NoSuchPendingContactException e) {
				logException(LOG, WARNING, e);
				// no error in UI as pending contact was converted into contact
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				addContactResult.postValue(new LiveResult<>(e));
			}
		});
	}

}
