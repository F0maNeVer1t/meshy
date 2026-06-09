package ru.itis.messaging_engine.plugin.tor;

interface TorRendezvousCrypto {

	static final int SEED_BYTES = 32;

	String getOnion(byte[] seed);

	String getPrivateKeyBlob(byte[] seed);
}
