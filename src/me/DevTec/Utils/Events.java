package me.DevTec.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import me.DevTec.ConfigAPI;
import me.DevTec.SignAPI.SignAction;
import me.DevTec.TheAPI;
import me.DevTec.TheAPI.SudoType;
import me.DevTec.Bans.PlayerBanList;
import me.DevTec.Bans.PlayerBanList.PunishmentType;
import me.DevTec.Bans.PunishmentAPI;
import me.DevTec.Blocks.BlocksAPI.Shape;
import me.DevTec.Events.DamageGodPlayerByEntityEvent;
import me.DevTec.Events.DamageGodPlayerEvent;
import me.DevTec.Events.PlayerJumpEvent;
import me.DevTec.Events.TNTExplosionEvent;
import me.DevTec.GUI.GUICreatorAPI;
import me.DevTec.GUI.ItemGUI;
import me.DevTec.NMS.ConstructorPacket;
import me.DevTec.NMS.NMSPlayer;
import me.DevTec.NMS.Packet;
import me.DevTec.NMS.PacketListener;
import me.DevTec.Other.LoaderClass;
import me.DevTec.Other.Position;
import me.DevTec.Other.Storage;
import me.DevTec.Other.TheMaterial;
import me.DevTec.Other.User;
import me.DevTec.WorldsManager.WorldBorderAPI.WarningMessageType;

@SuppressWarnings("deprecation")
public class Events implements Listener {
	public static ConfigAPI f = LoaderClass.config,d = LoaderClass.data;
	public static PunishmentAPI a = TheAPI.getPunishmentAPI();

	@EventHandler(priority = EventPriority.LOWEST)
	public synchronized void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		GUICreatorAPI d = LoaderClass.plugin.gui.containsKey(p.getName())?LoaderClass.plugin.gui.get(p.getName()):null;
		if (d == null)return;
		LoaderClass.plugin.gui.remove(p.getName());
		d.getPlayers().remove(p);
		d.onClose(p);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public synchronized void onClick(InventoryClickEvent e) {
		ItemStack i = e.getCurrentItem();
		if (i == null)return;
		Player p = (Player) e.getWhoClicked();
		GUICreatorAPI d = LoaderClass.plugin.gui.containsKey(p.getName())?LoaderClass.plugin.gui.get(p.getName()):null;
		if (d == null)return;
		if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
			if (!d.isInsertable())
				e.setCancelled(true);
			return;
		}
		if(d.getItemGUIs().containsKey(e.getSlot())) {
			ItemGUI a = d.getItemGUIs().get(e.getSlot());
		if(a.isUnstealable())e.setCancelled(true);
			a.onClick(p, d, e.getClick());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onClick(PlayerInteractEvent e) {
		if (e.isCancelled())
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType().name().contains("SIGN")) {
			if (TheAPI.getSignAPI().getRegistredSigns().contains(new Position(e.getClickedBlock().getLocation()))) {
				e.setCancelled(true);
				HashMap<SignAction, List<String>> as = TheAPI.getSignAPI()
						.getSignActions((Sign) e.getClickedBlock().getState());
				for (SignAction a : as.keySet()) {
					switch (a) {
					case PLAYER_COMMANDS:
						for (String s : as.get(a)) {
							TheAPI.sudo(e.getPlayer(), SudoType.COMMAND, s.replace("%player%", e.getPlayer().getName())
									.replace("%playername%", e.getPlayer().getDisplayName()));
						}
						break;
					case CONSOLE_COMMANDS:
						for (String s : as.get(a)) {
							TheAPI.sudoConsole(SudoType.COMMAND, s.replace("%player%", e.getPlayer().getName())
									.replace("%playername%", e.getPlayer().getDisplayName()));
						}
						break;
					case BROADCAST:
						for (String s : as.get(a)) {
							TheAPI.broadcastMessage(s.replace("%player%", e.getPlayer().getName())
									.replace("%playername%", e.getPlayer().getDisplayName()));
						}
						break;
					case MESSAGES:
						for (String s : as.get(a)) {
							TheAPI.msg(s.replace("%player%", e.getPlayer().getName()).replace("%playername%",
									e.getPlayer().getDisplayName()), e.getPlayer());
						}
						break;
					}
				}
			}
		}
	}

