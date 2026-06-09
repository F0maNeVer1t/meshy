package ru.itis.meshy.api.avatar;

import ru.itis.messaging_engine.api.Pair;
import ru.itis.messaging_engine.api.data.BdfDictionary;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.api.sync.Message;

import java.io.IOException;
import java.io.InputStream;

public interface AvatarMessageEncoder {
	/**
	 * Returns an update message and its metadata.
	 */
	Pair<Message, BdfDictionary> encodeUpdateMessage(GroupId groupId,
			long version, String contentType, InputStream in)
			throws IOException;
}
