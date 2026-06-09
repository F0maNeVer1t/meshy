package ru.itis.meshy.android.contactselection;

import androidx.annotation.UiThread;

import ru.itis.messaging_engine.api.contact.ContactId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

@NotNullByDefault
public interface ContactSelectorListener {

	@UiThread
	void contactsSelected(Collection<ContactId> contacts);

}
