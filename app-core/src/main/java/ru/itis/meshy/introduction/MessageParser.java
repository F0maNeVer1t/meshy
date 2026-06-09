package ru.itis.meshy.introduction;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.meshy.api.client.SessionId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MessageParser {

	BdfDictionary getMessagesVisibleInUiQuery();

	BdfDictionary getRequestsAvailableToAnswerQuery(SessionId sessionId);

	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;

	RequestMessage parseRequestMessage(Message m, BdfList body)
			throws FormatException;

	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;

	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;

	AuthMessage parseAuthMessage(Message m, BdfList body)
			throws FormatException;

	ActivateMessage parseActivateMessage(Message m, BdfList body)
			throws FormatException;

	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;

}
