package ru.itis.messaging_engine.client;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.client.ClientHelper;
import ru.itis.messaging_engine.api.client.ContactGroupFactory;
import ru.itis.messaging_engine.api.contact.Contact;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.identity.AuthorId;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.GroupFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ContactGroupFactoryImpl implements ContactGroupFactory {

	private static final byte[] LOCAL_GROUP_DESCRIPTOR = new byte[0];

	private final GroupFactory groupFactory;
	private final ClientHelper clientHelper;

	@Inject
	ContactGroupFactoryImpl(GroupFactory groupFactory,
			ClientHelper clientHelper) {
		this.groupFactory = groupFactory;
		this.clientHelper = clientHelper;
	}

	@Override
	public Group createLocalGroup(ClientId clientId, int majorVersion) {
		return groupFactory.createGroup(clientId, majorVersion,
				LOCAL_GROUP_DESCRIPTOR);
	}

	@Override
	public Group createContactGroup(ClientId clientId, int majorVersion,
			Contact contact) {
		AuthorId local = contact.getLocalAuthorId();
		AuthorId remote = contact.getAuthor().getId();
		byte[] descriptor = createGroupDescriptor(local, remote);
		return groupFactory.createGroup(clientId, majorVersion, descriptor);
	}

	@Override
	public Group createContactGroup(ClientId clientId, int majorVersion,
			AuthorId authorId1, AuthorId authorId2) {
		byte[] descriptor = createGroupDescriptor(authorId1, authorId2);
		return groupFactory.createGroup(clientId, majorVersion, descriptor);
	}

	private byte[] createGroupDescriptor(AuthorId local, AuthorId remote) {
		try {
			if (local.compareTo(remote) < 0)
				return clientHelper.toByteArray(BdfList.of(local, remote));
			else return clientHelper.toByteArray(BdfList.of(remote, local));
		} catch (FormatException e) {
			throw new RuntimeException(e);
		}
	}
}
