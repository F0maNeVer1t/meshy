package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	BdfDictionary getSessionQuery(SessionId s);

	BdfDictionary getAllSessionsQuery();

	Role getRole(BdfDictionary d) throws FormatException;

	boolean isSession(BdfDictionary d) throws FormatException;

	Session parseSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	CreatorSession parseCreatorSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	InviteeSession parseInviteeSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

	PeerSession parsePeerSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;
}
