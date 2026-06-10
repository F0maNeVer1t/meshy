package ru.itis.messaging_engine.plugin.wifidirect;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLock;
import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import ru.itis.messaging_engine.api.io.TimeoutMonitor;
import ru.itis.messaging_engine.api.plugin.Plugin;
import ru.itis.messaging_engine.api.plugin.duplex.AbstractDuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@NotNullByDefault
class AndroidWifiDirectTransportConnection
		extends AbstractDuplexTransportConnection {

	private final Socket socket;
	private final InputStream in;
	private final AndroidWakeLock wakeLock;

	AndroidWifiDirectTransportConnection(Plugin plugin,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor,
			Socket socket) throws IOException {
		super(plugin);
		this.socket = socket;
		in = timeoutMonitor.createTimeoutInputStream(
				socket.getInputStream(),
				plugin.getMaxIdleTime() * 2L);
		wakeLock = wakeLockManager.createWakeLock("WifiDirectConnection");
		wakeLock.acquire();
	}

	@Override
	protected InputStream getInputStream() {
		return in;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		try {
			socket.close();
			in.close();
		} finally {
			wakeLock.release();
		}
	}
}
