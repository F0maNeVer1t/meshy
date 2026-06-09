package ru.itis.messaging_engine.crypto;

import ru.itis.messaging_engine.api.crypto.SecretKey;

interface PasswordBasedKdf {

	int chooseCostParameter();

	SecretKey deriveKey(String password, byte[] salt, int cost);
}
