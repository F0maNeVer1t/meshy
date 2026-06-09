package ru.itis.messaging_engine.api.sync.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.sync.Group.Visibility;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when the visibility of a group is updated.
 */
@Immutable
@NotNullByDefault
public class GroupVisibilityUpdatedEvent extends Event {

	private final Visibility visibility;
	private final Collection<ContactId> affected;

	public GroupVisibilityUpdatedEvent(Visibility visibility,
			Collection<ContactId> affected) {
		this.visibility = visibility;
		this.affected = affected;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	/**
	 * Returns the contacts affected by the update.
	 */
	public Collection<ContactId> getAffectedContacts() {
		return affected;
	}
}
