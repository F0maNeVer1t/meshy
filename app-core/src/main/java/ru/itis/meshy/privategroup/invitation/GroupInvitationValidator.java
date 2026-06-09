package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.UniqueId;
import ru.itis.messaging_engine.api.client.BdfMessageContext;
import ru.itis.messaging_engine.api.client.BdfMessageValidator;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.meshy.api.privategroup.PrivateGroup;
import ru.itis.meshy.api.privategroup.PrivateGroupFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.annotation.concurrent.Immutable;

import static ru.itis.messaging_engine.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static ru.itis.meshy.api.privategroup.PrivateGroupConstants.GROUP_SALT_LENGTH;
import static ru.itis.meshy.api.privategroup.PrivateGroupConstants.MAX_GROUP_INVITATION_TEXT_LENGTH;
import static ru.itis.meshy.api.privategroup.PrivateGroupConstants.MAX_GROUP_NAME_LENGTH;
import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationFactory.SIGNING_LABEL_INVITE;
import static ru.itis.meshy.privategroup.invitation.MessageType.ABORT;
import static ru.itis.meshy.privategroup.invitation.MessageType.INVITE;
import static ru.itis.meshy.privategroup.invitation.MessageType.JOIN;
import static ru.itis.meshy.privategroup.invitation.MessageType.LEAVE;
import static ru.itis.meshy.util.ValidationUtils.validateAutoDeleteTimer;

@Immutable
@NotNullByDefault
class GroupInvitationValidator extends BdfMessageValidator {

	private final PrivateGroupFactory privateGroupFactory;
	private final MessageEncoder messageEncoder;

	GroupInvitationValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock,
			PrivateGroupFactory privateGroupFactory,
			MessageEncoder messageEncoder) {
		super(clientHelper, metadataEncoder, clock);
		this.privateGroupFactory = privateGroupFactory;
		this.messageEncoder = messageEncoder;
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws FormatException {
		MessageType type = MessageType.fromValue(body.getInt(0));
		switch (type) {
			case INVITE:
				return validateInviteMessage(m, body);
			case JOIN:
				return validateJoinMessage(m, body);
			case LEAVE:
				return validateLeaveMessage(m, body);
			case ABORT:
				return validateAbortMessage(m, body);
			default:
				throw new FormatException();
		}
	}

	private BdfMessageContext validateInviteMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, creator, group name, salt,
		// optional text, signature.
		// Client version 0.1: Message type, creator, group name, salt,
		// optional text, signature, optional auto-delete timer.
		checkSize(body, 6, 7);
		BdfList creatorList = body.getList(1);
		String groupName = body.getString(2);
		checkLength(groupName, 1, MAX_GROUP_NAME_LENGTH);
		byte[] salt = body.getRaw(3);
		checkLength(salt, GROUP_SALT_LENGTH);
		String text = body.getOptionalString(4);
		checkLength(text, 1, MAX_GROUP_INVITATION_TEXT_LENGTH);
		byte[] signature = body.getRaw(5);
		checkLength(signature, 1, MAX_SIGNATURE_LENGTH);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 7) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(6));
		}

		// Validate the creator and create the private group
		Author creator = clientHelper.parseAndValidateAuthor(creatorList);
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				groupName, creator, salt);
		// Verify the signature
		BdfList signed = BdfList.of(
				m.getTimestamp(),
				m.getGroupId(),
				privateGroup.getId()
		);
		try {
			clientHelper.verifySignature(signature, SIGNING_LABEL_INVITE,
					signed, creator.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
		// Create the metadata
		BdfDictionary meta = messageEncoder.encodeMetadata(INVITE,
				privateGroup.getId(), m.getTimestamp(), timer);
		return new BdfMessageContext(meta);
	}

	private BdfMessageContext validateJoinMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, private group ID, optional
		// previous message ID.
		// Client version 0.1: Message type, private group ID, optional
		// previous message ID, optional auto-delete timer.
		checkSize(body, 3, 4);
		byte[] privateGroupId = body.getRaw(1);
		checkLength(privateGroupId, UniqueId.LENGTH);
		byte[] previousMessageId = body.getOptionalRaw(2);
		checkLength(previousMessageId, UniqueId.LENGTH);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(3));
		}

		BdfDictionary meta = messageEncoder.encodeMetadata(JOIN,
				new GroupId(privateGroupId), m.getTimestamp(), timer);
		if (previousMessageId == null) {
			return new BdfMessageContext(meta);
		} else {
			MessageId dependency = new MessageId(previousMessageId);
			return new BdfMessageContext(meta,
					Collections.singletonList(dependency));
		}
	}

	private BdfMessageContext validateLeaveMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, private group ID, optional
		// previous message ID.
		// Client version 0.1: Message type, private group ID, optional
		// previous message ID, optional auto-delete timer.
		checkSize(body, 3, 4);
		byte[] privateGroupId = body.getRaw(1);
		checkLength(privateGroupId, UniqueId.LENGTH);
		byte[] previousMessageId = body.getOptionalRaw(2);
		checkLength(previousMessageId, UniqueId.LENGTH);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) {
			timer = validateAutoDeleteTimer(body.getOptionalLong(3));
		}

		BdfDictionary meta = messageEncoder.encodeMetadata(LEAVE,
				new GroupId(privateGroupId), m.getTimestamp(), timer);
		if (previousMessageId == null) {
			return new BdfMessageContext(meta);
		} else {
			MessageId dependency = new MessageId(previousMessageId);
			return new BdfMessageContext(meta,
					Collections.singletonList(dependency));
		}
	}

	private BdfMessageContext validateAbortMessage(Message m, BdfList body)
			throws FormatException {
		checkSize(body, 2);
		byte[] privateGroupId = body.getRaw(1);
		checkLength(privateGroupId, UniqueId.LENGTH);
		BdfDictionary meta = messageEncoder.encodeMetadata(ABORT,
				new GroupId(privateGroupId), m.getTimestamp(),
				NO_AUTO_DELETE_TIMER);
		return new BdfMessageContext(meta);
	}
}
