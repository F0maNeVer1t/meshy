package ru.itis.meshy.android.contactselection;

import static ru.itis.messaging_engine.util.LogUtils.logException;
import static java.util.logging.Level.WARNING;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.contact.ContactManager;
import ru.itis.messaging_engine.api.db.DatabaseExecutor;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.lifecycle.LifecycleManager;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.android.controller.DbControllerImpl;
import ru.itis.meshy.android.controller.handler.ResultExceptionHandler;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.identity.AuthorManager;
import ru.itis.meshy.api.sharing.SharingManager.SharingStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class ContactSelectorControllerImpl
		extends DbControllerImpl
		implements ContactSelectorController<SelectableContactItem> {

	private static final Logger LOG =
			Logger.getLogger(ContactSelectorControllerImpl.class.getName());

	private final ContactManager contactManager;
	private final AuthorManager authorManager;

	public ContactSelectorControllerImpl(@DatabaseExecutor Executor dbExecutor,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			AuthorManager authorManager) {
		super(dbExecutor, lifecycleManager);
		this.contactManager = contactManager;
		this.authorManager = authorManager;
	}

	@Override
	public void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<SelectableContactItem>, DbException> handler) {
		runOnDbThread(() -> {
			try {
				Collection<SelectableContactItem> contacts = new ArrayList<>();
				for (Contact c : contactManager.getContacts()) {
					AuthorInfo authorInfo = authorManager.getAuthorInfo(c);
					// was this contact already selected?
					boolean selected = selection.contains(c.getId());
					contacts.add(new SelectableContactItem(c, authorInfo,
							selected, getSharingStatus(g, c)));
				}
				handler.onResult(contacts);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				handler.onException(e);
			}
		});
	}

	@DatabaseExecutor
	protected abstract SharingStatus getSharingStatus(GroupId g, Contact c)
			throws DbException;

}
