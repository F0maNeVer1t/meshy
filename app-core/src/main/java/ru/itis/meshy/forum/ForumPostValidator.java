package ru.itis.meshy.forum;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.UniqueId;
import ru.itis.messaging_engine.api.client.BdfMessageContext;
import ru.itis.messaging_engine.api.client.BdfMessageValidator;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.InvalidMessageException;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.Collection;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static ru.itis.messaging_engine.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.forum.ForumConstants.KEY_AUTHOR;
import static ru.itis.meshy.api.forum.ForumConstants.KEY_PARENT;
import static ru.itis.meshy.api.forum.ForumConstants.KEY_READ;
import static ru.itis.meshy.api.forum.ForumConstants.KEY_TIMESTAMP;
import static ru.itis.meshy.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;
import static ru.itis.meshy.api.forum.ForumPostFactory.SIGNING_LABEL_POST;

@Immutable
@NotNullByDefault
class ForumPostValidator extends BdfMessageValidator {

	ForumPostValidator(ClientHelper clientHelper,
			MetadataEncoder metadataEncoder, Clock clock) {
		super(clientHelper, metadataEncoder, clock);
	}

	@Override
	protected BdfMessageContext validateMessage(Message m, Group g,
			BdfList body) throws InvalidMessageException, FormatException {
		// Parent ID, author, text, signature
		checkSize(body, 4);

		// Parent ID is optional
		byte[] parent = body.getOptionalRaw(0);
		checkLength(parent, UniqueId.LENGTH);

		// Author
		BdfList authorList = body.getList(1);
		Author author = clientHelper.parseAndValidateAuthor(authorList);

		// Text
		String text = body.getString(2);
		checkLength(text, 0, MAX_FORUM_POST_TEXT_LENGTH);

		// Signature
		byte[] sig = body.getRaw(3);
		checkLength(sig, 1, MAX_SIGNATURE_LENGTH);

		// Verify the signature
		BdfList signed = BdfList.of(g.getId(), m.getTimestamp(), parent,
				authorList, text);
		try {
			clientHelper.verifySignature(sig, SIGNING_LABEL_POST,
					signed, author.getPublicKey());
		} catch (GeneralSecurityException e) {
			throw new InvalidMessageException(e);
		}

		// Return the metadata and dependencies
		BdfDictionary meta = new BdfDictionary();
		Collection<MessageId> dependencies = emptyList();
		meta.put(KEY_TIMESTAMP, m.getTimestamp());
		if (parent != null) {
			meta.put(KEY_PARENT, parent);
			dependencies = singletonList(new MessageId(parent));
		}
		meta.put(KEY_AUTHOR, authorList);
		meta.put(KEY_READ, false);
		return new BdfMessageContext(meta, dependencies);
	}
}
