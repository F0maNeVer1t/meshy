package ru.itis.messaging_engine.transport.agreement;

import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SessionEncoder {

	BdfDictionary encodeSession(Session s, TransportId transportId);

	BdfDictionary getSessionQuery(TransportId transportId);
}
