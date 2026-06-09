package ru.itis.meshy.android.reporting;

import static ru.itis.meshy.android.util.UiUtils.startDevReportActivity;

import android.app.Application;
import android.os.Process;

import ru.itis.meshy.android.logging.LogEncrypter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.inject.Inject;

@NotNullByDefault
class MeshyExceptionHandler implements UncaughtExceptionHandler {

	private final Application app;
	private final LogEncrypter logEncrypter;
	private final long appStartTime;

	@Inject
    MeshyExceptionHandler(Application app, LogEncrypter logEncrypter) {
		this.app = app;
		this.logEncrypter = logEncrypter;
		appStartTime = System.currentTimeMillis();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// encrypt logs to disk before handing over to new process
		// the intent has limited space, so we can't reliably store them there.
		byte[] logKey = logEncrypter.encryptLogs();

		// activity runs in its own process, so we can kill the old one
		startDevReportActivity(app.getApplicationContext(),
				CrashReportActivity.class, e, appStartTime, logKey, null);
		Process.killProcess(Process.myPid());
		System.exit(10);
	}

}
