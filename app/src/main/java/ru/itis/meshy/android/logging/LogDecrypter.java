package ru.itis.meshy.android.logging;

import androidx.annotation.Nullable;

import ru.itis.messaging_engine.util.AndroidUtils;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface LogDecrypter {
	/**
	 * Returns decrypted log records from {@link AndroidUtils#getLogcatFile}
	 * or null if there was an error reading the logs.
	 */
	@Nullable
	String decryptLogs(@Nullable byte[] logKey);
}
