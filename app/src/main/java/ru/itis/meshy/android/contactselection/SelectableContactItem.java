package ru.itis.meshy.android.contactselection;

import static ru.itis.meshy.api.sharing.SharingManager.SharingStatus.SHAREABLE;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.meshy.api.identity.AuthorInfo;
import ru.itis.meshy.api.sharing.SharingManager.SharingStatus;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
public class SelectableContactItem extends BaseSelectableContactItem {

	private final SharingStatus sharingStatus;

	public SelectableContactItem(Contact contact, AuthorInfo authorInfo,
			boolean selected, SharingStatus sharingStatus) {
		super(contact, authorInfo, selected);
		this.sharingStatus = sharingStatus;
	}

	public SharingStatus getSharingStatus() {
		return sharingStatus;
	}

	@Override
	public boolean isDisabled() {
		return sharingStatus != SHAREABLE;
	}

}
