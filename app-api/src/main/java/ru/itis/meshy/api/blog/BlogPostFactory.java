package ru.itis.meshy.api.blog;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.LocalAuthor;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;
import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.Nullable;

import static ru.itis.meshy.api.blog.BlogManager.CLIENT_ID;

@NotNullByDefault
public interface BlogPostFactory {

	String SIGNING_LABEL_POST = CLIENT_ID.getString() + "/POST";
	String SIGNING_LABEL_COMMENT = CLIENT_ID.getString() + "/COMMENT";

	BlogPost createBlogPost(GroupId groupId, long timestamp,
			@Nullable MessageId parent, LocalAuthor author, String text)
			throws FormatException, GeneralSecurityException;

	Message createBlogComment(GroupId groupId, LocalAuthor author,
			@Nullable String comment, MessageId parentOriginalId,
			MessageId parentCurrentId)
			throws FormatException, GeneralSecurityException;

	/**
	 * Wraps a blog post
	 */
	Message wrapPost(GroupId groupId, byte[] descriptor, long timestamp,
			BdfList body) throws FormatException;

	/**
	 * Re-wraps a previously wrapped post
	 */
	Message rewrapWrappedPost(GroupId groupId, BdfList body)
			throws FormatException;

	/**
	 * Wraps a blog comment
	 */
	Message wrapComment(GroupId groupId, byte[] descriptor, long timestamp,
			BdfList body, MessageId parentCurrentId) throws FormatException;

	/**
	 * Re-wraps a previously wrapped comment
	 */
	Message rewrapWrappedComment(GroupId groupId, BdfList body,
			MessageId parentCurrentId) throws FormatException;
}
