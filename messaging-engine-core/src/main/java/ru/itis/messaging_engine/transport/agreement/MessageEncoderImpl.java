package ru.itis.messaging_engine.transport.agreement;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.crypto.PublicKey;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfEntry;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.transport.agreement.MessageType.ACTIVATE;
import static ru.itis.messaging_engine.transport.agreement.MessageType.KEY;
import static ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_IS_SESSION;
import static ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_LOCAL;
import static ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_MESSAGE_TYPE;
import static ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementConstants.MSG_KEY_TRANSPORT_ID;

@Immutable
@NotNullByDefault
class MessageEncoderImpl implements MessageEncoder {

	private final ClientHelper clientHelper;
	private final Clock clock;

	@Inject
	MessageEncoderImpl(ClientHelper clientHelper, Clock clock) {
		this.clientHelper = clientHelper;
		this.clock = clock;
	}

	@Override
	public Message encodeKeyMessage(GroupId contactGroupId,
			TransportId transportId, PublicKey publicKey) {
		BdfList body = BdfList.of(
				KEY.getValue(),
				transportId.getString(),
				publicKey.getEncoded());
		return encodeMessage(contactGroupId, body);
	}

	@Override
	public Message encodeActivateMessage(GroupId contactGroupId,
			TransportId transportId, MessageId previousMessageId) {
		BdfList body = BdfList.of(
				ACTIVATE.getValue(),
				transportId.getString(),
				previousMessageId);
		return encodeMessage(contactGroupId, body);
	}

	@Override
	public BdfDictionary encodeMessageMetadata(TransportId transportId,
			MessageType type, boolean local) {
		return BdfDictionary.of(
				new BdfEntry(MSG_KEY_IS_SESSION, false),
				new BdfEntry(MSG_KEY_TRANSPORT_ID, transportId.getString()),
				new BdfEntry(MSG_KEY_MESSAGE_TYPE, type.getValue()),
				new BdfEntry(MSG_KEY_LOCAL, local));
	}

	private Message encodeMessage(GroupId contactGroupId, BdfList body) {
		try {
			return clientHelper.createMessage(contactGroupId,
					clock.currentTimeMillis(), clientHelper.toByteArray(body));
		} catch (FormatException e) {
			throw new AssertionError();
		}
	}
}
