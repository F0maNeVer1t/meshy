package ru.itis.meshy.forum;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.meshy.api.forum.ForumPost;
import ru.itis.meshy.api.forum.ForumPostFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.util.StringUtils.utf8IsTooLong;
import static ru.itis.meshy.api.forum.ForumConstants.MAX_FORUM_POST_TEXT_LENGTH;

@Immutable
@NotNullByDefault
class ForumPostFactoryImpl implements ForumPostFactory {

	private final ClientHelper clientHelper;

	@Inject
	ForumPostFactoryImpl(ClientHelper clientHelper) {
		this.clientHelper = clientHelper;
	}

	@Override
	public ForumPost createPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException {
		// Validate the arguments
		if (utf8IsTooLong(text, MAX_FORUM_POST_TEXT_LENGTH))
			throw new IllegalArgumentException();
		// Serialise the data to be signed
		BdfList authorList = clientHelper.toList(author);
		BdfList signed = BdfList.of(groupId, timestamp, parent, authorList,
				text);
		// Sign the data
		byte[] sig = clientHelper.sign(SIGNING_LABEL_POST, signed,
				author.getPrivateKey());
		// Serialise the signed message
		BdfList message = BdfList.of(parent, authorList, text, sig);
		Message m = clientHelper.createMessage(groupId, timestamp, message);
		return new ForumPost(m, parent, author);
	}

}
