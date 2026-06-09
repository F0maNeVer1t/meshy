package ru.itis.meshy.blog;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.GroupFactory;
import ru.itis.meshy.api.blog.Blog;
import ru.itis.meshy.api.blog.BlogFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.blog.BlogManager.CLIENT_ID;
import static ru.itis.meshy.api.blog.BlogManager.MAJOR_VERSION;

@Immutable
@NotNullByDefault
class BlogFactoryImpl implements BlogFactory {

	private final GroupFactory groupFactory;
	private final ClientHelper clientHelper;

	@Inject
	BlogFactoryImpl(GroupFactory groupFactory, ClientHelper clientHelper) {

		this.groupFactory = groupFactory;
		this.clientHelper = clientHelper;
	}

	@Override
	public Blog createBlog(Author a) {
		return createBlog(a, false);
	}

	@Override
	public Blog createFeedBlog(Author a) {
		return createBlog(a, true);
	}

	private Blog createBlog(Author a, boolean rssFeed) {
		try {
			BdfList blog = BdfList.of(clientHelper.toList(a), rssFeed);
			byte[] descriptor = clientHelper.toByteArray(blog);
			Group g = groupFactory.createGroup(CLIENT_ID, MAJOR_VERSION,
					descriptor);
			return new Blog(g, a, rssFeed);
		} catch (FormatException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Blog parseBlog(Group g) throws FormatException {
		// Author, RSS feed
		BdfList descriptor = clientHelper.toList(g.getDescriptor());
		checkSize(descriptor, 2);
		BdfList authorList = descriptor.getList(0);
		boolean rssFeed = descriptor.getBoolean(1);

		Author author = clientHelper.parseAndValidateAuthor(authorList);
		return new Blog(g, author, rssFeed);
	}

}
