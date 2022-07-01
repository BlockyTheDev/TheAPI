package me.devtec.theapi.bukkit.packetlistener;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface PacketHandler<C> {

	public C get(Player player);

	public void add(Player player);

	public void remove(C channel);

	public boolean has(C channel);

	public void close();

	public default void send(Player player, Object packet) {
		this.send(this.get(player), packet);
	}

	public void send(C channel, Object packet);

	public default void send(Collection<? extends Player> onlinePlayers, Object packet) {
		onlinePlayers.forEach(player -> this.send(player, packet));
	}
}