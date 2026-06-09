package ru.itis.meshy.android.contactselection;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.android.controller.DbController;
import ru.itis.meshy.android.controller.handler.ResultExceptionHandler;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

@NotNullByDefault
public interface ContactSelectorController<I extends BaseSelectableContactItem>
		extends DbController {

	void loadContacts(GroupId g, Collection<ContactId> selection,
			ResultExceptionHandler<Collection<I>, DbException> handler);

}
