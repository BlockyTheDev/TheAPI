package me.devtec.shared.events.api;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.events.Event;
import me.devtec.shared.sockets.SocketClient;

public class ClientReceiveMessageEvent extends Event {
	private final Config data;
	private final SocketClient client;

	public ClientReceiveMessageEvent(SocketClient client, Config data) {
		this.data = data;
		this.client = client;
	}

	public SocketClient getClient() {
		return this.client;
	}

	public Config getInput() {
		return this.data;
	}
}
