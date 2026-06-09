package ru.itis.meshy.api.privategroup.invitation;

import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.crypto.CryptoExecutor;
import ru.itis.messaging_engine.api.crypto.PrivateKey;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.AuthorId;
import ru.itis.messaging_engine.api.sync.GroupId;
import org.briarproject.nullsafety.NotNullByDefault;

import static ru.itis.meshy.api.privategroup.invitation.GroupInvitationManager.CLIENT_ID;

@NotNullByDefault
public interface GroupInvitationFactory {

	String SIGNING_LABEL_INVITE = CLIENT_ID.getString() + "/INVITE";

	/**
	 * Returns a signature to include when inviting a member to join a private
	 * group. If the member accepts the invitation, the signature will be
	 * included in the member's join message.
	 */
	@CryptoExecutor
	byte[] signInvitation(Contact c, GroupId privateGroupId, long timestamp,
			PrivateKey privateKey);

	/**
	 * Returns a token to be signed by the creator when inviting a member to
	 * join a private group. If the member accepts the invitation, the
	 * signature will be included in the member's join message.
	 */
	BdfList createInviteToken(AuthorId creatorId, AuthorId memberId,
			GroupId privateGroupId, long timestamp);

}
