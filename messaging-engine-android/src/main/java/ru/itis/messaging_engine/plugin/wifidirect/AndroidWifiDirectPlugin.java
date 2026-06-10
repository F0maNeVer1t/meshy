package ru.itis.messaging_engine.plugin.wifidirect;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import ru.itis.messaging_engine.api.Pair;
import ru.itis.messaging_engine.api.data.BdfList;
import ru.itis.messaging_engine.api.event.Event;
import ru.itis.messaging_engine.api.event.EventListener;
import ru.itis.messaging_engine.api.keyagreement.KeyAgreementListener;
import ru.itis.messaging_engine.api.plugin.Backoff;
import ru.itis.messaging_engine.api.plugin.ConnectionHandler;
import ru.itis.messaging_engine.api.plugin.PluginCallback;
import ru.itis.messaging_engine.api.plugin.PluginException;
import ru.itis.messaging_engine.api.plugin.TransportId;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexPlugin;
import ru.itis.messaging_engine.api.plugin.duplex.DuplexTransportConnection;
import ru.itis.messaging_engine.api.properties.TransportProperties;
import ru.itis.messaging_engine.api.rendezvous.KeyMaterialSource;
import ru.itis.messaging_engine.api.rendezvous.RendezvousEndpoint;
import ru.itis.messaging_engine.api.settings.Settings;
import ru.itis.messaging_engine.api.settings.event.SettingsUpdatedEvent;
import ru.itis.messaging_engine.util.IoUtils;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static ru.itis.messaging_engine.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static ru.itis.messaging_engine.api.plugin.Plugin.REASON_USER;
import static ru.itis.messaging_engine.api.plugin.Plugin.State.ACTIVE;
import static ru.itis.messaging_engine.api.plugin.Plugin.State.DISABLED;
import static ru.itis.messaging_engine.api.plugin.Plugin.State.INACTIVE;
import static ru.itis.messaging_engine.api.plugin.Plugin.State.STARTING_STOPPING;
import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.DEFAULT_PREF_PLUGIN_ENABLE;
import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.ID;
import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.PROP_GROUP_OWNER_ADDRESS;
import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.PROP_GROUP_OWNER_PORT;
import static ru.itis.messaging_engine.api.plugin.WifiDirectConstants.SERVER_PORT;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
@SuppressLint("MissingPermission")
class AndroidWifiDirectPlugin implements DuplexPlugin, EventListener {

	private static final Logger LOG =
			getLogger(AndroidWifiDirectPlugin.class.getName());
	private static final String TAG = "MeshyWifiDirect";

	private static final int SOCKET_TIMEOUT = 10_000;

	private final Executor ioExecutor;
	private final Application app;
	private final Backoff backoff;
	private final PluginCallback callback;
	private final AndroidWifiDirectConnectionFactory connectionFactory;
	private final long maxLatency;
	private final int maxIdleTime;

	private final AtomicBoolean used = new AtomicBoolean(false);

	private volatile State state = STARTING_STOPPING;
	private volatile int reasonsDisabled = 0;
	private volatile WifiP2pManager manager = null;
	private volatile WifiP2pManager.Channel channel = null;
	private volatile ServerSocket serverSocket = null;
	private volatile WifiDirectReceiver receiver = null;
	private volatile boolean groupConnected = false;

	AndroidWifiDirectPlugin(Executor ioExecutor,
			Application app,
			Backoff backoff,
			PluginCallback callback,
			AndroidWifiDirectConnectionFactory connectionFactory,
			long maxLatency,
			int maxIdleTime) {
		this.ioExecutor = ioExecutor;
		this.app = app;
		this.backoff = backoff;
		this.callback = callback;
		this.connectionFactory = connectionFactory;
		this.maxLatency = maxLatency;
		this.maxIdleTime = maxIdleTime;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return maxLatency;
	}

	@Override
	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	@Override
	public void start() throws PluginException {
		if (used.getAndSet(true)) throw new PluginException();

		Settings settings = callback.getSettings();
		boolean enabledByUser = settings.getBoolean(PREF_PLUGIN_ENABLE,
				DEFAULT_PREF_PLUGIN_ENABLE);
		if (!enabledByUser) {
			Log.d(TAG, "start(): disabled by user");
			reasonsDisabled = REASON_USER;
			state = DISABLED;
			return;
		}

		manager = (WifiP2pManager) app.getSystemService(
				Context.WIFI_P2P_SERVICE);
		if (manager == null) {
			state = INACTIVE;
			return;
		}

		channel = manager.initialize(app, app.getMainLooper(), null);
		if (channel == null) {
			state = INACTIVE;
			return;
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		receiver = new WifiDirectReceiver();
		app.registerReceiver(receiver, filter);

		state = ACTIVE;
		Log.d(TAG, "start(): plugin ACTIVE, listening on port " + SERVER_PORT);
		ioExecutor.execute(this::acceptContactConnections);
	}

	@Override
	public void stop() {
		Log.d(TAG, "stop()");
		state = STARTING_STOPPING;
		if (receiver != null) {
			app.unregisterReceiver(receiver);
			receiver = null;
		}
		ServerSocket ss = serverSocket;
		if (ss != null) IoUtils.tryToClose(ss, LOG, WARNING);
		if (manager != null && channel != null) {
			manager.removeGroup(channel, null);
		}
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			if (s.getNamespace().equals(ID.getString())) {
				ioExecutor.execute(
						() -> onSettingsUpdated(s.getSettings()));
			}
		}
	}

