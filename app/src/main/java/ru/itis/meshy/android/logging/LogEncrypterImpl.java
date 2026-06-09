package ru.itis.meshy.android.logging;

import static ru.itis.messaging_engine .util.LogUtils.logException;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import androidx.annotation.Nullable;

import ru.itis.messaging_engine.api.crypto.CryptoComponent;
import ru.itis.messaging_engine.api.crypto.SecretKey;
import ru.itis.messaging_engine.api.reporting.DevConfig;
import ru.itis.messaging_engine.api.transport.StreamWriter;
import ru.itis.messaging_engine.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.inject.Inject;

@NotNullByDefault
class LogEncrypterImpl implements LogEncrypter {

	private static final Logger LOG =
			getLogger(LogEncrypterImpl.class.getName());

	private final DevConfig devConfig;
	private final CachingLogHandler logHandler;
	private final CryptoComponent crypto;
	private final StreamWriterFactory streamWriterFactory;

	@Inject
	LogEncrypterImpl(DevConfig devConfig,
			CachingLogHandler logHandler,
			CryptoComponent crypto,
			StreamWriterFactory streamWriterFactory) {
		this.devConfig = devConfig;
		this.logHandler = logHandler;
		this.crypto = crypto;
		this.streamWriterFactory = streamWriterFactory;
	}

	@Nullable
	@Override
	public byte[] encryptLogs() {
		SecretKey logKey = crypto.generateSecretKey();
		File logFile = devConfig.getLogcatFile();
		try (OutputStream out = new FileOutputStream(logFile)) {
			StreamWriter streamWriter =
					streamWriterFactory.createLogStreamWriter(out, logKey);
			Writer writer =
					new OutputStreamWriter(streamWriter.getOutputStream());
			writeLogString(writer);
			writer.close();
			return logKey.getBytes();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	private void writeLogString(Writer writer) throws IOException {
		Formatter formatter = new BriefLogFormatter();
		for (LogRecord record : logHandler.getRecentLogRecords()) {
			String formatted = formatter.format(record);
			writer.append(formatted).append('\n');
		}
	}

}
