package me.devtec.theapi.scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import me.devtec.shared.Ref;
import me.devtec.shared.components.ComponentAPI;
import me.devtec.shared.dataholder.Config;
import me.devtec.shared.utility.StringUtils;
import me.devtec.theapi.bukkit.BukkitLoader;
import me.devtec.theapi.nms.NmsProvider.Action;
import me.devtec.theapi.nms.NmsProvider.DisplayType;

/**
 * https://gist.github.com/MrZalTy/f8895d84979d49af946fbcc108b1bf2b
 * 
 * @author MrZalTy
 * Forked by StraikerinaCZ
 *
 */
public class ScoreboardAPI {
	private static final String protectId = (new Random().nextLong()+"").substring(0,5);
	
	private static final Config protection = new Config();
	
	protected final Config data = new Config();
	protected Player p;
	protected String player, sbname;
	protected int slott;
	protected String name = "";
	protected boolean destroyed;

	public ScoreboardAPI(Player player) {
		this(player, -1);
	}
	
	/**
	 * 
	 * @param player Player - Holder of scoreboard
	 * @param slot -1 to adaptive slot
	 */
	public ScoreboardAPI(Player player, int slot) {
		p = player;
		slott=slot;
		this.player = player.getName();
		sbname=protectId+this.player;
		if(sbname.length()>16)sbname=sbname.substring(0,16);
		BukkitLoader.getPacketHandler().send(p, createObjectivePacket(0,"§0"));
		Object packet = BukkitLoader.getNmsProvider().packetScoreboardDisplayObjective(1, null);
		Ref.set(packet, "b", sbname);
		BukkitLoader.getPacketHandler().send(p, packet);
	}
	
	public void setSlot(int slot) {
		slott=slot;
	}
	
	public void remove() {
		destroy();
	}

	public void destroy() {
		if(destroyed)return;
		destroyed = true;
		BukkitLoader.getPacketHandler().send(p, createObjectivePacket(1, Ref.isNewerThan(12)?null:""));
		for(String a : data.getKeys(player)){
			Team team = data.getAs(player+"."+a, Team.class);
			if(team!=null) {
				for(Object o : remove(team.currentPlayer, team.name))
					BukkitLoader.getPacketHandler().send(p, o);
			}
		}
		data.clear();
	}

	public void setTitle(String name) {
		setDisplayName(name);
	}

	public void setName(String name) {
		setDisplayName(name);
	}

	public void setDisplayName(String a) {
		destroyed=false;
		String displayName = name;
		if (!Ref.isNewerThan(12) && name.length() > 32)
			name = name.substring(0, 32);
		name = StringUtils.colorize(a);
		if(!name.equals(displayName))
		BukkitLoader.getPacketHandler().send(p, createObjectivePacket(2, name));
	}

	public void addLine(String value) {
		int i = -1;
		Set<String> slots = data.getKeys(player);
		while(!slots.contains(""+(++i)));
		setLine(i, value);
	}

	public synchronized void setLine(int line, String value) {
		value = StringUtils.colorize(value);
		if(getLine(line)!=null && getLine(line).equals(!Ref.isNewerThan(12)?cut(value):value))return;
		Team team = null;
		boolean add = true;
		Set<String> s = data.getKeys(player);
		for(String wd : s) {
			Team t = data.getAs(player+"."+wd, Team.class);
			if(t.slot==line) {
				team=t;
				add=false;
			}
		}
		if(add)team = getTeam(line, line);
		team.setValue(value);
		sendLine(team, line, add);
	}

	private String cut(String a) {
		List<String> d = StringUtils.fixedSplit(a, 17);
		if (a.length() <= 17)return d.get(0);
		if (a.length() <= 34)return d.get(0)+d.get(1);
		String text = d.get(0);
		a=a.substring(d.get(0).length());
		d = StringUtils.fixedSplit(a, 18);
		text+=StringUtils.getLastColors(text)+d.get(0);
		a=a.substring(d.get(0).length());
		d = StringUtils.fixedSplit(a, 17);
		text+=d.get(0);
		return text;
	}

	public synchronized void removeLine(int line) {
		if(!data.exists(player+"."+line))return;
		Team team = getTeam(line, line);
		for(Object o : remove(team.currentPlayer, team.name))
			BukkitLoader.getPacketHandler().send(p, o);
		data.remove(player+"."+line);
	}
	
	public synchronized void removeUpperLines(int line) {
		for(String a : data.getKeys(player)) {
			if(Integer.parseInt(a)>line) {
				Team team = data.getAs(player+"."+a, Team.class);
				for(Object o : remove(team.currentPlayer, team.name))
					BukkitLoader.getPacketHandler().send(p, o);
				data.remove(player+"."+line);
			}
		}
	}

