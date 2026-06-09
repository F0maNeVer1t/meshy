package ru.itis.meshy.api.forum;

import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.meshy.api.client.NamedGroup;
import ru.itis.meshy.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class Forum extends NamedGroup implements Shareable {

	public Forum(Group group, String name, byte[] salt) {
		super(group, name, salt);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Forum && super.equals(o);
	}

}
