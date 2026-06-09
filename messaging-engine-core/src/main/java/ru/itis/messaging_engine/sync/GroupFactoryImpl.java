package ru.itis.messaging_engine.sync;

import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.sync.ClientId;
import ru.itis.messaging_engine.api.sync.Group;
import ru.itis.messaging_engine.api.sync.GroupFactory;
import ru.itis.messaging_engine.api.sync.GroupId;
import ru.itis.messaging_engine.util.ByteUtils;
import ru.itis.messaging_engine.util.StringUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.sync.Group.FORMAT_VERSION;
import static ru.itis.messaging_engine.api.sync.GroupId.LABEL;
import static ru.itis.messaging_engine.util.ByteUtils.INT_32_BYTES;

@Immutable
@NotNullByDefault
class GroupFactoryImpl implements GroupFactory {

	private static final byte[] FORMAT_VERSION_BYTES =
			new byte[] {FORMAT_VERSION};

	private final CryptoComponent crypto;

	@Inject
	GroupFactoryImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public Group createGroup(ClientId c, int majorVersion, byte[] descriptor) {
		byte[] majorVersionBytes = new byte[INT_32_BYTES];
		ByteUtils.writeUint32(majorVersion, majorVersionBytes, 0);
		byte[] hash = crypto.hash(LABEL, FORMAT_VERSION_BYTES,
				StringUtils.toUtf8(c.getString()), majorVersionBytes,
				descriptor);
		return new Group(new GroupId(hash), c, majorVersion, descriptor);
	}
}