	public String getLine(int line) {
		if (data.exists(player+"."+line) && data.get(player+"."+line)!=null)
			return ((Team) data.get(player+"."+line)).getValue();
		return null;
	}

	public List<String> getLines() {
		List<String> lines = new ArrayList<>();
		for(String line : data.getKeys(player))
			lines.add(((Team) data.get(player+"."+line)).getValue());
		return lines;
	}

	private synchronized void sendLine(Team team, int line, boolean add) {
		destroyed=false;
		team.sendLine(line);
		if(add)
			data.set(player+"."+line, team);
	}

	private Team getTeam(int line, int realPos) {
		if (data.get(player+"."+line)==null)
			data.set(player+"."+line, new Team(line, realPos));
		return data.getAs(player+"."+line, Team.class);
	}
	
	private Object[] create(String prefix, String suffix, String name, String realName, int slot) {
		protection.set(player+"."+name, true);
		Object[] o = new Object[2];
		o[0]=c(0, prefix, suffix, name, realName);
		o[1]=BukkitLoader.getNmsProvider().packetScoreboardScore(Action.CHANGE, sbname, name, slot);
		return o;
	}
	
	private Object[] modify(String prefix, String suffix, String name, String realName, int slot) {
		Object[] o = new Object[2];
		o[0]=c(2, prefix, suffix, name, realName);
		o[1]=BukkitLoader.getNmsProvider().packetScoreboardScore(Action.CHANGE, sbname, name, slot);
		return o;
	}
	
	private Object[] remove(String name, String realName) {
		protection.remove(player+"."+name);
		Object[] o = new Object[2];
		o[0]=c(1, "", "", name,realName);
		o[1]=BukkitLoader.getNmsProvider().packetScoreboardScore(Action.REMOVE, sbname, name, 0);
		return o;
	}
	
	private static final Class<?> sbTeam = Ref.getClass("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$b");
	private static final sun.misc.Unsafe unsafe = (sun.misc.Unsafe) Ref.getNulled(Ref.field(sun.misc.Unsafe.class, "theUnsafe"));
	private static final Object white = Ref.method(Ref.nmsOrOld("EnumChatFormat", "EnumChatFormat"), "a",char.class)==null?
			Ref.invokeStatic(Ref.method(Ref.nmsOrOld("EnumChatFormat", "EnumChatFormat"), "a",int.class), -1):
			Ref.invokeStatic(Ref.method(Ref.nmsOrOld("EnumChatFormat", "EnumChatFormat"), "a",char.class), 'f');

