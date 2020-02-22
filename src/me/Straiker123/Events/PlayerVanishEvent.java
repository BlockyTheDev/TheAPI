package me.Straiker123.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerVanishEvent extends Event implements Cancellable {
	Player p;
	boolean v,req;
	String s;
	public PlayerVanishEvent(Player player, String perm, boolean b, boolean req) {
		p=player;
		v=b;
		this.req=req;
		s=perm;
	}

	public String getPermission() {
		return s;
	}
	
	public boolean requiredPermission() {
		return req;
	}
	
	public void setPermission(String perm) {
		s=perm;
	}
	
	@Override
	public boolean isCancelled() {
		return cancel;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public boolean vanish() {
		return v;
	}
	
	public void setVanish(boolean vanish) {
		v=vanish;
	}
	
	boolean cancel;
	@Override
	public void setCancelled(boolean c) {
		cancel=c;
	}
	
	@Override
	public HandlerList getHandlers() {
		return new HandlerList();
	}
	public static HandlerList getHandlerList() {
		return new HandlerList();
	}


}