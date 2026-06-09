package ru.itis.messaging_engine.crypto;

import ru.itis.messaging_engine.api.crypto.KeyParser;
import ru.itis.messaging_engine.api.crypto.PrivateKey;
import ru.itis.messaging_engine.api.crypto.PublicKey;
import ru.itis.messaging_engine.api.crypto.SignaturePrivateKey;
import ru.itis.messaging_engine.api.crypto.SignaturePublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class SignatureKeyParser implements KeyParser {

	@Override
	public PublicKey parsePublicKey(byte[] encodedKey)
			throws GeneralSecurityException {
		if (encodedKey.length != 32) throw new GeneralSecurityException();
		return new SignaturePublicKey(encodedKey);
	}

	@Override
	public PrivateKey parsePrivateKey(byte[] encodedKey)
			throws GeneralSecurityException {
		if (encodedKey.length != 32) throw new GeneralSecurityException();
		return new SignaturePrivateKey(encodedKey);
	}
}