	private Object c(int mode, String prefix, String suffix, String name, String realName) {
		Object packet = BukkitLoader.getNmsProvider().packetScoreboardTeam();
		String always = "ALWAYS";
		if(Ref.isNewerThan(16)) {
			Ref.set(packet, "i", realName);
			try {
				Object o = unsafe.allocateInstance(sbTeam);
				Ref.set(o, "a", BukkitLoader.getNmsProvider().chatBase("{\"text\":\""+name+"\"}"));
				Ref.set(o, "b", BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(prefix, true)));
				Ref.set(o, "c", BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(suffix, true)));
				Ref.set(o, "d", always);
				Ref.set(o, "e", always);
				Ref.set(o, "f", white);
				Ref.set(packet, "k", Optional.of(o));
			} catch (Exception e) {
			}
			Ref.set(packet, "h", mode);
			Ref.set(packet, "j", ImmutableList.copyOf(new String[]{name}));
		}else {
			Ref.set(packet, "a", realName);
			Ref.set(packet, "b", Ref.isNewerThan(12)?BukkitLoader.getNmsProvider().chatBase("{\"text\":\"\"}"):"");
			Ref.set(packet, "c", Ref.isNewerThan(12)?BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(prefix, true)):prefix);
			Ref.set(packet, "d", Ref.isNewerThan(12)?BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(suffix, true)):suffix);
			if(Ref.isNewerThan(7)) {
				Ref.set(packet, "e", always);
				Ref.set(packet, "f", Ref.isNewerThan(8)? always : -1);
				if(Ref.isNewerThan(8))
					Ref.set(packet, "g",Ref.isNewerThan(12)?white:-1);
				Ref.set(packet, Ref.isNewerThan(8)?"i":"h", mode);
				Ref.set(packet, Ref.isNewerThan(8)?"h":"g", ImmutableList.copyOf(new String[]{name}));
			}else {
				Ref.set(packet, "f", mode);
				Ref.set(packet, "e", ImmutableList.copyOf(new String[]{name}));
			}
		}
		return packet;
	}

	private Object createObjectivePacket(int mode, String displayName) {
		Object packet = BukkitLoader.getNmsProvider().packetScoreboardObjective();
		if(Ref.isNewerThan(16)) {
			Ref.set(packet, "d", sbname);
			Ref.set(packet, "e", BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(displayName, true)));
			Ref.set(packet, "f", BukkitLoader.getNmsProvider().getEnumScoreboardHealthDisplay(DisplayType.INTEGER));
			Ref.set(packet, "g", mode);
		}else {
			Ref.set(packet, "a", sbname);
			Ref.set(packet, "b", Ref.isNewerThan(12)?BukkitLoader.getNmsProvider().toIChatBaseComponent(ComponentAPI.toComponent(displayName, true)):displayName);
			if(Ref.isNewerThan(7)) {
				Ref.set(packet, "c", BukkitLoader.getNmsProvider().getEnumScoreboardHealthDisplay(DisplayType.INTEGER));
				Ref.set(packet, "d", mode);
			}else
				Ref.set(packet, "c", mode);
		}
		return packet;
	}
	
	public class Team {
		private String prefix = "", suffix = "", currentPlayer, old;
		private String name, format;
		private int slot;
		private boolean changed, first = true;
		private Team(int slot, int realPos) {
			String s = "" + realPos;
			for(int i = ChatColor.values().length-1; i > -1; --i) {
				s=s.replace(i+"", ChatColor.values()[i]+"");
			}
			currentPlayer = s;
			if(Ref.isOlderThan(13)) {
				currentPlayer+="§f";
				format=currentPlayer;
			}else format=null;
			this.slot=slot;
			name=""+slot;
		}
		
		public synchronized void sendLine(int line) {
			if (first) {
				if(protection.getBoolean(player+"."+name))
					name+=protectId;
				Object[] o = create(prefix, suffix, currentPlayer, name, slott==-1?line:slott);
				BukkitLoader.getPacketHandler().send(p, o[0]);
				BukkitLoader.getPacketHandler().send(p, o[1]);
				first = false;
				old=null;
				changed=false;
				return;
			}
			if(old!=null) {
				Object[] o = remove(old, name);
				BukkitLoader.getPacketHandler().send(p, o[0]);
				BukkitLoader.getPacketHandler().send(p, o[1]);
				old=null;
			}
			if (changed) {
				changed = false;
				Object[] o = modify(prefix, suffix, currentPlayer, name, slott==-1?line:slott);
				BukkitLoader.getPacketHandler().send(p, o[0]);
				BukkitLoader.getPacketHandler().send(p, o[1]);
			}
		}

		public String getValue() {
			return Ref.isOlderThan(13)?prefix+currentPlayer.replaceFirst(format,"")+suffix:prefix+suffix;
		}

		private void setPlayer(String a) {
			a=format+a;
			if (currentPlayer==null||!currentPlayer.equals(a)) {
				old = currentPlayer;
				currentPlayer = a;
			}
		}

		public synchronized void setValue(String a) {
			if(a==null)a="";
			if (Ref.isOlderThan(13)) {
				List<String> d = StringUtils.fixedSplit(a, 16);
				if (a.length() <= 16) {
					setPlayer("");
					if (!prefix.equals(d.get(0)))
						changed = true;
					prefix = d.get(0);
					if (!suffix.equals(""))
						changed = true;
					suffix = "";
					return;
				}
				if (a.length() <= 32) {
					if (!prefix.equals(d.get(0)))
						changed = true;
					prefix = d.get(0);
					setPlayer("");
					if(d.size()>1) {
						if (!suffix.equals(d.get(1)))
							changed = true;
						suffix = d.get(1);
					}else {
						if (!suffix.equals(""))
							changed = true;
						suffix = "";
					}
					return;
				}
				if(Ref.isOlderThan(8)) {
					if (!prefix.equals(d.get(0)))
						changed = true;
					prefix = d.get(0);
					d = StringUtils.fixedSplit(a=a.substring(prefix.length()), 17-format.length());
					setPlayer(d.get(0));
					d = StringUtils.fixedSplit(a.substring(d.get(0).length()), 16);
					if (!suffix.equals(d.get(0)))
						changed = true;
					suffix = d.get(0);
					return;
				}
				if (!prefix.equals(d.get(0)))
					changed = true;
				prefix = d.get(0);
				a=a.substring(d.get(0).length());
				d = StringUtils.fixedSplit(a, 18);
				setPlayer(StringUtils.getLastColors(prefix)+d.get(0));
				a=a.substring(d.get(0).length());
				d = StringUtils.fixedSplit(a, 17);
				if(d.isEmpty()) {
					if (!suffix.equals(""))
						changed = true;
					suffix = "";
				}else {
				if (!suffix.equals(d.get(0)))
					changed = true;
				suffix = d.get(0);
				}
			} else {
				if (!prefix.equals(a))
					changed = true;
				prefix = a;
			}
		}
	}
}