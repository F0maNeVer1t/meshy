package ru.itis.meshy.api.privategroup.invitation;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.meshy.api.privategroup.PrivateGroup;
import ru.itis.meshy.api.sharing.InvitationItem;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupInvitationItem extends InvitationItem<PrivateGroup> {

	private final Contact creator;

	public GroupInvitationItem(PrivateGroup privateGroup, Contact creator) {
		super(privateGroup, false);
		this.creator = creator;
	}

	public Contact getCreator() {
		return creator;
	}

}
