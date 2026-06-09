package ru.itis.meshy.sharing;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.data.MetadataEncoder;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.system.Clock;
import ru.itis.meshy.api.forum.Forum;
import ru.itis.meshy.api.forum.ForumFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.util.ValidationUtils.checkLength;
import static ru.itis.messaging_engine.util.ValidationUtils.checkSize;
import static ru.itis.meshy.api.forum.ForumConstants.FORUM_SALT_LENGTH;
import static ru.itis.meshy.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH;

@Immutable
@NotNullByDefault
class ForumSharingValidator extends SharingValidator {

	private final ForumFactory forumFactory;

	@Inject
	ForumSharingValidator(MessageEncoder messageEncoder,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ForumFactory forumFactory) {
		super(messageEncoder, clientHelper, metadataEncoder, clock);
		this.forumFactory = forumFactory;
	}

	@Override
	protected GroupId validateDescriptor(BdfList descriptor)
			throws FormatException {
		// Name, salt
		checkSize(descriptor, 2);
		String name = descriptor.getString(0);
		checkLength(name, 1, MAX_FORUM_NAME_LENGTH);
		byte[] salt = descriptor.getRaw(1);
		checkLength(salt, FORUM_SALT_LENGTH);
		Forum forum = forumFactory.createForum(name, salt);
		return forum.getId();
	}

}
