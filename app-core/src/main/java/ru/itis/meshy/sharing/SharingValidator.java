package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.UniqueId;
import ru.itis.messaging_engine.api.client.BdfMessageContext;
import ru.itis.messaging_engine.api.client.BdfMessageValidator;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.singletonList;
import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static ru.itis.meshy.api.sharing.SharingConstants.MAX_INVITATION_TEXT_LENGTH;
import static ru.itis.meshy.sharing.MessageType.INVITE;
import static ru.itis.meshy.util.ValidationUtils.validateAutoDeleteTimer;

@Immutable
@NotNullByDefault
abstract class SharingValidator extends BdfMessageValidator {

	private final MessageEncoder messageEncoder;

	SharingValidator(MessageEncoder messageEncoder, ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
		this.messageEncoder = messageEncoder;
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws FormatException {
		MessageType type = MessageType.fromValue(body.getInt(0));
		switch (type) {
			case INVITE:
				return validateInviteMessage(m, body);
			case ACCEPT:
			case DECLINE:
				return validateAcceptOrDeclineMessage(type, m, body);
			case LEAVE:
			case ABORT:
				return validateLeaveOrAbortMessage(type, m, body);
			default:
				throw new FormatException();
		}
	}

	private BdfMessageContext validateInviteMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, optional previous message ID,
		// descriptor, optional text.
		// Client version 0.1: Message type, optional previous message ID,
		// descriptor, optional text, optional auto-delete timer.
		checkSize(body, 4, 5);
		byte[] previousMessageId = body.getOptionalRaw(1);
		checkLength(previousMessageId, UniqueId.LENGTH);
		BdfList descriptor = body.getList(2);
		GroupId shareableId = validateDescriptor(descriptor);
		String text = body.getOptionalString(3);
		checkLength(text, 1, MAX_INVITATION_TEXT_LENGTH);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 5) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(4));
		}

		BdfDictionary meta = messageEncoder.encodeMetadata(INVITE, shareableId,
				m.getTimestamp(), false, false, false, false, false, timer,
				false);
		if (previousMessageId == null) {
			return new BdfMessageContext(meta);
		} else {
			MessageId dependency = new MessageId(previousMessageId);
			return new BdfMessageContext(meta, singletonList(dependency));
		}
	}

	protected abstract GroupId validateDescriptor(BdfList descriptor)
			throws FormatException;

	private BdfMessageContext validateLeaveOrAbortMessage(MessageType type,
			Message m, BdfList body) throws FormatException {
		checkSize(body, 3);
		byte[] shareableId = body.getRaw(1);
		checkLength(shareableId, UniqueId.LENGTH);
		byte[] previousMessageId = body.getOptionalRaw(2);
		checkLength(previousMessageId, UniqueId.LENGTH);

		BdfDictionary meta = messageEncoder.encodeMetadata(type,
				new GroupId(shareableId), m.getTimestamp(), false, false,
				false, false, false, NO_AUTO_DELETE_TIMER, false);
		if (previousMessageId == null) {
			return new BdfMessageContext(meta);
		} else {
			MessageId dependency = new MessageId(previousMessageId);
			return new BdfMessageContext(meta, singletonList(dependency));
		}
	}

	private BdfMessageContext validateAcceptOrDeclineMessage(MessageType type,
			Message m, BdfList body) throws FormatException {
		// Client version 0.0: Message type, shareable ID, optional previous
		// message ID.
		// Client version 0.1: Message type, shareable ID, optional previous
		// message ID, optional auto-delete timer.
		checkSize(body, 3, 4);
		byte[] shareableId = body.getRaw(1);
		checkLength(shareableId, UniqueId.LENGTH);
		byte[] previousMessageId = body.getOptionalRaw(2);
		checkLength(previousMessageId, UniqueId.LENGTH);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(3));
		}

		BdfDictionary meta = messageEncoder.encodeMetadata(type,
				new GroupId(shareableId), m.getTimestamp(), false, false,
				false, false, false, timer, false);
		if (previousMessageId == null) {
			return new BdfMessageContext(meta);
		} else {
			MessageId dependency = new MessageId(previousMessageId);
			return new BdfMessageContext(meta, singletonList(dependency));
		}
	}
}
