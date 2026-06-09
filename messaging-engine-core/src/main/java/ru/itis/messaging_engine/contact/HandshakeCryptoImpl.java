package ru.itis.messaging_engine.contact;

import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.crypto.KeyPair;
import ru.itis.messaging_engine.api.crypto.PublicKey;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static ru.itis.messaging_engine.contact.HandshakeConstants.ALICE_PROOF_LABEL;
import static ru.itis.messaging_engine.contact.HandshakeConstants.BOB_PROOF_LABEL;
import static ru.itis.messaging_engine.contact.HandshakeConstants.MASTER_KEY_LABEL_0_0;
import static ru.itis.messaging_engine.contact.HandshakeConstants.MASTER_KEY_LABEL_0_1;

@Immutable
@NotNullByDefault
class HandshakeCryptoImpl implements HandshakeCrypto {

	private final CryptoComponent crypto;

	@Inject
	HandshakeCryptoImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public KeyPair generateEphemeralKeyPair() {
		return crypto.generateAgreementKeyPair();
	}

	@Override
	@Deprecated
	public SecretKey deriveMasterKey_0_0(PublicKey theirStaticPublicKey,
			PublicKey theirEphemeralPublicKey, KeyPair ourStaticKeyPair,
			KeyPair ourEphemeralKeyPair, boolean alice) throws
			GeneralSecurityException {
		byte[] theirStatic = theirStaticPublicKey.getEncoded();
		byte[] theirEphemeral = theirEphemeralPublicKey.getEncoded();
		byte[] ourStatic = ourStaticKeyPair.getPublic().getEncoded();
		byte[] ourEphemeral = ourEphemeralKeyPair.getPublic().getEncoded();
		byte[][] inputs = {
				alice ? ourStatic : theirStatic,
				alice ? theirStatic : ourStatic,
				alice ? ourEphemeral : theirEphemeral,
				alice ? theirEphemeral : ourEphemeral
		};
		return crypto.deriveSharedSecretBadly(MASTER_KEY_LABEL_0_0,
				theirStaticPublicKey, theirEphemeralPublicKey,
				ourStaticKeyPair, ourEphemeralKeyPair, alice, inputs);
	}

	@Override
	public SecretKey deriveMasterKey_0_1(PublicKey theirStaticPublicKey,
			PublicKey theirEphemeralPublicKey, KeyPair ourStaticKeyPair,
			KeyPair ourEphemeralKeyPair, boolean alice) throws
			GeneralSecurityException {
		byte[] theirStatic = theirStaticPublicKey.getEncoded();
		byte[] theirEphemeral = theirEphemeralPublicKey.getEncoded();
		byte[] ourStatic = ourStaticKeyPair.getPublic().getEncoded();
		byte[] ourEphemeral = ourEphemeralKeyPair.getPublic().getEncoded();
		byte[][] inputs = {
				alice ? ourStatic : theirStatic,
				alice ? theirStatic : ourStatic,
				alice ? ourEphemeral : theirEphemeral,
				alice ? theirEphemeral : ourEphemeral
		};
		return crypto.deriveSharedSecret(MASTER_KEY_LABEL_0_1,
				theirStaticPublicKey, theirEphemeralPublicKey,
				ourStaticKeyPair, ourEphemeralKeyPair, alice, inputs);
	}

	@Override
	public byte[] proveOwnership(SecretKey masterKey, boolean alice) {
		String label = alice ? ALICE_PROOF_LABEL : BOB_PROOF_LABEL;
		return crypto.mac(label, masterKey);
	}

	@Override
	public boolean verifyOwnership(SecretKey masterKey, boolean alice,
			byte[] proof) {
		String label = alice ? ALICE_PROOF_LABEL : BOB_PROOF_LABEL;
		return crypto.verifyMac(proof, label, masterKey);
	}
}
