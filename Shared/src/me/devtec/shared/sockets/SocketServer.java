package me.devtec.shared.sockets;

import java.util.Map;

public interface SocketServer {
	public static final int ACCEPTED = 1;
	public static final int DECNILED = 2;
	public static final int DECNILED_PLUGIN = 3;
	public static final int PROCESS_LOGIN = 4;
	public static final int RECEIVE_DATA = 10;
	public static final int RECEIVE_NAME = 11;

	public String serverName();

	public Map<String, SocketClient> connectedClients();

	public boolean isRunning();

	public void start();

	public void stop();
}
