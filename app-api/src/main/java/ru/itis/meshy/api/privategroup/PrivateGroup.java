package ru.itis.meshy.api.privategroup;

import ru.itis.messaging_engine.api.identity.Author;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.meshy.api.client.NamedGroup;
import ru.itis.meshy.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class PrivateGroup extends NamedGroup implements Shareable {

	private final Author creator;

	public PrivateGroup(Group group, String name, Author creator, byte[] salt) {
		super(group, name, salt);
		this.creator = creator;
	}

	public Author getCreator() {
		return creator;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PrivateGroup && super.equals(o);
	}

}
