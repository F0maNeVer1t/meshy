package ru.itis.meshy.api.privategroup;

import ru.itis.messaging_engine.api.crypto.CryptoExecutor;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

import static ru.itis.meshy.api.privategroup.PrivateGroupManager.CLIENT_ID;

@NotNullByDefault
public interface GroupMessageFactory {

	String SIGNING_LABEL_JOIN = CLIENT_ID.getString() + "/JOIN";
	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";

	/**
	 * Creates a join announcement message for the creator of a group.
	 *
	 * @param groupId The ID of the private group that is being joined
	 * @param timestamp The timestamp to be used in the join announcement
	 * @param creator The creator's identity
	 */
	@CryptoExecutor
	GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor creator);

	/**
	 * Creates a join announcement message for a joining member.
	 *
	 * @param groupId The ID of the private group that is being joined
	 * @param timestamp The timestamp to be used in the join announcement,
	 * which must be greater than the timestamp of the invitation message
	 * @param member The member's identity
	 * @param inviteTimestamp The timestamp of the invitation message
	 * @param creatorSignature The creator's signature from the invitation
	 * message
	 */
	@CryptoExecutor
	GroupMessage createJoinMessage(GroupId groupId, long timestamp,
			LocalAuthor member, long inviteTimestamp, byte[] creatorSignature);

	/**
	 * Creates a private group post.
	 *
	 * @param groupId The ID of the private group
	 * @param timestamp Must be greater than the timestamps of the parent
	 * post, if any, and the member's previous message
	 * @param parentId The ID of the parent post, or null if the post has no
	 * parent
	 * @param author The author of the post
	 * @param text The text of the post
	 * @param previousMsgId The ID of the author's previous message
	 * in this group
	 */
	@CryptoExecutor
	GroupMessage createGroupMessage(GroupId groupId, long timestamp,
			@Nullable MessageId parentId, LocalAuthor author, String text,
			MessageId previousMsgId);

}
