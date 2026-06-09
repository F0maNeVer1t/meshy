package ru.itis.messaging_engine.api.client;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.db.Metadata;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.InvalidMessageException;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageContext;
import ru.itis.messaging_engine.api.sync.validation.MessageValidator;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import static java.util.logging.Logger.getLogger;
import static ru.itis.messaging_engine.api.transport.TransportConstants.MAX_CLOCK_DIFFERENCE;

@Immutable
@NotNullByDefault
public abstract class BdfMessageValidator implements MessageValidator {

	protected static final Logger LOG =
			getLogger(BdfMessageValidator.class.getName());

	protected final ClientHelper clientHelper;
	protected final MetadataEncoder metadataEncoder;
	protected final Clock clock;
	protected final boolean canonical;

	/**
	 * Transitional alternative to
	 * {@link #BdfMessageValidator(ClientHelper, MetadataEncoder, Clock)} that
	 * accepts messages in non-canonical form, for backward compatibility.
	 */
	@Deprecated
	protected BdfMessageValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock, boolean canonical) {
		this.clientHelper = clientHelper;
		this.metadataEncoder = metadataEncoder;
		this.clock = clock;
		this.canonical = canonical;
	}

	protected BdfMessageValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		this(clientHelper, metadataEncoder, clock, true);
	}

	protected abstract BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException;

	@Override
	public MessageContext validateMessage(Message m, Group g)
			throws InvalidMessageException {
		// Reject the message if it's too far in the future
		long now = clock.currentTimeMillis();
		if (m.getTimestamp() - now > MAX_CLOCK_DIFFERENCE) {
			throw new InvalidMessageException(
					"Timestamp is too far in the future");
		}
		try {
			BdfList bodyList = clientHelper.toList(m, canonical);
			BdfMessageContext result = validateMessage(m, g, bodyList);
			Metadata meta = metadataEncoder.encode(result.getDictionary());
			return new MessageContext(meta, result.getDependencies());
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
	}
}