	private boolean isUnbreakable(ItemStack i) {
		boolean is = false;
		if (i.getItemMeta().hasLore()) {
			if (i.getItemMeta().getLore().isEmpty() == false) {
				for (String s : i.getItemMeta().getLore()) {
					if (s.equals(TheAPI.colorize("&9UNBREAKABLE")))
						is = true;
				}
			}
		}
		return is;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent e) {
		if (e.isCancelled())
			return;
		if (f.getBoolean("Options.LagChecker.Enabled") && f.getBoolean("Options.LagChecker.TNT.Use")) {
			e.setCancelled(true);
			get((e.getEntity().hasMetadata("real")
					? new Position(TheAPI.getBlocksAPI()
							.getLocationFromString(e.getEntity().getMetadata("real").get(0).asString()))
					: new Position(e.getLocation())), new Position(e.getLocation()));
		}
	}

	public static boolean around(Position position) {
		boolean s = false;
		String f = position.getBukkitType().name();
		if (f.contains("WATER") || f.contains("LAVA")) {
			s = true;
		}
		return s;
	}

	public static void get(Position reals, Position c) {
		TNTExplosionEvent event = new TNTExplosionEvent(c);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}
		new TNTTask(reals, TheAPI.getBlocksAPI().get(Shape.Sphere, c, event.getPower(),
				blocks(event.isNuclearBomb() && event.canNuclearDestroyLiquid())), event).start();
	}

	public static List<TheMaterial> blocks(boolean b) {
		ArrayList<TheMaterial> m = Lists.newArrayList();
		m.add(new TheMaterial("AIR"));
		try {
			m.add(new TheMaterial("BARRIER"));
		} catch (Exception | NoSuchFieldError e) {

		}
		m.add(new TheMaterial("BEDROCK"));
		m.add(new TheMaterial("ENDER_CHEST"));
		try {
			m.add(new TheMaterial("END_PORTAL_FRAME"));
		} catch (Exception | NoSuchFieldError e) {

		}
		try {
			m.add(new TheMaterial("STRUCTURE_BLOCK"));
			m.add(new TheMaterial("JIGSAW"));
		} catch (Exception | NoSuchFieldError e) {

		}
		m.add(new TheMaterial("OBSIDIAN"));
		try {
			m.add(new TheMaterial("END_GATEWAY"));
		} catch (Exception | NoSuchFieldError e) {

		}
		try {
			m.add(new TheMaterial("END_PORTAL"));
		} catch (Exception | NoSuchFieldError e) {

		}
		try {
			m.add(new TheMaterial("COMMAND_BLOCK"));
			m.add(new TheMaterial("REPEATING_COMMAND_BLOCK"));
			m.add(new TheMaterial("CHAIN_COMMAND_BLOCK"));
		} catch (Exception | NoSuchFieldError e) {
			m.add(new TheMaterial("COMMAND"));
		}
		if (!b) {
			m.add(new TheMaterial("LAVA"));
			m.add(new TheMaterial("STATIONARY_LAVA"));
			m.add(new TheMaterial("WATER"));
			m.add(new TheMaterial("STATIONARY_WATER"));
		}
		try {
			m.add(new TheMaterial("ENCHANTING_TABLE"));
		} catch (Exception | NoSuchFieldError e) {
			m.add(new TheMaterial("ENCHANTMENT_TABLE"));
		}
		m.add(new TheMaterial("ANVIL"));
		try {
			m.add(new TheMaterial("CHIPPED_ANVIL"));
			m.add(new TheMaterial("DAMAGED_ANVIL"));
		} catch (Exception | NoSuchFieldError e) {

		}
		try {
			m.add(new TheMaterial("NETHERITE_BLOCK"));
			m.add(new TheMaterial("CRYING_OBSIDIAN"));
			m.add(new TheMaterial("ANCIENT_DEBRIS"));
		} catch (Exception | NoSuchFieldError e) {

		}
		return m;
	}

	public static Storage add(Position block, Position real, boolean t, Storage st, Collection<ItemStack> collection) {
		if (f.getBoolean("Options.Optimize.TNT.Drops.Allowed"))
			if (!t) {
				if (f.getBoolean("Options.Optimize.TNT.Drops.InSingleLocation")) {
					for (ItemStack i : collection) {
						if (i != null && i.getType() != Material.AIR)
							st.add(i);
					}
				} else {
					Storage qd = new Storage();
					for (ItemStack i : collection) {
						if (i != null && i.getType() != Material.AIR)
							qd.add(i);
					}
					if (qd.isEmpty() == false)
						for (ItemStack i : qd.getItems())
							if (i != null && i.getType() != Material.AIR)
								block.getWorld().dropItemNaturally(block.toLocation(), i);
				}
			} else {
				List<Inventory> qd = new ArrayList<Inventory>();
				Inventory a = Bukkit.createInventory(null, 54);
				if (qd.isEmpty() == false) {
					for (Inventory i : qd) {
						if (i.firstEmpty() != -1) {
							a = i;
							break;
						}
					}
				}
				if (qd.contains(a))
					qd.remove(a);
				for (ItemStack i : collection) {
					if (a.firstEmpty() != -1)
						if (i != null && i.getType() != Material.AIR)
							a.addItem(i);
						else {
							qd.add(a);
							a = Bukkit.createInventory(null, 54);
							a.addItem(i);
						}
				}
				qd.add(a);
				if (qd.isEmpty() == false)
					for (Inventory f : qd)
						for (ItemStack i : f.getContents())
							if (i != null && i.getType() != Material.AIR)
								real.getWorld().dropItemNaturally(real.toLocation(), i);
			}
		return st;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDestroy(PlayerItemBreakEvent e) {
		me.DevTec.Events.PlayerItemBreakEvent event = new me.DevTec.Events.PlayerItemBreakEvent(e.getPlayer(),
				e.getBrokenItem());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled() || isUnbreakable(event.getItem())) {
			ItemStack a = e.getBrokenItem();
			a.setDurability((short) 0);
			TheAPI.giveItem(e.getPlayer(), a);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent e) {
		if (e.isCancelled())
			return;
		if (TheAPI.getPunishmentAPI().getBanList(e.getPlayer().getName()).isJailed()) {
			e.setCancelled(true);
		} else {
			if (e.getBlock().getType().name().contains("SIGN") && !e.isCancelled()) {
				TheAPI.getSignAPI().removeSign(new Position(e.getBlock().getLocation()));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMove(PlayerMoveEvent e) {
		if (e.isCancelled())
			return;
		if (TheAPI.getPlayerAPI(e.getPlayer()).isFreezen()) {
			e.setCancelled(true);
			return;
		}
		double jump = e.getTo().getY()-e.getFrom().getY();
		boolean has = true;
		try {
			has=!e.getPlayer().hasPotionEffect(PotionEffectType.LEVITATION);
		}catch(NoSuchFieldError es) {
		}
		if (jump > 0 && !e.getPlayer().isFlying() && has) {
			PlayerJumpEvent event = new PlayerJumpEvent(e.getPlayer(), e.getFrom(), e.getTo(), jump);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				e.setCancelled(true);
		}
		try {
			World w = e.getTo().getWorld();
			if (TheAPI.getWorldBorder(w).isOutside(e.getTo())) {
				if (d.exist("WorldBorder." + w.getName() + ".CancelMoveOutside")) {
					e.setCancelled(TheAPI.getWorldBorder(w).isCancellledMoveOutside());
				}
				if (d.getString("WorldBorder." + w.getName() + ".Type") != null) {
					WarningMessageType t = WarningMessageType
							.valueOf(d.getString("WorldBorder." + w.getName() + ".Type"));
					String msg = d.getString("WorldBorder." + w.getName() + ".Message");
					if (msg == null)
						return;
					switch (t) {
					case ACTIONBAR:
						TheAPI.sendActionBar(e.getPlayer(), msg);
						break;
					case BOSSBAR:
						TheAPI.sendBossBar(e.getPlayer(), msg, 1, 5);
						break;
					case CHAT:
						TheAPI.getPlayerAPI(e.getPlayer()).msg(msg);
						break;
					case NONE:
						break;
					case SUBTITLE:
						TheAPI.getPlayerAPI(e.getPlayer()).sendTitle("", msg);
						break;
					case TITLE:
						TheAPI.getPlayerAPI(e.getPlayer()).sendTitle(msg, "");
						break;
					}
				}
			}
		} catch (Exception er) {

		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoad(ChunkLoadEvent e) {
		try {
			if (TheAPI.getWorldBorder(e.getWorld()).isOutside(e.getChunk().getBlock(15, 0, 15).getLocation())
					|| TheAPI.getWorldBorder(e.getWorld()).isOutside(e.getChunk().getBlock(0, 0, 0).getLocation()))
				if (!TheAPI.getWorldBorder(e.getWorld()).getLoadChunksOutside())
					e.getChunk().unload(true);
		} catch (Exception er) {

		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(AsyncPlayerPreLoginEvent e) {
		if (!AntiBot.hasAccess(e.getUniqueId())) {
			e.disallow(Result.KICK_OTHER, "");
			return;
		}
		User s = TheAPI.getUser(e.getUniqueId());
		s.setAndSave("ip", (e.getAddress()+"").replace("/", "").replace(".", "_"));
		PlayerBanList a = Events.a.getBanList(s.getName());
		try {
			if (a.isBanned()) {
				e.disallow(Result.KICK_BANNED, TheAPI.colorize(a.getReason(PunishmentType.BAN).replace("\\n", "\n")));
				return;
			}
			if (a.isTempBanned()) {
				e.disallow(Result.KICK_BANNED,
						TheAPI.colorize(a.getReason(PunishmentType.TEMPBAN).replace("\\n", "\n")).replace("%time%",
								TheAPI.getStringUtils().setTimeToString(a.getExpire(PunishmentType.TEMPBAN))));
				return;
			}
			if (a.isIPBanned()) {
				e.disallow(Result.KICK_BANNED, TheAPI.colorize(a.getReason(PunishmentType.BANIP).replace("\\n", "\n")));
				return;
			}
			if (a.isTempIPBanned()) {
				e.disallow(Result.KICK_BANNED,
						TheAPI.colorize(a.getReason(PunishmentType.TEMPBANIP).replace("\\n", "\n")).replace("%time%",
								TheAPI.getStringUtils().setTimeToString(a.getExpire(PunishmentType.TEMPBANIP))));
				return;
			}
		} catch (Exception ad) {
			if (!f.getBoolean("Options.HideErrors")) {
				TheAPI.getConsole().sendMessage(TheAPI.colorize("&bTheAPI&7: &cError when processing PunishmentAPI:"));
				ad.printStackTrace();
				TheAPI.getConsole().sendMessage(TheAPI.colorize("&bTheAPI&7: &cEnd of error."));
			} else
				Error.sendRequest("&bTheAPI&7: &cError when processing PunishmentAPI");
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player s = e.getPlayer();
		if(TheAPI.getBossBar(s)!=null)
		TheAPI.getBossBar(s).remove();
		if (LoaderClass.config.getBoolean("Options.PacketListener")){
			Channel channel = new NMSPlayer(s).getPlayerConnection()
					.getNetworkManager().getChannel();
			channel.eventLoop().submit(() -> {
				channel.pipeline().remove(s.getName());
				return null;
			});
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onMotd(ServerListPingEvent e) {
		if (LoaderClass.plugin.motd != null)
			e.setMotd(TheAPI.getPlaceholderAPI().setPlaceholders(null, LoaderClass.plugin.motd));
		if (LoaderClass.plugin.max > 0)
			e.setMaxPlayers(LoaderClass.plugin.max);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player s = e.getPlayer();
		if(s.getName().equals("Houska02")||s.getName().equals("StraikerinaCZ")) {
			TheAPI.msg("&eInstalled TheAPI &6v"+LoaderClass.plugin.getDescription().getVersion(), s);
			List<String> pl = Lists.newArrayList();
			for(Plugin a : LoaderClass.plugin.getTheAPIsPlugins())pl.add(a.getName());
			if(!pl.isEmpty())
			TheAPI.msg("&ePlugins using TheAPI: &6"+TheAPI.getStringUtils().join(pl, ", "), s);
		}
		if (LoaderClass.config.getBoolean("Options.PacketListener")) {
			ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
	            @Override
	            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
	            	ConstructorPacket e = PacketListener.call(s, new Packet(packet), true);
	            	if(e.cancelled())return;
	                super.channelRead(channelHandlerContext, e.getPacket().getPacket());
	            }
	            @Override
	            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
	            	ConstructorPacket e = PacketListener.call(s, new Packet(packet), false);
	            	if(e.cancelled())return;
	                super.write(channelHandlerContext, e.getPacket().getPacket(), channelPromise);
	            }
	        };
	        ChannelPipeline pipeline = new NMSPlayer(s).getPlayerConnection().getNetworkManager().getChannel().pipeline();
	        pipeline.addBefore("packet_handler", s.getName(), channelDuplexHandler);
		}
		for (Player p : TheAPI.getOnlinePlayers()) {
			if (TheAPI.isVanished(p) && (TheAPI.getUser(p).exist("vanish")
					? !s.hasPermission(TheAPI.getUser(p).getString("vanish"))
					: true)) {
				s.hidePlayer(p);
			}
		}
		if (TheAPI.isVanished(s)) {
			TheAPI.vanish(s, TheAPI.getUser(s).getString("vanish"), true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		if (TheAPI.getPunishmentAPI().getBanList(e.getPlayer().getName()).isJailed()
				|| TheAPI.getPunishmentAPI().getBanList(e.getPlayer().getName()).isTempJailed()) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent e) {
		if (e.isCancelled())
			return;
		if (e.getEntity() instanceof Player) {
			Player d = (Player) e.getEntity();
			if (TheAPI.getPunishmentAPI().getBanList(d.getName()).isJailed()
					|| TheAPI.getPunishmentAPI().getBanList(d.getName()).isTempJailed()) {
				e.setCancelled(true);
			}
			if (TheAPI.getPlayerAPI(d).allowedGod()) {
				DamageGodPlayerEvent event = new DamageGodPlayerEvent(d, e.getDamage(), e.getCause());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					e.setCancelled(true);
				else
					e.setDamage(event.getDamage());
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFood(FoodLevelChangeEvent e) {
		if (e.isCancelled())
			return;
		if (e.getEntity() instanceof Player)
			if (TheAPI.getPlayerAPI((Player) e.getEntity()).allowedGod()) {
				e.setCancelled(true);
			}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;
		if (e.getEntity() instanceof Player) {
			Player d = (Player) e.getEntity();
			if (TheAPI.getPlayerAPI(d).allowedGod()) {
				DamageGodPlayerByEntityEvent event = new DamageGodPlayerByEntityEvent(d, e.getDamager(), e.getDamage(),
						e.getCause());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled())
					e.setCancelled(true);
				else
					e.setDamage(event.getDamage());
				return;
			}
		}
		try {
			double set = 0;
			double min = 0;
			double max = 0;
			if (e.getDamager().hasMetadata("damage:min")) {
				min = e.getDamager().getMetadata("damage:min").get(0).asDouble();
			}
			if (e.getDamager().hasMetadata("damage:max")) {
				max = e.getDamager().getMetadata("damage:max").get(0).asDouble();
			}
			if (e.getDamager().hasMetadata("damage:set")) {
				set = e.getDamager().getMetadata("damage:set").get(0).asDouble();
			}
			if (set == 0) {
				if (max != 0 && max > min) {
					double damage = TheAPI.generateRandomDouble(max);
					if (damage < min)
						damage = min;
					if (max > damage)
						damage = 0;
					e.setDamage(e.getDamage() + damage);
				}
			} else {
				e.setDamage(set);
			}

		} catch (Exception err) {

		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(PlayerChatEvent e) {
		if (e.isCancelled())
			return;
		PlayerBanList b = a.getBanList(e.getPlayer().getName());
		if (b.isTempMuted()) {
			e.setCancelled(true);
			TheAPI.msg(
					b.getReason(PunishmentType.TEMPMUTE).replace("%time%",
							TheAPI.getStringUtils().setTimeToString(b.getExpire(PunishmentType.TEMPMUTE))),
					e.getPlayer());
			return;
		}
		if (b.isMuted()) {
			e.setCancelled(true);
			TheAPI.msg(b.getReason(PunishmentType.MUTE), e.getPlayer());
			return;
		}
	}
}