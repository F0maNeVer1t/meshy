package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfEntry;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.privategroup.PrivateGroup;
import ru.itis.meshy.api.privategroup.PrivateGroupFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.meshy.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static ru.itis.meshy.client.MessageTrackerConstants.MSG_KEY_READ;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_AUTO_DELETE_TIMER;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_AVAILABLE_TO_ANSWER;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_INVITATION_ACCEPTED;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_IS_AUTO_DECLINE;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_LOCAL;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_MESSAGE_TYPE;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_PRIVATE_GROUP_ID;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_TIMESTAMP;
import static ru.itis.meshy.privategroup.invitation.GroupInvitationConstants.MSG_KEY_VISIBLE_IN_UI;
import static ru.itis.meshy.privategroup.invitation.MessageType.INVITE;

@Immutable
@NotNullByDefault
class MessageParserImpl implements MessageParser {

	private final PrivateGroupFactory privateGroupFactory;
	private final ClientHelper clientHelper;

	@Inject
	MessageParserImpl(PrivateGroupFactory privateGroupFactory,
			ClientHelper clientHelper) {
		this.privateGroupFactory = privateGroupFactory;
		this.clientHelper = clientHelper;
	}

	@Override
	public BdfDictionary getMessagesVisibleInUiQuery() {
		return BdfDictionary.of(new BdfEntry(MSG_KEY_VISIBLE_IN_UI, true));
	}

	@Override
	public BdfDictionary getInvitesAvailableToAnswerQuery() {
		return BdfDictionary.of(
				new BdfEntry(MSG_KEY_AVAILABLE_TO_ANSWER, true),
				new BdfEntry(MSG_KEY_MESSAGE_TYPE, INVITE.getValue())
		);
	}

	@Override
	public BdfDictionary getInvitesAvailableToAnswerQuery(
			GroupId privateGroupId) {
		return BdfDictionary.of(
				new BdfEntry(MSG_KEY_AVAILABLE_TO_ANSWER, true),
				new BdfEntry(MSG_KEY_MESSAGE_TYPE, INVITE.getValue()),
				new BdfEntry(MSG_KEY_PRIVATE_GROUP_ID, privateGroupId)
		);
	}

	@Override
	public MessageMetadata parseMetadata(BdfDictionary meta)
			throws FormatException {
		MessageType type =
				MessageType.fromValue(meta.getInt(MSG_KEY_MESSAGE_TYPE));
		GroupId privateGroupId =
				new GroupId(meta.getRaw(MSG_KEY_PRIVATE_GROUP_ID));
		long timestamp = meta.getLong(MSG_KEY_TIMESTAMP);
		boolean local = meta.getBoolean(MSG_KEY_LOCAL);
		boolean read = meta.getBoolean(MSG_KEY_READ, false);
		boolean visible = meta.getBoolean(MSG_KEY_VISIBLE_IN_UI, false);
		boolean available = meta.getBoolean(MSG_KEY_AVAILABLE_TO_ANSWER, false);
		boolean accepted = meta.getBoolean(MSG_KEY_INVITATION_ACCEPTED, false);
		long timer = meta.getLong(MSG_KEY_AUTO_DELETE_TIMER,
				NO_AUTO_DELETE_TIMER);
		boolean isAutoDecline = meta.getBoolean(MSG_KEY_IS_AUTO_DECLINE, false);
		return new MessageMetadata(type, privateGroupId, timestamp, local, read,
				visible, available, accepted, timer, isAutoDecline);
	}

	@Override
	public InviteMessage getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException {
		Message message = clientHelper.getMessage(txn, m);
		BdfList body = clientHelper.toList(message);
		return parseInviteMessage(message, body);
	}

	@Override
	public InviteMessage parseInviteMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, creator, group name, salt,
		// optional text, signature.
		// Client version 0.1: Message type, creator, group name, salt,
		// optional text, signature, optional auto-delete timer.
		BdfList creatorList = body.getList(1);
		String groupName = body.getString(2);
		byte[] salt = body.getRaw(3);
		String text = body.getOptionalString(4);
		byte[] signature = body.getRaw(5);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 7) timer = body.getLong(6, NO_AUTO_DELETE_TIMER);

		Author creator = clientHelper.parseAndValidateAuthor(creatorList);
		PrivateGroup privateGroup = privateGroupFactory.createPrivateGroup(
				groupName, creator, salt);
		return new InviteMessage(m.getId(), m.getGroupId(),
				privateGroup.getId(), m.getTimestamp(), groupName, creator,
				salt, text, signature, timer);
	}

	@Override
	public JoinMessage parseJoinMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, private group ID, optional
		// previous message ID.
		// Client version 0.1: Message type, private group ID, optional
		// previous message ID, optional auto-delete timer.
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = b == null ? null : new MessageId(b);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);

		return new JoinMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp(), previousMessageId, timer);
	}

	@Override
	public LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException {
		// Client version 0.0: Message type, private group ID, optional
		// previous message ID.
		// Client version 0.1: Message type, private group ID, optional
		// previous message ID, optional auto-delete timer.
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		byte[] b = body.getOptionalRaw(2);
		MessageId previousMessageId = b == null ? null : new MessageId(b);
		long timer = NO_AUTO_DELETE_TIMER;
		if (body.size() == 4) timer = body.getLong(3, NO_AUTO_DELETE_TIMER);

		return new LeaveMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp(), previousMessageId, timer);
	}

	@Override
	public AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException {
		GroupId privateGroupId = new GroupId(body.getRaw(1));
		return new AbortMessage(m.getId(), m.getGroupId(), privateGroupId,
				m.getTimestamp());
	}

}
