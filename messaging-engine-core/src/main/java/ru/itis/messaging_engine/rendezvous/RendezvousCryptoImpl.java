package ru.itis.messaging_engine.rendezvous;

import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.rendezvous.KeyMaterialSource;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.rendezvous.RendezvousConstants.KEY_MATERIAL_LABEL;
import static ru.itis.messaging_engine.rendezvous.RendezvousConstants.PROTOCOL_VERSION;
import static ru.itis.messaging_engine.rendezvous.RendezvousConstants.RENDEZVOUS_KEY_LABEL;
import static ru.itis.messaging_engine.util.StringUtils.toUtf8;

@Immutable
@NotNullByDefault
class RendezvousCryptoImpl implements RendezvousCrypto {

	private final CryptoComponent crypto;

	@Inject
	RendezvousCryptoImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public SecretKey deriveRendezvousKey(SecretKey staticMasterKey) {
		return crypto.deriveKey(RENDEZVOUS_KEY_LABEL, staticMasterKey,
				new byte[] {PROTOCOL_VERSION});
	}

	@Override
	public KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t) {
		SecretKey sourceKey = crypto.deriveKey(KEY_MATERIAL_LABEL,
				rendezvousKey, toUtf8(t.getString()));
		return new KeyMaterialSourceImpl(sourceKey);
	}
}
