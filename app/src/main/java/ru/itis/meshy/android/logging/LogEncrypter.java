package ru.itis.meshy.android.logging;

import androidx.annotation.Nullable;

import ru.itis.messaging_engine.util.AndroidUtils;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface LogEncrypter {
	/**
	 * Writes encrypted log records to {@link AndroidUtils#getLogcatFile}
	 * and returns the encryption key if everything went fine.
	 */
	@Nullable
	byte[] encryptLogs();
}
