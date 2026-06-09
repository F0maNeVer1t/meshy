package ru.itis.messaging_engine.properties;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.BdfMessageContext;
import ru.itis.messaging_engine.api.client.BdfMessageValidator;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static ru.itis.messaging_engine.api.plugin.TransportId.MAX_TRANSPORT_ID_LENGTH;
import static ru.itis.messaging_engine.api.properties.TransportPropertyConstants.MSG_KEY_LOCAL;
import static ru.itis.messaging_engine.api.properties.TransportPropertyConstants.MSG_KEY_TRANSPORT_ID;
import static ru.itis.messaging_engine.api.properties.TransportPropertyConstants.MSG_KEY_VERSION;
import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class TransportPropertyValidator extends BdfMessageValidator {

	TransportPropertyValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		// Accept transport properties in non-canonical form
		// TODO: Remove this after a reasonable migration period
		//  (added 2023-02-17)
		super(clientHelper, metadataEncoder, clock, false);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws FormatException {
		// Transport ID, version, properties
		checkSize(body, 3);
		// Transport ID
		String transportId = body.getString(0);
		checkLength(transportId, 1, MAX_TRANSPORT_ID_LENGTH);
		// Version
		long version = body.getLong(1);
		if (version < 0) throw new FormatException();
		// Properties
		BdfDictionary dictionary = body.getDictionary(2);
		clientHelper.parseAndValidateTransportProperties(dictionary);
		// Return the metadata
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_TRANSPORT_ID, transportId);
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_LOCAL, false);
		return new BdfMessageContext(meta);
	}
}
