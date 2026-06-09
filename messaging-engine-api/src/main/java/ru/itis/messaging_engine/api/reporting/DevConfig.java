package ru.itis.messaging_engine.api.reporting;

import ru.itis.messaging_engine.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

@NotNullByDefault
public interface DevConfig {

	PublicKey getDevPublicKey();

	String getDevOnionAddress();

	File getReportDir();

	File getLogcatFile();
}
