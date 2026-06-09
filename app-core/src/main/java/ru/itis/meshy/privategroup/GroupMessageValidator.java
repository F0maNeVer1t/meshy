package ru.itis.meshy.privategroup;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.BdfMessageContext;
import ru.itis.messaging_engine.api.client.BdfMessageValidator;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.InvalidMessageException;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.meshy.api.privategroup.PrivateGroup;
import ru.itis.meshy.api.privategroup.PrivateGroupFactory;
import ru.itis.meshy.api.privategroup.invitation.GroupInvitationFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import static ru.itis.messaging_engine.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.privategroup.GroupMessageFactory.SIGNING_LABEL_JOIN;
import static ru.itis.meshy.api.privategroup.GroupMessageFactory.SIGNING_LABEL_POST;
import static ru.itis.meshy.api.privategroup.MessageType.JOIN;
import static ru.itis.meshy.api.privategroup.MessageType.POST;
import static ru.itis.meshy.api.privategroup.PrivateGroupConstants.MAX_GROUP_POST_TEXT_LENGTH;
import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationFactory.SIGNING_LABEL_INVITE;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_INITIAL_JOIN_MSG;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_MEMBER;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_PARENT_MSG_ID;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_PREVIOUS_MSG_ID;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_READ;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_TIMESTAMP;
import static ru.itis.meshy.privategroup.GroupConstants.KEY_TYPE;

@Immutable
@NotNullByDefault
class GroupMessageValidator extends BdfMessageValidator {

	private final PrivateGroupFactory privateGroupFactory;
	private final GroupInvitationFactory groupInvitationFactory;

	GroupMessageValidator(PrivateGroupFactory privateGroupFactory,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, GroupInvitationFactory groupInvitationFactory) {
		super(clientHelper, metadataEncoder, clock);
		this.privateGroupFactory = privateGroupFactory;
		this.groupInvitationFactory = groupInvitationFactory;
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {

		checkSize(body, 4, 6);

		// Message type (int)
		int type = body.getInt(0);

		// Member (author)
		BdfList memberList = body.getList(1);
		Author member = clientHelper.parseAndValidateAuthor(memberList);

		BdfMessageContext c;
		if (type == JOIN.getInt()) {
			c = validateJoin(m, g, body, member);
			addMessageMetadata(c, memberList, m.getTimestamp());
		} else if (type == POST.getInt()) {
			c = validatePost(m, g, body, member);
			addMessageMetadata(c, memberList, m.getTimestamp());
		} else {
			throw new InvalidMessageException("Unknown Message Type");
		}
		c.getDictionary().put(KEY_TYPE, type);
		return c;
	}

	private BdfMessageContext validateJoin(Message m, Group g, BdfList body,
			Author member) throws FormatException {
		// Message type, member, optional invite, member's signature
		checkSize(body, 4);
		BdfList inviteList = body.getOptionalList(2);
		byte[] memberSignature = body.getRaw(3);
		checkLength(memberSignature, 1, MAX_SIGNATURE_LENGTH);

		// Invite is null if the member is the creator of the private group
		PrivateGroup pg = privateGroupFactory.parsePrivateGroup(g);
		Author creator = pg.getCreator();
		boolean isCreator = member.equals(creator);
		if (isCreator) {
			if (inviteList != null) throw new FormatException();
		} else {
			if (inviteList == null) throw new FormatException();
			// Timestamp, creator's signature
			checkSize(inviteList, 2);
			// Join timestamp must be greater than invite timestamp
			long inviteTimestamp = inviteList.getLong(0);
			if (m.getTimestamp() <= inviteTimestamp)
				throw new FormatException();
			byte[] creatorSignature = inviteList.getRaw(1);
			checkLength(creatorSignature, 1, MAX_SIGNATURE_LENGTH);
			// The invite token is signed by the creator of the private group
			BdfList token = groupInvitationFactory.createInviteToken(
					creator.getId(), member.getId(), g.getId(),
					inviteTimestamp);
			try {
				clientHelper.verifySignature(creatorSignature,
						SIGNING_LABEL_INVITE,
						token, creator.getPublicKey());
			} catch (GeneralSecurityException e) {
				throw new FormatException();
			}
		}

		// Verify the member's signature
		BdfList memberList = body.getList(1); // Already validated
		BdfList signed = BdfList.of(
				g.getId(),
				m.getTimestamp(),
				memberList,
				inviteList
		);
		try {
			clientHelper.verifySignature(memberSignature, SIGNING_LABEL_JOIN,
					signed, member.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}

		// Return the metadata and no dependencies
		BdfDictionary meta = new BdfDictionary();
		meta.put(KEY_INITIAL_JOIN_MSG, isCreator);
		return new BdfMessageContext(meta);
	}

	private BdfMessageContext validatePost(Message m, Group g, BdfList body,
			Author member) throws FormatException {
		// Message type, member, optional parent ID, previous message ID,
		// text, signature
		checkSize(body, 6);
		byte[] parentId = body.getOptionalRaw(2);
		checkLength(parentId, MessageId.LENGTH);
		byte[] previousMessageId = body.getRaw(3);
		checkLength(previousMessageId, MessageId.LENGTH);
		String text = body.getString(4);
		checkLength(text, 1, MAX_GROUP_POST_TEXT_LENGTH);
		byte[] signature = body.getRaw(5);
		checkLength(signature, 1, MAX_SIGNATURE_LENGTH);

		// Verify the member's signature
		BdfList memberList = body.getList(1); // Already validated
		BdfList signed = BdfList.of(
				g.getId(),
				m.getTimestamp(),
				memberList,
				parentId,
				previousMessageId,
				text
		);
		try {
			clientHelper.verifySignature(signature, SIGNING_LABEL_POST,
					signed, member.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}

		// The parent post, if any, and the member's previous message are
		// dependencies
		Collection<MessageId> dependencies = new ArrayList<>();
		if (parentId != null) dependencies.add(new MessageId(parentId));
		dependencies.add(new MessageId(previousMessageId));

		// Return the metadata and dependencies
		BdfDictionary meta = new BdfDictionary();
		if (parentId != null) meta.put(KEY_PARENT_MSG_ID, parentId);
		meta.put(KEY_PREVIOUS_MSG_ID, previousMessageId);
		return new BdfMessageContext(meta, dependencies);
	}

	private void addMessageMetadata(BdfMessageContext c, BdfList member,
			long timestamp) {
		c.getDictionary().put(KEY_MEMBER, member);
		c.getDictionary().put(KEY_TIMESTAMP, timestamp);
		c.getDictionary().put(KEY_READ, false);
	}

}
