package ru.itis.messaging_engine.transport.agreement;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.crypto.KeyPair;
import ru.itis.messaging_engine.api.crypto.PrivateKey;
import ru.itis.messaging_engine.api.crypto.PublicKey;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

@NotNullByDefault
interface TransportKeyAgreementCrypto {

	KeyPair generateKeyPair();

	SecretKey deriveRootKey(KeyPair localKeyPair, PublicKey remotePublicKey)
			throws GeneralSecurityException;

	PublicKey parsePublicKey(byte[] encoded) throws FormatException;

	PrivateKey parsePrivateKey(byte[] encoded) throws FormatException;
}
