package ru.itis.meshy.api.client;

import ru.itis.messaging_engine.api.sync.Group;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public abstract class NamedGroup extends BaseGroup {

	private final String name;
	private final byte[] salt;

	public NamedGroup(Group group, String name, byte[] salt) {
		super(group);
		this.name = name;
		this.salt = salt;
	}

	public String getName() {
		return name;
	}

	public byte[] getSalt() {
		return salt;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof NamedGroup && super.equals(o);
	}

}