	private void onSettingsUpdated(Settings settings) {
		boolean enabledByUser = settings.getBoolean(PREF_PLUGIN_ENABLE,
				DEFAULT_PREF_PLUGIN_ENABLE);
		if (enabledByUser && getState() == DISABLED) {
			Log.d(TAG, "Enabled by user, starting Wi-Fi Direct");
			reasonsDisabled = 0;
			state = INACTIVE;
			try {
				startWifiDirect();
			} catch (PluginException ex) {
				Log.w(TAG, "Failed to start Wi-Fi Direct after enable");
			}
		} else if (!enabledByUser && getState() != DISABLED) {
			Log.d(TAG, "Disabled by user");
			stopWifiDirect();
			reasonsDisabled = REASON_USER;
			state = DISABLED;
			callback.pluginStateChanged(getState());
		}
	}

	private void startWifiDirect() throws PluginException {
		manager = (WifiP2pManager) app.getSystemService(
				Context.WIFI_P2P_SERVICE);
		if (manager == null) {
			state = INACTIVE;
			callback.pluginStateChanged(getState());
			return;
		}

		channel = manager.initialize(app, app.getMainLooper(), null);
		if (channel == null) {
			state = INACTIVE;
			callback.pluginStateChanged(getState());
			return;
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		receiver = new WifiDirectReceiver();
		app.registerReceiver(receiver, filter);

		state = ACTIVE;
		callback.pluginStateChanged(getState());
		ioExecutor.execute(this::acceptContactConnections);
	}

	private void stopWifiDirect() {
		if (receiver != null) {
			app.unregisterReceiver(receiver);
			receiver = null;
		}
		ServerSocket ss = serverSocket;
		if (ss != null) IoUtils.tryToClose(ss, LOG, WARNING);
		if (manager != null && channel != null) {
			manager.removeGroup(channel, null);
		}
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public int getReasonsDisabled() {
		return reasonsDisabled;
	}

	@Override
	public boolean shouldPoll() {
		return true;
	}

	@Override
	public int getPollingInterval() {
		return backoff.getPollingInterval();
	}

	@Override
	public void poll(Collection<Pair<TransportProperties, ConnectionHandler>>
			properties) {
		if (getState() != ACTIVE) return;
		Log.d(TAG, "poll(): state=ACTIVE, discovering peers");
		backoff.increment();
		discoverPeers();
		for (Pair<TransportProperties, ConnectionHandler> p : properties) {
			TransportProperties props = p.getFirst();
			ConnectionHandler handler = p.getSecond();
			String goAddress = props.get(PROP_GROUP_OWNER_ADDRESS);
			String portStr = props.get(PROP_GROUP_OWNER_PORT);
			if (goAddress != null && portStr != null) {
				ioExecutor.execute(() -> {
					DuplexTransportConnection conn =
							connectToGroupOwner(goAddress,
									parsePort(portStr));
					if (conn != null) {
						backoff.reset();
						handler.handleConnection(conn);
					}
				});
			}
		}
	}

	@Override
	@Nullable
	public DuplexTransportConnection createConnection(TransportProperties p) {
		if (getState() != ACTIVE) return null;
		String goAddress = p.get(PROP_GROUP_OWNER_ADDRESS);
		String portStr = p.get(PROP_GROUP_OWNER_PORT);
		if (goAddress == null || portStr == null) return null;
		return connectToGroupOwner(goAddress, parsePort(portStr));
	}

	@Override
	public boolean supportsKeyAgreement() {
		return false;
	}

	@Override
	@Nullable
	public KeyAgreementListener createKeyAgreementListener(
			byte[] localCommitment) {
		return null;
	}

	@Override
	@Nullable
	public DuplexTransportConnection createKeyAgreementConnection(
			byte[] remoteCommitment, BdfList descriptor) {
		return null;
	}

	@Override
	public boolean supportsRendezvous() {
		return false;
	}

	@Override
	@Nullable
	public RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k,
			boolean alice, ConnectionHandler incoming) {
		return null;
	}

	private void acceptContactConnections() {
		Log.d(TAG, "acceptContactConnections() started");
		while (getState() == ACTIVE) {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket();
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(SERVER_PORT));
				serverSocket = ss;
				Log.d(TAG, "ServerSocket listening on port " + SERVER_PORT);
				while (getState() == ACTIVE) {
					Socket s = ss.accept();
					Log.d(TAG, "Accepted connection from " +
							s.getInetAddress());
					if (getState() != ACTIVE) {
						s.close();
						break;
					}
					backoff.reset();
					DuplexTransportConnection conn =
							connectionFactory.wrapSocket(this, s);
					callback.handleConnection(conn);
				}
			} catch (IOException e) {
				if (getState() == ACTIVE && LOG.isLoggable(WARNING))
					LOG.log(WARNING, "ServerSocket failed", e);
			} finally {
				if (ss != null) IoUtils.tryToClose(ss, LOG, WARNING);
			}
			if (getState() != ACTIVE) return;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private int parsePort(String portStr) {
		try {
			return Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			return SERVER_PORT;
		}
	}

	@Nullable
	private DuplexTransportConnection connectToGroupOwner(
			String address, int port) {
		Socket s = new Socket();
		try {
			InetAddress addr = InetAddress.getByName(address);
			s.connect(new InetSocketAddress(addr, port), SOCKET_TIMEOUT);
			return connectionFactory.wrapSocket(this, s);
		} catch (IOException e) {
			Log.w(TAG, "Failed to connect to GO at " + address +
					":" + port + " - " + e.getMessage());
			IoUtils.tryToClose(s, LOG, WARNING);
			return null;
		}
	}

	private void discoverPeers() {
		WifiP2pManager m = manager;
		WifiP2pManager.Channel c = channel;
		if (m == null || c == null) return;

		m.discoverPeers(c, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Peer discovery started");
			}

			@Override
			public void onFailure(int reason) {
				Log.w(TAG, "Peer discovery failed, reason=" + reason);
			}
		});
	}

	private void connectToPeer(WifiP2pDevice device) {
		WifiP2pManager m = manager;
		WifiP2pManager.Channel c = channel;
		if (m == null || c == null) return;

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.groupOwnerIntent = 0;

		m.connect(c, config, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Connecting to peer: " + device.deviceAddress);
			}

			@Override
			public void onFailure(int reason) {
				Log.w(TAG, "Connect to peer failed, reason=" + reason);
			}
		});
	}

	private void onConnectionInfoAvailable(WifiP2pInfo info) {
		groupConnected = info.groupFormed;
		if (info.groupFormed && !info.isGroupOwner) {
			InetAddress goAddress = info.groupOwnerAddress;
			if (goAddress == null) return;
			Log.d(TAG, "Connected as client, GO at " +
					goAddress.getHostAddress());
			ioExecutor.execute(() -> {
				// Wait for P2P network interface to be fully ready
				for (int attempt = 1; attempt <= 5; attempt++) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					Log.d(TAG, "TCP connect attempt " + attempt +
							" to GO at " + goAddress.getHostAddress());
					DuplexTransportConnection conn = connectToGroupOwner(
							goAddress.getHostAddress(), SERVER_PORT);
					if (conn != null) {
						Log.d(TAG, "TCP connection established to GO!");
						backoff.reset();
						callback.handleConnection(conn);
						return;
					}
				}
				Log.w(TAG, "All TCP connect attempts to GO failed");
			});
		} else if (info.groupFormed) {
			Log.d(TAG, "This device is Group Owner");
		}
	}

	private void onPeersAvailable(WifiP2pDeviceList peerList) {
		Collection<WifiP2pDevice> devices = peerList.getDeviceList();
		if (devices.isEmpty()) {
			Log.d(TAG, "No peers found");
			return;
		}
		Log.d(TAG, "Found " + devices.size() + " Wi-Fi Direct peers");
		if (groupConnected) {
			Log.d(TAG, "Already in a group, skipping connect");
			return;
		}
		for (WifiP2pDevice device : devices) {
			if (device.status == WifiP2pDevice.AVAILABLE) {
				connectToPeer(device);
				break;
			}
		}
	}

	private class WifiDirectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null) return;

			switch (action) {
				case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
					int wifiState = intent.getIntExtra(
							WifiP2pManager.EXTRA_WIFI_STATE, -1);
					if (wifiState ==
							WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
						Log.d(TAG, "Broadcast: Wi-Fi Direct enabled");
					} else {
						Log.d(TAG, "Broadcast: Wi-Fi Direct disabled");
					}
					break;
				}
				case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
					WifiP2pManager m = manager;
					WifiP2pManager.Channel c = channel;
					if (m != null && c != null) {
						m.requestPeers(c,
								peers -> onPeersAvailable(peers));
					}
					break;
				}
				case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
					WifiP2pManager m = manager;
					WifiP2pManager.Channel c = channel;
					if (m == null || c == null) break;
					NetworkInfo networkInfo = intent.getParcelableExtra(
							WifiP2pManager.EXTRA_NETWORK_INFO);
					if (networkInfo != null && networkInfo.isConnected()) {
						m.requestConnectionInfo(c,
								info -> onConnectionInfoAvailable(info));
					}
					break;
				}
			}
		}
	}
}
