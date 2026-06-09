package ru.itis.meshy.blog;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.messaging_engine.util.StringUtils;
import ru.itis.meshy.api.blog.BlogPost;
import ru.itis.meshy.api.blog.BlogPostFactory;
import ru.itis.meshy.api.blog.MessageType;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.meshy.api.blog.BlogConstants.MAX_BLOG_COMMENT_TEXT_LENGTH;
import static ru.itis.meshy.api.blog.BlogConstants.MAX_BLOG_POST_TEXT_LENGTH;
import static ru.itis.meshy.api.blog.MessageType.COMMENT;
import static ru.itis.meshy.api.blog.MessageType.POST;
import static ru.itis.meshy.api.blog.MessageType.WRAPPED_COMMENT;
import static ru.itis.meshy.api.blog.MessageType.WRAPPED_POST;

@Immutable
@NotNullByDefault
class BlogPostFactoryImpl implements BlogPostFactory {

	private final ClientHelper clientHelper;
	private final Clock clock;

	@Inject
	BlogPostFactoryImpl(ClientHelper clientHelper, Clock clock) {
		this.clientHelper = clientHelper;
		this.clock = clock;
	}

	@Override
	public BlogPost createBlogPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException {

		// Validate the arguments
		int textLength = StringUtils.toUtf8(text).length;
		if (textLength > MAX_BLOG_POST_TEXT_LENGTH)
			throw new IllegalArgumentException();

		// Serialise the data to be signed
		BdfList signed = BdfList.of(groupId, timestamp, text);

		// Generate the signature
		byte[] sig = clientHelper
				.sign(SIGNING_LABEL_POST, signed, author.getPrivateKey());

		// Serialise the signed message
		BdfList message = BdfList.of(POST.getInt(), text, sig);
		Message m = clientHelper.createMessage(groupId, timestamp, message);
		return new BlogPost(m, parent, author);
	}

	@Override
	public Message createBlogComment(GroupId groupId, LocalAuthor author,
			@Nullable String comment, MessageId parentOriginalId,
			MessageId parentCurrentId)
			throws FormatException, GeneralSecurityException {

		if (comment != null) {
			int commentLength = StringUtils.toUtf8(comment).length;
			if (commentLength == 0) throw new IllegalArgumentException();
			if (commentLength > MAX_BLOG_COMMENT_TEXT_LENGTH)
				throw new IllegalArgumentException();
		}

		long timestamp = clock.currentTimeMillis();

		// Generate the signature
		BdfList signed = BdfList.of(groupId, timestamp, comment,
				parentOriginalId, parentCurrentId);
		byte[] sig = clientHelper
				.sign(SIGNING_LABEL_COMMENT, signed, author.getPrivateKey());

		// Serialise the signed message
		BdfList message = BdfList.of(COMMENT.getInt(), comment,
				parentOriginalId, parentCurrentId, sig);
		return clientHelper.createMessage(groupId, timestamp, message);
	}

	@Override
	public Message wrapPost(GroupId groupId, byte[] descriptor,
			long timestamp, BdfList body) throws FormatException {

		if (getType(body) != POST)
			throw new IllegalArgumentException("Needs to wrap a POST");

		// Serialise the message
		String text = body.getString(1);
		byte[] signature = body.getRaw(2);
		BdfList message = BdfList.of(WRAPPED_POST.getInt(), descriptor,
				timestamp, text, signature);
		return clientHelper
				.createMessage(groupId, clock.currentTimeMillis(), message);
	}

	@Override
	public Message rewrapWrappedPost(GroupId groupId, BdfList body)
			throws FormatException {

		if (getType(body) != WRAPPED_POST)
			throw new IllegalArgumentException("Needs to wrap a WRAPPED_POST");

		// Serialise the message
		byte[] descriptor = body.getRaw(1);
		long timestamp = body.getLong(2);
		String text = body.getString(3);
		byte[] signature = body.getRaw(4);
		BdfList message = BdfList.of(WRAPPED_POST.getInt(), descriptor,
				timestamp, text, signature);
		return clientHelper
				.createMessage(groupId, clock.currentTimeMillis(), message);
	}

	@Override
	public Message wrapComment(GroupId groupId, byte[] descriptor,
			long timestamp, BdfList body, MessageId parentCurrentId)
			throws FormatException {

		if (getType(body) != COMMENT)
			throw new IllegalArgumentException("Needs to wrap a COMMENT");

		// Serialise the message
		String comment = body.getOptionalString(1);
		byte[] pOriginalId = body.getRaw(2);
		byte[] oldParentId = body.getRaw(3);
		byte[] signature = body.getRaw(4);
		BdfList message = BdfList.of(WRAPPED_COMMENT.getInt(), descriptor,
				timestamp, comment, pOriginalId, oldParentId, signature,
				parentCurrentId);
		return clientHelper
				.createMessage(groupId, clock.currentTimeMillis(), message);
	}

	@Override
	public Message rewrapWrappedComment(GroupId groupId, BdfList body,
			MessageId parentCurrentId) throws FormatException {

		if (getType(body) != WRAPPED_COMMENT)
			throw new IllegalArgumentException(
					"Needs to wrap a WRAPPED_COMMENT");

		// Serialise the message
		byte[] descriptor = body.getRaw(1);
		long timestamp = body.getLong(2);
		String comment = body.getOptionalString(3);
		byte[] pOriginalId = body.getRaw(4);
		byte[] oldParentId = body.getRaw(5);
		byte[] signature = body.getRaw(6);
		BdfList message = BdfList.of(WRAPPED_COMMENT.getInt(), descriptor,
				timestamp, comment, pOriginalId, oldParentId, signature,
				parentCurrentId);
		return clientHelper
				.createMessage(groupId, clock.currentTimeMillis(), message);
	}

	private MessageType getType(BdfList body) throws FormatException {
		return MessageType.valueOf(body.getInt(0));
	}
}
