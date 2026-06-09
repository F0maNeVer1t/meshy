package ru.itis.meshy.api.messaging;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;

@NotNullByDefault
public interface PrivateMessageFactory {

	/**
	 * Creates a private message in the
	 * {@link PrivateMessageFormat#TEXT_ONLY TEXT_ONLY} format.
	 */
	PrivateMessage createLegacyPrivateMessage(GroupId groupId, long timestamp,
			String text) throws FormatException;

	/**
	 * Creates a private message in the
	 * {@link PrivateMessageFormat#TEXT_IMAGES TEXT_IMAGES} format. This format
	 * requires the contact to support client version 0.1 or higher.
	 */
	PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers)
			throws FormatException;

	/**
	 * Creates a private message in the
	 * {@link PrivateMessageFormat#TEXT_IMAGES_AUTO_DELETE TEXT_IMAGES_AUTO_DELETE}
	 * format. This format requires the contact to support client version 0.3
	 * or higher.
	 */
	PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers,
			long autoDeleteTimer) throws FormatException;
}
