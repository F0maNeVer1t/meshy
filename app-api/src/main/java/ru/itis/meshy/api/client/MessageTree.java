package ru.itis.meshy.api.client;

import ru.itis.messaging_engine.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MessageTree<T extends MessageTree.MessageNode> {

	void add(Collection<T> nodes);

	void add(T node);

	void clear();

	List<T> depthFirstOrder();

	boolean contains(MessageId m);

	@NotNullByDefault
	interface MessageNode {

		MessageId getId();

		@Nullable
		MessageId getParentId();

		void setLevel(int level);

		long getTimestamp();
	}

}
