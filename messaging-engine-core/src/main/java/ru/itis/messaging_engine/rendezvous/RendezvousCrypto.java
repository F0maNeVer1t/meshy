package ru.itis.messaging_engine.rendezvous;

import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.rendezvous.KeyMaterialSource;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RendezvousCrypto {

	SecretKey deriveRendezvousKey(SecretKey staticMasterKey);

	KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t);
}
