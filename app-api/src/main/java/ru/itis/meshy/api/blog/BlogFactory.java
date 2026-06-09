package ru.itis.meshy.api.blog;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface BlogFactory {

	/**
	 * Creates a personal blog for a given author.
	 */
	Blog createBlog(Author author);

	/**
	 * Creates a RSS feed blog for a given author.
	 */
	Blog createFeedBlog(Author author);

	/**
	 * Parses a blog with the given Group
	 */
	Blog parseBlog(Group g) throws FormatException;

}
