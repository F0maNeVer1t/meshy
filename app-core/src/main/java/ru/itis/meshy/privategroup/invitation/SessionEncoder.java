package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.data.BdfDictionary;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s);
}
