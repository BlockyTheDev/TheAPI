package me.DevTec.Utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.DevTec.ItemCreatorAPI;
import me.DevTec.PlayerAPI.InvseeType;
import me.DevTec.RankingAPI;
import me.DevTec.ScoreboardAPI;
import me.DevTec.TheAPI;
import me.DevTec.TheAPI.TPSType;
import me.DevTec.Blocks.BlockSave;
import me.DevTec.Blocks.BlocksAPI.Shape;
import me.DevTec.GUI.GUICreatorAPI;
import me.DevTec.GUI.GUICreatorAPI.Options;
import me.DevTec.Other.LoaderClass;
import me.DevTec.Other.Position;
import me.DevTec.Other.TheMaterial;
import me.DevTec.Scheduler.Tasker;

public class TheAPICommand implements CommandExecutor, TabCompleter {
	private static boolean r;

	private String getPlugin(Plugin a) {
		if (a.isEnabled())
			return "&a" + a.getName();
		return "&c" + a.getName();
	}

	private boolean perm(CommandSender s, String p) {
		if (s.hasPermission("TheAPI.Command." + p))
			return true;
		TheAPI.msg("&6You do not have permission '&eTheAPI.Command." + p + "&6' to do that!", s);
		return false;
	}
	
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onCommand(CommandSender s, Command arg1, String arg2, String[] args) {
		if (args.length == 0) {
			TheAPI.msg("&7-----------------", s);
			if (s.hasPermission("TheAPI.Command.Info"))
				TheAPI.msg("&e/TheAPI Info", s);
			if (s.hasPermission("TheAPI.Command.Invsee"))
				TheAPI.msg("&e/TheAPI Invsee", s);
			if (s.hasPermission("TheAPI.Command.Reload"))
				TheAPI.msg("&e/TheAPI Reload", s);
			if (s.hasPermission("TheAPI.Command.WorldsManager"))
				TheAPI.msg("&e/TheAPI WorldsManager", s);
			if (s.hasPermission("TheAPI.Command.ClearCache"))
				TheAPI.msg("&e/TheAPI ClearCache", s);
			if (s.hasPermission("TheAPI.Command.PluginManager"))
				TheAPI.msg("&e/TheAPI PluginManager", s);
			if (s.isOp())
				TheAPI.msg("&e/TheAPI Test", s);
			TheAPI.msg("&7*Credits; &eCreated by DevTec*", s);
			TheAPI.msg("&7-----------------", s);
			return true;
		}
		if (args[0].equalsIgnoreCase("test")) {
			if (!s.isOp())
				return true; // sender must be player & has op
			if (args.length == 1) {
				TheAPI.msg("&7-----------------", s);
				TheAPI.msg("&e/TheAPI Test ActionBar", s);
				TheAPI.msg("&e/TheAPI Test BlocksAPI", s);
				TheAPI.msg("&e/TheAPI Test BossBar", s);
				TheAPI.msg("&e/TheAPI Test PlayerName", s);
				TheAPI.msg("&e/TheAPI Test RankingAPI", s);
				TheAPI.msg("&e/TheAPI Test Scoreboard", s);
				TheAPI.msg("&e/TheAPI Test TabList", s);
				TheAPI.msg("&e/TheAPI Test Title", s);
				TheAPI.msg("&e/TheAPI Test GUICreatorAPI", s);
				TheAPI.msg("&e/TheAPI Test hideShowEntity", s);
				TheAPI.msg("&e/TheAPI Test Other - DevTec currently testing", s);
				TheAPI.msg("&7-----------------", s);
				return true;
			}
			if (args[1].equalsIgnoreCase("Other")) {
					return true;
			}
			if(s instanceof Player) {
			Player p = (Player) s;
			if (args[1].equalsIgnoreCase("hideShowEntity")) {
				Pig pig = (Pig) p.getWorld().spawnEntity(p.getLocation(), EntityType.PIG);
				pig.setCollidable(false);
				pig.setBaby();
				try {
					pig.setAI(false);
				} catch (Exception | NoSuchMethodError e) {
				}
				pig.setSilent(true);
				new Tasker() {
					@Override
					public void run() {
						pig.setCustomName(TheAPI.colorize("&4Become invisible in " + (5 - runTimes())));
						pig.setCustomNameVisible(true);
						if (runTimes() == 5) {
							for (Player all : TheAPI.getOnlinePlayers())
								TheAPI.hideEntity(all, pig);
							pig.setCustomName(TheAPI.colorize("&4Repeat visible!"));
							new Tasker() {
								@Override
								public void run() {
									for (Player all : TheAPI.getOnlinePlayers())
										TheAPI.showEntity(all, pig);
									new Tasker() {
										@Override
										public void run() {
											pig.remove();
										}
									}.later(100);
								}
							}.later(100);
						}
					}
				}.repeatingTimes(0, 20, 5);
				return true;
			}
			if (args[1].equalsIgnoreCase("GUICreatorAPI")) {
				TheAPI.msg("&eThis maybe help you with creating gui: https://i.imgur.com/f43qxux.png", p);
				GUICreatorAPI a = TheAPI.getGUICreatorAPI(p);
				// REQUIRED
				a.setSize(54);
				a.setTitle("&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"));
				a.open();
				// Frame
				ItemStack m = new ItemStack(Material.GLASS);
				if(Material.matchMaterial("BLACK_STAINED_GLASS_PANE")!=null)m=new ItemStack(Material.matchMaterial("BLACK_STAINED_GLASS_PANE"));
				else if(Material.matchMaterial("STAINED_GLASS_PANE")!=null)m = new ItemStack(Material.matchMaterial("STAINED_GLASS_PANE"), 1, (byte)15);
				ItemCreatorAPI iCreator = TheAPI.getItemCreatorAPI(m);
				iCreator.setDisplayName(" ");
				ItemStack item = iCreator.create();
				HashMap<Options, Object> setting = new HashMap<Options, Object>();
				setting.put(Options.CANT_BE_TAKEN, true);
				setting.put(Options.CANT_PUT_ITEM, true);
				for (int i = 0; i < 10; ++i)
					a.setItem(i, item, setting);
				a.setItem(17, item, setting);
				a.setItem(18, item, setting);
				a.setItem(26, item, setting);
				a.setItem(27, item, setting);
				a.setItem(35, item, setting);
				a.setItem(36, item, setting);
				for (int i = 44; i < 54; ++i)
					a.setItem(i, item, setting);

				// Items
				iCreator = TheAPI.getItemCreatorAPI(Material.DIAMOND);
				iCreator.setDisplayName("&eCreator of plugin");
				iCreator.addLore("");
				iCreator.addLore("&cPlugin is created by Straiker123");
				item = iCreator.create();
				a.setItem(20, item, setting);

				iCreator = TheAPI.getItemCreatorAPI(Material.EMERALD);
				iCreator.setDisplayName("&eVersion of plugin");
				iCreator.addLore("");
				iCreator.addLore("&cTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"));
				item = iCreator.create();
				a.setItem(22, item, setting);

				iCreator = TheAPI.getItemCreatorAPI(Material.GOLD_INGOT);
				iCreator.setDisplayName("&ePlugins using TheAPI");
				iCreator.addLore("");
				iCreator.addLore("&7---------");
				for (Plugin d : LoaderClass.plugin.getTheAPIsPlugins()) // add plugins to lore
					iCreator.addLore("&c" + d.getName() + " v" + TheAPI.getPluginsManagerAPI().getVersion(d.getName())); // get
																															// version
																															// of
																															// plugin
				iCreator.addLore("&7---------");
				item = iCreator.create();
				a.setItem(24, item, setting);

				iCreator = TheAPI.getItemCreatorAPI(Material.BARRIER);
				iCreator.setDisplayName("&cClose");
				item = iCreator.create();
				setting.put(Options.RUNNABLE, new Runnable() { // Apply this runnable on item
					@Override
					public void run() {
						a.close();
					}
				});
				a.setItem(49, item, setting);
				return true;
			}
			if (args[1].equalsIgnoreCase("RankingAPI")) {
				HashMap<String, BigDecimal> tops = new HashMap<String, BigDecimal>();
				TheAPI.msg("&eInput:", s);
				TheAPI.msg("&6- Straiker123, 50.0", s);
				TheAPI.msg("&6- TheAPI, 5431.6", s);
				TheAPI.msg("&6- SCR, 886.5", s);
				TheAPI.msg("&6- Houska02, 53.11", s);
				tops.put("Straiker123", new BigDecimal(50.0));
				tops.put("TheAPI", new BigDecimal(5431.6));
				tops.put("SCR", new BigDecimal(886.5));
				tops.put("Houska02", new BigDecimal(53.11));
				RankingAPI<String> map = new RankingAPI<String>(tops);
				TheAPI.msg("&eResult:", s);
				for (int i = 1; i < map.size(); ++i) { // 1 2 3 4
					TheAPI.msg("&6" + map.getPosition(map.getObject(i)) + ". " + map.getObject(i) + " with "
							+ map.getValue(map.getObject(i)) + " points", s);
				}
				return true;
			}

			if (args[1].equalsIgnoreCase("bossbar")) {
				TheAPI.sendBossBar(p, "&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"), 0.5, 40);
				return true;
			}
			if (args[1].equalsIgnoreCase("PlayerName")) {
				String old = p.getName();
				TheAPI.msg("&eYour nickname changed to &nTheAPI", s);
				TheAPI.getNameTagAPI(p, "", "").setPlayerName("TheAPI");
				new Tasker() {
					@Override
					public void run() {
						TheAPI.getNameTagAPI(p, "", "").setPlayerName(old);
					}
				}.later(40);
				return true;
			}
			if (args[1].equalsIgnoreCase("ActionBar")) {
				TheAPI.sendActionBar(p, "&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"));
				return true;
			}
			if (args[1].equalsIgnoreCase("Title")) {
				TheAPI.sendTitle(p, "&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"), "");
				return true;
			}
			if (args[1].equalsIgnoreCase("TabList")) {
				TheAPI.getTabListAPI().setHeaderFooter(p,
						"&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"),
						"&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"));
				return true;
			}
			if (args[1].equalsIgnoreCase("Scoreboard")) {
				ScoreboardAPI a = TheAPI.getScoreboardAPI(p, false, true);
				a.setDisplayName("&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI"));
				a.setLine(0, "&aBy DevTec");
				new Tasker() {
					@Override
					public void run() {
							if(runTimes()==50)a.destroy();
						else {
							a.setDisplayName("&eTheAPI v" + TheAPI.getPluginsManagerAPI().getVersion("TheAPI")+" &7: "+TheAPI.generateRandomInt(10));
							a.setLine(1, "&7Random: &c"+TheAPI.generateRandomInt(10));
						}
					}}.repeatingTimes(0, 2, 50);
				return true;
			}
			if (args[1].equalsIgnoreCase("BlocksAPI")) {
				if (!r) {
					r = true;
					HashMap<Position,BlockSave> save = Maps.newHashMap();
					for(Position pos : TheAPI.getBlocksAPI().get(Shape.Sphere, new Position(p.getLocation()), 5,new TheMaterial("AIR"))) {
						save.put(pos, new BlockSave(pos));
					}
					TheAPI.getBlocksAPI().set(Shape.Sphere, new Position(p.getLocation()), 5, new TheMaterial("DIAMOND_BLOCK"),new TheMaterial("AIR"));
					new Tasker() {
						@Override
						public void run() {
							for(Position s : save.keySet())save.get(s).load(s);
							save.clear();
							r = false;
						}
					}.later(40);
					return true;
				}
				return true;
			}
		}}
		if (args[0].equalsIgnoreCase("PluginManager") || args[0].equalsIgnoreCase("pm") 
				|| args[0].equalsIgnoreCase("plugin") ||args[0].equalsIgnoreCase("pluginm")||args[0].equalsIgnoreCase("pmanager")) {
			if (perm(s,"PluginManager")) {
				if (args.length == 1) {
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&e/TheAPI PluginManager Enable <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Disable <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Load <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Unload <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Reload <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Info <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager EnableAll", s);
					TheAPI.msg("&e/TheAPI PluginManager DisableAll", s);
					TheAPI.msg("&e/TheAPI PluginManager LoadAll", s);
					TheAPI.msg("&e/TheAPI PluginManager UnloadAll", s);
					TheAPI.msg("&e/TheAPI PluginManager ReloadAll", s);
					TheAPI.msg("&e/TheAPI PluginManager Info <plugin>", s);
					TheAPI.msg("&e/TheAPI PluginManager Files", s);
					List<Plugin> pl =  TheAPI.getPluginsManagerAPI().getPlugins();
					if(!pl.isEmpty()) {
					TheAPI.msg("&7Plugins:", s);
					for (Plugin w :pl)
						TheAPI.msg("&7 - &e" + getPlugin(w), s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Files")||args[1].equalsIgnoreCase("notloaded")||args[1].equalsIgnoreCase( "toload")||args[1].equalsIgnoreCase( "unloaded")) {
					HashMap<String, String> d = TheAPI.getPluginsManagerAPI().getPluginsToLoadWithNames();
					if(d.isEmpty()) {
						TheAPI.msg("&eNo plugin to load.", s);
						return true;
					}
					TheAPI.msg("&ePlugins to load:", s);
					for(String a : d.keySet()) {
						String text = " &7- &e"+d.get(a)+" &7(&e"+a+"&7)";
					TheAPI.msg(text, s);
					}
					return true;
				}
				if (args[1].equalsIgnoreCase("EnableAll")) {
					List<Plugin> ad = TheAPI.getPluginsManagerAPI().getPlugins();
					ad.remove(TheAPI.getPluginsManagerAPI().getPlugin(LoaderClass.plugin.getName()));
					if(ad.isEmpty()) {
						TheAPI.msg("&eNo plugin to enable.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					for(Plugin a : ad) {
						TheAPI.msg("&eEnabling plugin "+a.getName()+"..", s);
						TheAPI.getPluginsManagerAPI().enablePlugin(a);
						TheAPI.msg("&ePlugin "+a.getName()+" enabled.", s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("DisableAll")) {
					List<Plugin> ad = TheAPI.getPluginsManagerAPI().getPlugins();
					ad.remove(TheAPI.getPluginsManagerAPI().getPlugin(LoaderClass.plugin.getName()));
					if(ad.isEmpty()) {
						TheAPI.msg("&eNo plugin to disable.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					for(Plugin a : ad) {
						TheAPI.msg("&eDisabling plugin "+a.getName()+"..", s);
						TheAPI.getPluginsManagerAPI().disablePlugin(a);
						TheAPI.msg("&ePlugin "+a.getName()+" disabled.", s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("ReloadAll")) {
					if(TheAPI.getPluginsManagerAPI().getPlugins().isEmpty()) {
						TheAPI.msg("&eNo plugin to reload.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins()) {
						TheAPI.msg("&eReloading plugin "+a.getName()+"..", s);
						TheAPI.getPluginsManagerAPI().reloadPlugin(a);
						TheAPI.msg("&ePlugin "+a.getName()+" reloaded.", s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("LoadAll")) {
					if(TheAPI.getPluginsManagerAPI().getPluginsToLoad().isEmpty()) {
						TheAPI.msg("&eNo plugin to load.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					for(String a : TheAPI.getPluginsManagerAPI().getPluginsToLoad()) {
						if(a.equals("TheAPI"))continue;
						TheAPI.msg("&eLoading plugin "+a+"..", s);
						TheAPI.getPluginsManagerAPI().loadPlugin(a);
						TheAPI.msg("&ePlugin "+a+" loaded.", s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("UnloadAll")) {
					List<Plugin> ad = TheAPI.getPluginsManagerAPI().getPlugins();
					ad.remove(TheAPI.getPluginsManagerAPI().getPlugin(LoaderClass.plugin.getName()));
					if(ad.isEmpty()) {
						TheAPI.msg("&eNo plugin to unload.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					for(Plugin a : ad) {
						TheAPI.msg("&eUnloading plugin "+a.getName()+"..", s);
						TheAPI.getPluginsManagerAPI().unloadPlugin(a);
						TheAPI.msg("&ePlugin "+a.getName()+" unloaded.", s);
					}
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Enable")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Enable <plugin>", s);
						return true;
					}
					if(args[2].equals("TheAPI")) {
						TheAPI.msg("&eYou can't enable TheAPI.", s);
						return true;
					}
					int f=0;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins())
						if(a.getName().equals(args[2])) {
							if(a.isEnabled())f=1;
							else f = 2;
							break;
						}
					if(f==1) {
						TheAPI.msg("&7Plugin "+args[2]+" is already enabled.", s);
						return true;
					}
					if(f==0) {
						TheAPI.msg("&7Plugin "+args[2]+" isn't loaded.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eEnabling plugin "+args[2]+"..", s);
					TheAPI.getPluginsManagerAPI().enablePlugin(args[2]);
					TheAPI.msg("&ePlugin "+args[2]+" enabled.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Disable")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Disable <plugin>", s);
						return true;
					}
					if(args[2].equals("TheAPI")) {
						TheAPI.msg("&eYou can't disable TheAPI.", s);
						return true;
					}
					int f=0;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins())
						if(a.getName().equals(args[2])) {
							if(a.isEnabled())f=1;
							else f = 2;
							break;
						}
					if(f==2) {
						TheAPI.msg("&7Plugin "+args[2]+" is already disabled.", s);
						return true;
					}
					if(f==0) {
						TheAPI.msg("&7Plugin "+args[2]+" isn't loaded.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eDisabling plugin "+args[2]+"..", s);
					TheAPI.getPluginsManagerAPI().disablePlugin(args[2]);
					TheAPI.msg("&ePlugin "+args[2]+" disabled.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Load")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Load <plugin>", s);
						return true;
					}
					String pluginName = args[2];
					if(TheAPI.getPluginsManagerAPI().getRawPluginsToLoad().contains(pluginName)||TheAPI.getPluginsManagerAPI().getRawPluginsToLoad().contains(pluginName+".jar")
							||TheAPI.getPluginsManagerAPI().getPluginsToLoad().contains(pluginName)||TheAPI.getPluginsManagerAPI().getPluginsToLoad().contains(pluginName+".jar")) {
					if(pluginName.equals("TheAPI")) {
						TheAPI.msg("&eYou can't load TheAPI.", s);
						return true;
					}
					int f=0;
					String real = null;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins())
						if(a.getName().equals(pluginName)||TheAPI.getPluginsManagerAPI().getFileOfPlugin(a).getName().equals(pluginName)
								||TheAPI.getPluginsManagerAPI().getFileOfPlugin(a).getName().equals(pluginName+".jar")) {
							if(a.isEnabled())f=1;
							else f = 2;
							real=a.getName();
							break;
						}
					if(f==2) {
						TheAPI.msg("&7Plugin "+real+" is already loaded, but disabled.", s);
						return true;
					}
					if(f==1) {
						TheAPI.msg("&7Plugin "+real+" is already loaded.", s);
						return true;
					}
					real = TheAPI.getPluginsManagerAPI().getPluginNameByFile(pluginName);
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eLoading plugin "+real+" ("+pluginName+")..", s);
					TheAPI.getPluginsManagerAPI().loadPlugin(pluginName);
					TheAPI.msg("&ePlugin "+real+" ("+pluginName+") loaded & enabled.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
					}
					if (pluginName.endsWith(".jar"))
						pluginName = pluginName.substring(0, pluginName.length() - 4);
					TheAPI.msg("&7Plugin "+pluginName+" not found.", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Unload")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Unload <plugin>", s);
						return true;
					}
					if(args[2].equals("TheAPI")) {
						TheAPI.msg("&eYou can't unload TheAPI.", s);
						return true;
					}
					int f=0;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins()) {
						if(a.getName().equals(args[2])) {
							if(a.isEnabled())f=1;
							else f = 2;
							break;
						}}
					if(f==0) {
						TheAPI.msg("&7Plugin "+args[2]+" isn't loaded.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eUnloading plugin "+args[2]+"..", s);
					TheAPI.getPluginsManagerAPI().unloadPlugin(args[2]);
					TheAPI.msg("&ePlugin "+args[2]+" unloaded.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Reload")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Reload <plugin>", s);
						return true;
					}
					int i = 0;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins())
						if(a.getName().equals(args[2])) {
							if(a.isEnabled())i=1;
							else i = 2;
							break;
						}
					if(i==0) {
						TheAPI.msg("&7Plugin "+args[2]+" isn't loaded.", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eReloading plugin "+args[2]+"..", s);
					TheAPI.getPluginsManagerAPI().reloadPlugin(args[2]);
					TheAPI.msg("&ePlugin "+args[2]+" reloaded.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Info")||args[1].equalsIgnoreCase("information")||args[1].equalsIgnoreCase("informations")) {
					if (args.length == 2) {
						TheAPI.msg("&e/TheAPI PluginManager Info <plugin>", s);
						return true;
					}
					int i = 0;
					for(Plugin a : TheAPI.getPluginsManagerAPI().getPlugins())
						if(a.getName().equals(args[2])) {
							if(a.isEnabled())i=1;
							else i = 2;
							break;
						}
					if(i==0) {
						TheAPI.msg("&7Plugin "+args[2]+" isn't loaded.", s);
						return true;
					}
					TheAPI.msg("&7╔═════════════════════════════", s);
					TheAPI.msg("&7║ Name: &e"+args[2], s);
					TheAPI.msg("&7║ State: &e"+(i==1?"Enabled":"Disabled"), s);
					if (TheAPI.getPluginsManagerAPI().getCommands(args[2]).size() != 0) {
						TheAPI.msg("&7║ Commands:", s);
						for (String a : TheAPI.getPluginsManagerAPI().getCommands(args[2]))
							TheAPI.msg("&7║  - &e" + a, s);
					}
					if (TheAPI.getPluginsManagerAPI().getPermissions(args[2]).size() != 0) {
						TheAPI.msg("&7║ Permissions:", s);
						for (Permission a : TheAPI.getPluginsManagerAPI().getPermissions(args[2])) {
							TheAPI.msg("&7║  » &e" + a.getName()+"&7:", s);
							Map<String, Boolean> c = a.getChildren();
							if(c.isEmpty()==false)
								for(String d : c.keySet())
						TheAPI.msg("&7║    - "+(c.get(d) ? "&a" : "&c") + d, s);
						}
					}
					if (TheAPI.getPluginsManagerAPI().getVersion(args[2]) != null)
						TheAPI.msg("&7║ Version: &e"+TheAPI.getPluginsManagerAPI().getVersion(args[2]), s);
					if (TheAPI.getPluginsManagerAPI().getWebsite(args[2]) != null)
						TheAPI.msg("&7║ Website: &e"+TheAPI.getPluginsManagerAPI().getWebsite(args[2]), s);
					if (TheAPI.getPluginsManagerAPI().getMainClass(args[2]) != null)
						TheAPI.msg("&7║ MainClass: &e"+TheAPI.getPluginsManagerAPI().getMainClass(args[2]), s);
					if (!TheAPI.getPluginsManagerAPI().getAuthor(args[2]).isEmpty())
						TheAPI.msg("&7║ Author(s): &e"+TheAPI.getStringUtils().join(TheAPI.getPluginsManagerAPI().getAuthor(args[2]),", "), s);
					if (!TheAPI.getPluginsManagerAPI().getSoftDepend(args[2]).isEmpty())
						TheAPI.msg("&7║ SoftDepend(s): &e"+TheAPI.getStringUtils().join(TheAPI.getPluginsManagerAPI().getSoftDepend(args[2]),", "), s);
					if (!TheAPI.getPluginsManagerAPI().getDepend(args[2]).isEmpty())
						TheAPI.msg("&7║ Depend(s): &e"+TheAPI.getStringUtils().join(TheAPI.getPluginsManagerAPI().getDepend(args[2]),", "), s);
					TheAPI.msg("&7╚═════════════════════════════", s);
					return true;
				}
				TheAPI.msg("&7-----------------", s);
				TheAPI.msg("&e/TheAPI PluginManager Enable <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Disable <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Load <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Unload <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Reload <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Info <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager EnableAll", s);
				TheAPI.msg("&e/TheAPI PluginManager DisableAll", s);
				TheAPI.msg("&e/TheAPI PluginManager LoadAll", s);
				TheAPI.msg("&e/TheAPI PluginManager UnloadAll", s);
				TheAPI.msg("&e/TheAPI PluginManager ReloadAll", s);
				TheAPI.msg("&e/TheAPI PluginManager Info <plugin>", s);
				TheAPI.msg("&e/TheAPI PluginManager Files", s);
				List<Plugin> pl =  TheAPI.getPluginsManagerAPI().getPlugins();
				if(!pl.isEmpty()) {
				TheAPI.msg("&7Plugins:", s);
				for (Plugin w :pl)
					TheAPI.msg("&7 - &e" + getPlugin(w), s);
				}
				TheAPI.msg("&7-----------------", s);
				return true;
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("cc") || args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("clearcache")) {
			if (perm(s,"ClearCache")) {
				TheAPI.msg("&7-----------------", s);
				TheAPI.msg("&eClearing cache..", s);
				for (Player id : LoaderClass.plugin.gui.keySet())
					LoaderClass.plugin.gui.get(id).close();
				LoaderClass.plugin.gui.clear();
				TheAPI.clearCache();
				for(World w : Bukkit.getWorlds())
					for(Chunk c: w.getLoadedChunks())c.unload(true);
				TheAPI.msg("&eCache cleared.", s);
				TheAPI.msg("&7-----------------", s);
				return true;
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("invsee")) {
			if (!s.hasPermission("TheAPI.Command.Invsee"))return true;
			if(args.length==1) {
				TheAPI.msg("&e/TheAPI Invsee <player>", s);
				return true;
			}
			Player p = TheAPI.getPlayer(args[1]);
			if(p!=null) {
			TheAPI.msg("&7Opening inventory of player "+p.getName()+"..", s);
			TheAPI.getPlayerAPI((Player)s).invsee(p, InvseeType.INVENTORY);
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
			if (perm(s,"Reload")) {
				TheAPI.msg("&7-----------------", s);
				TheAPI.msg("&eReloading configs..", s);
				for (Player p : TheAPI.getOnlinePlayers())
					TheAPI.getUser(p).config().reload();
				LoaderClass.data.reload();
				LoaderClass.config.reload();
				LoaderClass.gameapi.reload();
				LoaderClass.unused.reload();
				Tasks.unload();
				Tasks.load();
				TheAPI.msg("&eConfigs reloaded.", s);
				TheAPI.msg("&7-----------------", s);
				return true;
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("informations") || args[0].equalsIgnoreCase("info")) {
			if (perm(s,"Info")) {
				new Tasker() {
					public void run() {
						TheAPI.msg("&7╔═════════════════════════════", s);
						TheAPI.msg("&7║ Memory:", s);
						TheAPI.msg("&7║  Max: &e"+TheAPI.getMemoryAPI().getMaxMemory(), s);
						TheAPI.msg("&7║  Used: &e"+TheAPI.getMemoryAPI().getUsedMemory(false)+" &7(&e"+TheAPI.getMemoryAPI().getUsedMemory(true)+"%&7)", s);
						TheAPI.msg("&7║  Free: &e"+TheAPI.getMemoryAPI().getFreeMemory(false)+" &7(&e"+TheAPI.getMemoryAPI().getFreeMemory(true)+"%&7)", s);
						TheAPI.msg("&7║ Worlds:", s);
						for(World w : Bukkit.getWorlds())
							TheAPI.msg("&7║  - &e"+w.getName()+" &7(Ent:&e"+w.getEntities().size()+"&7, Players:&e"+w.getPlayers().size()+"&7, Chunks:&e"+w.getLoadedChunks().length+"&7)", s);
						TheAPI.msg("&7║ Players:", s);
						TheAPI.msg("&7║  Max: &e"+TheAPI.getMaxPlayers(), s);
						TheAPI.msg("&7║  Online: &e"+TheAPI.getOnlinePlayers().size()+" &7(&e"+(TheAPI.getOnlinePlayers().size()/((double)TheAPI.getMaxPlayers()/100))+"%&7)", s);
						OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
						TheAPI.msg("&7║ System:", s);
						TheAPI.msg("&7║  CPU: &e"+String.format("%2.02f",TheAPI.getProcessCpuLoad()).replaceFirst(",", ".").replaceFirst("\\.00", "")+"%", s);
						TheAPI.msg("&7║  Name: &e"+osBean.getName(), s);
						TheAPI.msg("&7║  Procesors: &e"+osBean.getAvailableProcessors(), s);
						TheAPI.msg("&7║ TPS: &e"+TheAPI.getServerTPS(TPSType.ONE_MINUTE)+", "+TheAPI.getServerTPS(TPSType.FIVE_MINUTES)+", "+TheAPI.getServerTPS(TPSType.FIFTEEN_MINUTES), s);
						TheAPI.msg("&7║ Version: &ev" + LoaderClass.plugin.getDescription().getVersion(), s);
						if (LoaderClass.plugin.getTheAPIsPlugins().size() != 0) {
							TheAPI.msg("&7║ Plugins using TheAPI:", s);
							for (Plugin a : LoaderClass.plugin.getTheAPIsPlugins())
								TheAPI.msg("&7║  - &e" + getPlugin(a), s);
						}
						TheAPI.msg("&7╚═════════════════════════════", s);
					}
				}.runAsync();
				return true;
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("worldsmanager") || args[0].equalsIgnoreCase("world") || args[0].equalsIgnoreCase("worlds") || args[0].equalsIgnoreCase("wm") || args[0].equalsIgnoreCase( "mw") || args[0].equalsIgnoreCase( "worldmanager")) {
			if (perm(s,"WorldsManager")) {
				if (args.length == 1) {
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&e/TheAPI WorldsManager Create <world> <generator>", s);
					TheAPI.msg("&e/TheAPI WorldsManager Delete <world>", s);
					TheAPI.msg("&e/TheAPI WorldsManager Unload <world>", s);
					TheAPI.msg("&e/TheAPI WorldsManager Load <world> <generator>", s);
					TheAPI.msg("&e/TheAPI WorldsManager Teleport <world> [player]", s);
					TheAPI.msg("&e/TheAPI WorldsManager Save <world>", s);
					TheAPI.msg("&e/TheAPI WorldsManager SaveAll", s);
					TheAPI.msg("&7Worlds:", s);
					for (World w : Bukkit.getWorlds())
						TheAPI.msg("&7 - &e" + w.getName(), s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("Teleport") || args[1].equalsIgnoreCase("tp")) {
					if (args.length == 2) {
						if (s instanceof Player)
							TheAPI.msg("&e/TheAPI WorldsManager Teleport <world> [player]", s);
						else
							TheAPI.msg("&e/TheAPI WorldsManager Teleport <world> <player>", s);
						TheAPI.msg("&7Worlds:", s);
						for (World w : Bukkit.getWorlds())
							TheAPI.msg("&7 - &e" + w.getName(), s);
						return true;
					}
					if (Bukkit.getWorld(args[2]) == null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&7World with name '" + args[2] + "' doesn't exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (args.length == 3) {
						if (s instanceof Player) {
							try {
								((Player) s).teleport(Bukkit.getWorld(args[2]).getSpawnLocation());
							} catch (Exception e) {
								((Player) s).teleport(new Location(Bukkit.getWorld(args[2]), 60, 60, 60));
							}
							TheAPI.msg("&eTeleporting to the world " + args[2] + "..", s);
							return true;
						} else
							TheAPI.msg("&e/TheAPI WorldsManager Teleport <world> <player>", s);
						return true;
					}
					if (args.length == 4) {
						Player p = Bukkit.getPlayer(args[3]);
						if (p == null) {
							TheAPI.msg("&ePlayer " + args[3] + " isn't online", p);
							return true;
						}
						TheAPI.msg("&eTeleporting to the world " + args[2] + "..", p);
						TheAPI.msg("&eTeleporting player " + p.getName() + " to the world " + args[2] + "..", s);
						return true;
					}
				}
				if (args[1].equalsIgnoreCase("saveall")) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eTheAPI WorldsManager saving " + (Bukkit.getWorlds().size()) + " world(s)..", s);
						for (World w : Bukkit.getWorlds())
							w.save();
						TheAPI.msg("&eWorlds saved..", s);
						TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("save")) {
					if (args.length == 2) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Save <world>", s);
						TheAPI.msg("&7Worlds:", s);
						for (World w : Bukkit.getWorlds())
							TheAPI.msg("&7 - &e" + w.getName(), s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}

					if (Bukkit.getWorld(args[2]) == null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eWorld with name '" + args[2] + "' doesn't exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}

					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eTheAPI WorldsManager saving world with name '" + args[2] + "'..", s);
					Bukkit.getWorld(args[2]).save();
					TheAPI.msg("&eWorld with name '" + args[2] + "' saved.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("unload")) {
					if (args.length == 2) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Unload <world>", s);
						TheAPI.msg("&7Worlds:", s);
						for (World w : Bukkit.getWorlds())
							TheAPI.msg("&7 - &e" + w.getName(), s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (Bukkit.getWorld(args[2]) == null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eWorld with name '" + args[2] + "' doesn't exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eTheAPI WorldsManager unloading world with name '" + args[2] + "'..", s);
					TheAPI.getWorldsManager().unloadWorld(args[2], true);

					List<String> a = LoaderClass.config.getConfig().getStringList("Worlds");
					a.remove(args[2]);
					LoaderClass.config.getConfig().set("Worlds", a);
					TheAPI.msg("&eWorld with name '" + args[2] + "' unloaded.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("load")) {
					if (args.length == 2) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Load <world> <generator>", s);
						TheAPI.msg("&7Generators:", s);
						for (String w : Arrays.asList("Default", "Nether", "The_End", "The_Void", "Flat"))
							TheAPI.msg("&7 - &e" + w, s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (Bukkit.getWorld(args[2]) != null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eWorld with name '" + args[2] + "' already exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (args.length == 3) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Load <world> <generator>", s);
						TheAPI.msg("&7Generators:", s);
						for (String w : Arrays.asList("Default", "Nether", "The_End", "The_Void", "Flat"))
							TheAPI.msg("&7 - &e" + w, s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (new File(Bukkit.getWorldContainer().getPath() + "/" + args[2] + "/session.lock").exists()) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eTheAPI WorldsManager loading world with name '" + args[2] + "'..", s);
						int generator = 0;
						if (args[3].equalsIgnoreCase("Flat"))
							generator = 1;
						if (args[3].equalsIgnoreCase("Nether"))
							generator = 2;
						if (args[3].equalsIgnoreCase("The_End") || args[3].equalsIgnoreCase("End"))
							generator = 3;
						if (args[3].equalsIgnoreCase("The_Void") || args[3].equalsIgnoreCase("Void")
								|| args[3].equalsIgnoreCase("Empty"))
							generator = 4;
						Environment env = Environment.NORMAL;
						WorldType wt = WorldType.NORMAL;
						switch (generator) {
						case 1:
							wt = WorldType.FLAT;
							break;
						case 2:
							env = Environment.NETHER;
							break;
						case 3:
							try {
								env = Environment.valueOf("THE_END");
							} catch (Exception e) {
								env = Environment.valueOf("END");
							}
							break;
						case 4:
							wt = null;
							break;
						}
						TheAPI.getWorldsManager().load(args[2], env, wt);
						List<String> a = LoaderClass.config.getStringList("Worlds");
						a.add(args[2]);
						LoaderClass.config.set("Worlds", a);
						LoaderClass.config.set("WorldsSetting." + args[2] + ".Generator", generator);
						LoaderClass.config.set("WorldsSetting." + args[2] + ".GenerateStructures", true);
						LoaderClass.config.save();
						TheAPI.msg("&eWorld with name '" + args[2] + "' loaded.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eWorld with name '" + args[2] + "' doesn't exists.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("delete")) {
					if (args.length == 2) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Delete <world>", s);
						TheAPI.msg("&7Worlds:", s);
						for (World w : Bukkit.getWorlds())
							TheAPI.msg("&7 - &e" + w.getName(), s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (Bukkit.getWorld(args[2]) == null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eWorld with name '" + args[2] + "' doesn't exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eTheAPI WorldsManager deleting world with name '" + args[2] + "'..", s);
					TheAPI.getWorldsManager().delete(Bukkit.getWorld(args[2]), true);
					List<String> a = LoaderClass.config.getConfig().getStringList("Worlds");
					if(a.contains(args[2])) {
					a.remove(args[2]);
					LoaderClass.config.set("Worlds", a);
					}
					LoaderClass.config.set("WorldsSetting." + args[2], null);
					LoaderClass.config.save();
					TheAPI.msg("&eWorld with name '" + args[2] + "' deleted.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				if (args[1].equalsIgnoreCase("create")) {
					if (args.length == 2) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Create <world> <generator>", s);
						TheAPI.msg("&7Generators:", s);
						for (String w : Arrays.asList("Default", "Nether", "The_End", "The_Void"))
							TheAPI.msg("&7 - &e" + w, s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					if (Bukkit.getWorld(args[2]) != null) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&eWorld with name '" + args[2] + "' already exists.", s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}

					if (args.length == 3) {
						TheAPI.msg("&7-----------------", s);
						TheAPI.msg("&e/TheAPI WorldsManager Create " + args[2] + " <generator>", s);
						TheAPI.msg("&7Generators:", s);
						for (String w : Arrays.asList("Default", "Nether", "The_End", "The_Void", "Flat"))
							TheAPI.msg("&7 - &e" + w, s);
						TheAPI.msg("&7-----------------", s);
						return true;
					}
					int generator = 0;
					if (args[3].equalsIgnoreCase("Flat"))
						generator = 1;
					if (args[3].equalsIgnoreCase("Nether"))
						generator = 2;
					if (args[3].equalsIgnoreCase("The_End") || args[3].equalsIgnoreCase("End"))
						generator = 3;
					if (args[3].equalsIgnoreCase("The_Void") || args[3].equalsIgnoreCase("Void")
							|| args[3].equalsIgnoreCase("Empty"))
						generator = 4;
					TheAPI.msg("&7-----------------", s);
					TheAPI.msg("&eTheAPI WorldsManager creating new world with name '" + args[2] + "' using generator '"
							+ args[3] + "'..", s);
					Environment env = Environment.NORMAL;
					WorldType wt = WorldType.NORMAL;
					switch (generator) {
					case 1:
						wt = WorldType.FLAT;
						break;
					case 2:
						env = Environment.NETHER;
						break;
					case 3:
						try {
							env = Environment.valueOf("THE_END");
						} catch (Exception e) {
							env = Environment.valueOf("END");
						}
						break;
					case 4:
						wt = null;
						break;
					}
					List<String> a = LoaderClass.config.getStringList("Worlds");
					if(!a.contains(args[2])) {
					a.add(args[2]);
					LoaderClass.config.set("Worlds", a);
				    }
					LoaderClass.config.set("WorldsSetting." + args[2] + ".Generator", generator);
					LoaderClass.config.set("WorldsSetting." + args[2] + ".GenerateStructures", true);
					LoaderClass.config.save();
					TheAPI.getWorldsManager().create(args[2], env, wt, true, 0);
					TheAPI.msg("&eWorld with name '" + args[2] + "' created.", s);
					TheAPI.msg("&7-----------------", s);
					return true;
				}
				TheAPI.msg("&7-----------------", s);
				TheAPI.msg("&e/TheAPI WorldsManager Create <world> <generator>", s);
				TheAPI.msg("&e/TheAPI WorldsManager Delete <world>", s);
				TheAPI.msg("&e/TheAPI WorldsManager Unload <world>", s);
				TheAPI.msg("&e/TheAPI WorldsManager Load <world> <generator>", s);
				TheAPI.msg("&e/TheAPI WorldsManager Teleport <world> [player]", s);
				TheAPI.msg("&e/TheAPI WorldsManager Save <world>", s);
				TheAPI.msg("&e/TheAPI WorldsManager SaveAll", s);
				TheAPI.msg("&7Worlds:", s);
				for (World w : Bukkit.getWorlds())
					TheAPI.msg("&7 - &e" + w.getName(), s);
				TheAPI.msg("&7-----------------", s);
				return true;

			}
			return true;
		}
		return false;
	}

	List<String> getWorlds() {
		List<String> list = Lists.newArrayList();
		for (World w : Bukkit.getWorlds())
			list.add(w.getName());
		return list;
	}

	@Override
	public List<String> onTabComplete(CommandSender s, Command arg1, String arg2, String[] args) {
		List<String> c = Lists.newArrayList();
		if (args.length == 1) {
			if (s.hasPermission("TheAPI.Command.Info"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("Info"), Lists.newArrayList()));
			if (s.hasPermission("TheAPI.Command.Reload"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("Reload"), Lists.newArrayList()));
			if (s.hasPermission("TheAPI.Command.ClearCache"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("ClearCache"), Lists.newArrayList()));
			if (s.hasPermission("TheAPI.Command.WorldsManager"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("WorldsManager"), Lists.newArrayList()));
			if (s.hasPermission("TheAPI.Command.Invsee"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("Invsee"), Lists.newArrayList()));
			if (s.hasPermission("TheAPI.Command.PluginManager"))
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("PluginManager"), Lists.newArrayList()));
			if (s.isOp())
				c.addAll(StringUtil.copyPartialMatches(args[0], Arrays.asList("Test"), Lists.newArrayList()));
		}
		if (args[0].equalsIgnoreCase("Test") && s.isOp()) {
			if (args.length == 2) {
				c.addAll(StringUtil.copyPartialMatches(args[1],
						Arrays.asList("ActionBar", "hideShowEntity", "BlocksAPI", "BossBar", "PlayerName",
								"RankingAPI", "Scoreboard", "TabList", "Title", "GUICreatorAPI"),
						Lists.newArrayList()));
			}
		}
		if (args[0].equalsIgnoreCase("Invsee") && s.hasPermission("TheAPI.Command.Invsee")) {
			if (args.length == 2)
			return null;
		}
		if (s.hasPermission("TheAPI.Command.PluginManager"))
		if (args[0].equalsIgnoreCase("PluginManager") || args[0].equalsIgnoreCase("pm")) {
			if (args.length == 2) {
				c.addAll(StringUtil.copyPartialMatches(args[1],
						Arrays.asList("Load", "Unload", "Reload", "Enable", "Disable", "Info", "Files", "DisableAll", "EnableAll", "ReloadAll", "UnloadAll", "LoadAll"),
						Lists.newArrayList()));
			}
			if (args.length == 3) {
				if (args[1].equalsIgnoreCase("Load")) {
					c.addAll(StringUtil.copyPartialMatches(args[2],
							TheAPI.getPluginsManagerAPI().getPluginsToLoad(), Lists.newArrayList()));
				}
				if (args[1].equalsIgnoreCase("Unload")||args[1].equalsIgnoreCase("Enable") || args[1].equalsIgnoreCase("Disable")
						 || args[1].equalsIgnoreCase("Info") || args[1].equalsIgnoreCase("Reload")) {
						c.addAll(StringUtil.copyPartialMatches(args[2],
								TheAPI.getPluginsManagerAPI().getPluginsNames(), Lists.newArrayList()));
				}
			}}
		if (s.hasPermission("TheAPI.Command.WorldsManager"))
		if (args[0].equalsIgnoreCase("WorldsManager") || args[0].equalsIgnoreCase("wm")) {
			if (args.length == 2) {
				c.addAll(StringUtil.copyPartialMatches(args[1],
						Arrays.asList("Create", "Delete", "Load", "Teleport", "Unload", "Save", "SaveAll"),
						Lists.newArrayList()));
			}
			if (args.length >= 3) {
				if (args[1].equalsIgnoreCase("Create") || args[1].equalsIgnoreCase("Load")) {
					if (args.length == 3)
						return Arrays.asList("?");
					if (args.length == 4)
						c.addAll(StringUtil.copyPartialMatches(args[3],
								Arrays.asList("Default", "Nether", "The_End", "The_Void", "Flat"), Lists.newArrayList()));
				}
				if (args[1].equalsIgnoreCase("Teleport")) {
					if (args.length == 3)
						c.addAll(StringUtil.copyPartialMatches(args[1], getWorlds(), Lists.newArrayList()));
					if (args.length == 4)
						return null;
				}
				if (args[1].equalsIgnoreCase("Unload") || args[1].equalsIgnoreCase("Delete")
						|| args[1].equalsIgnoreCase("Save")) {
					if (args.length == 3)
						c.addAll(StringUtil.copyPartialMatches(args[1], getWorlds(), Lists.newArrayList()));
				}
			}
		}
		return c;
	}

}
