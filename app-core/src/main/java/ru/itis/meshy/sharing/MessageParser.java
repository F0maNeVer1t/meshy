package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MessageParser<S extends Shareable> {

	BdfDictionary getMessagesVisibleInUiQuery();

	BdfDictionary getInvitesAvailableToAnswerQuery();

	BdfDictionary getInvitesAvailableToAnswerQuery(GroupId shareableId);

	MessageMetadata parseMetadata(BdfDictionary meta) throws FormatException;

	S createShareable(BdfList descriptor) throws FormatException;

	InviteMessage<S> getInviteMessage(Transaction txn, MessageId m)
			throws DbException, FormatException;

	InviteMessage<S> parseInviteMessage(Message m, BdfList body)
			throws FormatException;

	AcceptMessage parseAcceptMessage(Message m, BdfList body)
			throws FormatException;

	DeclineMessage parseDeclineMessage(Message m, BdfList body)
			throws FormatException;

	LeaveMessage parseLeaveMessage(Message m, BdfList body)
			throws FormatException;

	AbortMessage parseAbortMessage(Message m, BdfList body)
			throws FormatException;

}
