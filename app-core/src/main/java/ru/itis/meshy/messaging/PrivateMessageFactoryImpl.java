package ru.itis.meshy.messaging;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.meshy.api.attachment.AttachmentHeader;
import ru.itis.meshy.api.messaging.PrivateMessage;
import ru.itis.meshy.api.messaging.PrivateMessageFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.util.StringUtils.utf8IsTooLong;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static ru.itis.meshy.api.messaging.MessagingConstants.MAX_PRIVATE_MESSAGE_TEXT_LENGTH;
import static ru.itis.meshy.messaging.MessageTypes.PRIVATE_MESSAGE;

@Immutable
@NotNullByDefault
class PrivateMessageFactoryImpl implements PrivateMessageFactory {

	private final ClientHelper clientHelper;

	@Inject
	PrivateMessageFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}

	@Override
	public PrivateMessage createLegacyPrivateMessage(GroupId groupId,
			long timestamp, String text) throws FormatException {
		// Validate the arguments
		if (utf8IsTooLong(text, MAX_PRIVATE_MESSAGE_TEXT_LENGTH))
			throw new IllegalArgumentException();
		// Serialise the message
		BdfList body = BdfList.of(text);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m);
	}

	@Override
	public PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers)
			throws FormatException {
		validateTextAndAttachmentHeaders(text, headers);
		BdfList attachmentList = serialiseAttachmentHeaders(headers);
		// Serialise the message
		BdfList body = BdfList.of(PRIVATE_MESSAGE, text, attachmentList);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m, text != null, headers);
	}

	@Override
	public PrivateMessage createPrivateMessage(GroupId groupId, long timestamp,
			@Nullable String text, List<AttachmentHeader> headers,
			long autoDeleteTimer) throws FormatException {
		validateTextAndAttachmentHeaders(text, headers);
		BdfList attachmentList = serialiseAttachmentHeaders(headers);
		// Serialise the message
		Long timer = autoDeleteTimer == NO_AUTO_DELETE_TIMER ?
				null : autoDeleteTimer;
		BdfList body = BdfList.of(PRIVATE_MESSAGE, text, attachmentList, timer);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		return new PrivateMessage(m, text != null, headers, autoDeleteTimer);
	}

	private void validateTextAndAttachmentHeaders(@Nullable String text,
			List<AttachmentHeader> headers) {
		if (text == null) {
			if (headers.isEmpty()) throw new IllegalArgumentException();
		} else if (utf8IsTooLong(text, MAX_PRIVATE_MESSAGE_TEXT_LENGTH)) {
			throw new IllegalArgumentException();
		}
	}

	private BdfList serialiseAttachmentHeaders(List<AttachmentHeader> headers) {
		BdfList attachmentList = new BdfList();
		for (AttachmentHeader a : headers) {
			attachmentList.add(
					BdfList.of(a.getMessageId(), a.getContentType()));
		}
		return attachmentList;
	}
}
