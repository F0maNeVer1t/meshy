package ru.itis.meshy.api.privategroup.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.identity.AuthorId;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.privategroup.Visibility;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ContactRelationshipRevealedEvent extends Event {

	private final GroupId groupId;
	private final AuthorId memberId;
	private final ContactId contactId;
	private final Visibility visibility;

	public ContactRelationshipRevealedEvent(GroupId groupId, AuthorId memberId,
			ContactId contactId, Visibility visibility) {
		this.groupId = groupId;
		this.memberId = memberId;
		this.contactId = contactId;
		this.visibility = visibility;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public AuthorId getMemberId() {
		return memberId;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public Visibility getVisibility() {
		return visibility;
	}

}
