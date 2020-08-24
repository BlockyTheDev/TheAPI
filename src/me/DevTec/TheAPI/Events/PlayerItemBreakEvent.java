package me.DevTec.TheAPI.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerItemBreakEvent extends Event implements Cancellable {
	public PlayerItemBreakEvent(Player p, ItemStack item) {
		s = p;
		i = item;
	}

	Player s;
	ItemStack i;

	public ItemStack getItem() {
		return i;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	boolean cancel;

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	public Player getPlayer() {
		return s;
	}

	private static final HandlerList cs = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return cs;
	}

	public static HandlerList getHandlerList() {
		return cs;
	}

}