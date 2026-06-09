package ru.itis.messaging_engine.plugin.tcp;

import ru.itis.messaging_engine.api.plugin.Plugin;
import ru.itis.messaging_engine.api.plugin.duplex.AbstractDuplexTransportConnection;
import ru.itis.messaging_engine.util.IoUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
class TcpTransportConnection extends AbstractDuplexTransportConnection {

	private final Socket socket;

	TcpTransportConnection(Plugin plugin, Socket socket) {
		super(plugin);
		this.socket = socket;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return IoUtils.getInputStream(socket);
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return IoUtils.getOutputStream(socket);
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		socket.close();
	}
}
