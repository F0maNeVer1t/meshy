package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.meshy.api.forum.Forum;
import ru.itis.meshy.api.forum.ForumFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ForumMessageParserImpl extends MessageParserImpl<Forum> {

	private final ForumFactory forumFactory;

	@Inject
	ForumMessageParserImpl(ClientHelper clientHelper,
			ForumFactory forumFactory) {
		super(clientHelper);
		this.forumFactory = forumFactory;
	}

	@Override
	public Forum createShareable(BdfList descriptor) throws FormatException {
		// Name, salt
		String name = descriptor.getString(0);
		byte[] salt = descriptor.getRaw(1);
		return forumFactory.createForum(name, salt);
	}

}
