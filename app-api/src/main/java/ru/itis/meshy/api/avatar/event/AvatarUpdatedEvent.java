package ru.itis.meshy.api.avatar.event;

import ru.itis.messaging_engine.api.contact.ContactId;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a new avatar is received.
 */
@Immutable
@NotNullByDefault
public class AvatarUpdatedEvent extends Event {

	private final ContactId contactId;
	private final AttachmentHeader attachmentHeader;

	public AvatarUpdatedEvent(ContactId contactId,
			AttachmentHeader attachmentHeader) {
		this.contactId = contactId;
		this.attachmentHeader = attachmentHeader;
	}

	public ContactId getContactId() {
		return contactId;
	}

	public AttachmentHeader getAttachmentHeader() {
		return attachmentHeader;
	}
}
