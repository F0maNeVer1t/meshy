package ru.itis.messaging_engine.transport.agreement;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.crypto.KeyPair;
import ru.itis.messaging_engine.api.crypto.PrivateKey;
import ru.itis.messaging_engine.api.crypto.PublicKey;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.api.Bytes.compare;
import static ru.itis.messaging_engine.transport.agreement.TransportKeyAgreementConstants.ROOT_KEY_LABEL;

@Immutable
@NotNullByDefault
class TransportKeyAgreementCryptoImpl implements TransportKeyAgreementCrypto {

	private final CryptoComponent crypto;

	@Inject
	TransportKeyAgreementCryptoImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public KeyPair generateKeyPair() {
		return crypto.generateAgreementKeyPair();
	}

	@Override
	public SecretKey deriveRootKey(KeyPair localKeyPair,
			PublicKey remotePublicKey) throws GeneralSecurityException {
		byte[] theirPublic = remotePublicKey.getEncoded();
		byte[] ourPublic = localKeyPair.getPublic().getEncoded();
		boolean alice = compare(ourPublic, theirPublic) < 0;
		byte[][] inputs = {
				alice ? ourPublic : theirPublic,
				alice ? theirPublic : ourPublic
		};
		return crypto.deriveSharedSecret(ROOT_KEY_LABEL, remotePublicKey,
				localKeyPair, inputs);
	}

	@Override
	public PublicKey parsePublicKey(byte[] encoded) throws FormatException {
		try {
			return crypto.getAgreementKeyParser().parsePublicKey(encoded);
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
	}

	@Override
	public PrivateKey parsePrivateKey(byte[] encoded) throws FormatException {
		try {
			return crypto.getAgreementKeyParser().parsePrivateKey(encoded);
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
	}
}
