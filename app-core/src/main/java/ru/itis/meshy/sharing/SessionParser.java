package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.meshy.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionParser {

	BdfDictionary getSessionQuery(SessionId s);

	BdfDictionary getAllSessionsQuery();

	boolean isSession(BdfDictionary d) throws FormatException;

	Session parseSession(GroupId contactGroupId, BdfDictionary d)
			throws FormatException;

}
