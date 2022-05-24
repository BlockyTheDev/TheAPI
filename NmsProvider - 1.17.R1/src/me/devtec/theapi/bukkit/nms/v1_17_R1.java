package me.devtec.theapi.bukkit.nms;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.devtec.shared.Ref;
import me.devtec.shared.components.Component;
import me.devtec.shared.components.ComponentAPI;
import me.devtec.shared.events.EventManager;
import me.devtec.theapi.bukkit.BukkitLoader;
import me.devtec.theapi.bukkit.BukkitLoader.InventoryClickType;
import me.devtec.theapi.bukkit.events.ServerListPingEvent;
import me.devtec.theapi.bukkit.events.ServerListPingEvent.PlayerProfile;
import me.devtec.theapi.bukkit.game.Position;
import me.devtec.theapi.bukkit.game.TheMaterial;
import me.devtec.theapi.bukkit.gui.AnvilGUI;
import me.devtec.theapi.bukkit.gui.GUI.ClickType;
import me.devtec.theapi.bukkit.gui.HolderGUI;
import me.devtec.theapi.bukkit.nms.utils.InventoryUtils;
import me.devtec.theapi.bukkit.nms.utils.InventoryUtils.DestinationType;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.Particle;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatClickable.EnumClickAction;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatHexColor;
import net.minecraft.network.chat.ChatHoverable.EnumHoverAction;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutExperience;
import net.minecraft.network.protocol.game.PacketPlayOutHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.network.protocol.game.PacketPlayOutPosition;
import net.minecraft.network.protocol.game.PacketPlayOutResourcePackSend;
import net.minecraft.network.protocol.game.PacketPlayOutRespawn;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.network.protocol.status.PacketStatusOutServerInfo;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.network.protocol.status.ServerPing.ServerData;
import net.minecraft.network.protocol.status.ServerPing.ServerPingPlayerSample;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.inventory.ICrafting;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria.EnumScoreboardHealthDisplay;

public class v1_17_R1 implements NmsProvider {
	private static final MinecraftServer server = MinecraftServer.getServer();
	private static final sun.misc.Unsafe unsafe = (sun.misc.Unsafe) Ref
			.getNulled(Ref.field(sun.misc.Unsafe.class, "theUnsafe"));

	@Override
	public Collection<? extends Player> getOnlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}

	@Override
	public Object getEntity(Entity entity) {
		return ((CraftEntity) entity).getHandle();
	}

	@Override
	public Object getEntityLiving(LivingEntity entity) {
		return ((CraftLivingEntity) entity).getHandle();
	}

	@Override
	public Object getPlayer(Player player) {
		return ((CraftPlayer) player).getHandle();
	}

	@Override
	public Object getWorld(World world) {
		return ((CraftWorld) world).getHandle();
	}

	@Override
	public Object getChunk(Chunk chunk) {
		return ((CraftChunk) chunk).getHandle();
	}

	@Override
	public int getEntityId(Object entity) {
		return ((net.minecraft.world.entity.Entity) entity).getId();
	}

	@Override
	public Object getScoreboardAction(Action type) {
		return type == Action.CHANGE ? ScoreboardServer.Action.a : ScoreboardServer.Action.b;
	}

	@Override
	public Object getEnumScoreboardHealthDisplay(DisplayType type) {
		return type == DisplayType.INTEGER ? EnumScoreboardHealthDisplay.a : EnumScoreboardHealthDisplay.b;
	}

	@Override
	public Object getNBT(ItemStack itemStack) {
		return ((net.minecraft.world.item.ItemStack) this.asNMSItem(itemStack)).getOrCreateTag();
	}

	@Override
	public Object parseNBT(String json) {
		try {
			return MojangsonParser.parse(json);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public ItemStack setNBT(ItemStack stack, Object nbt) {
		if (nbt instanceof NBTEdit)
			nbt = ((NBTEdit) nbt).getNBT();
		net.minecraft.world.item.ItemStack i = (net.minecraft.world.item.ItemStack) this.asNMSItem(stack);
		i.setTag((NBTTagCompound) nbt);
		return this.asBukkitItem(i);
	}

	@Override
	public Object asNMSItem(ItemStack stack) {
		if (stack == null)
			return net.minecraft.world.item.ItemStack.b;
		return CraftItemStack.asNMSCopy(stack);
	}

	@Override
	public ItemStack asBukkitItem(Object stack) {
		return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) stack);
	}

	@Override
	public int getContainerId(Object container) {
		return ((Container) container).j;
	}

	@Override
	public Object packetResourcePackSend(String url, String hash, boolean requireRP, String prompt) {
		return new PacketPlayOutResourcePackSend(url, hash, requireRP, prompt == null ? null
				: (IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(prompt)));
	}

	@Override
	public Object packetSetSlot(int container, int slot, int changeId, Object itemStack) {
		return new PacketPlayOutSetSlot(container, changeId, slot,
				(net.minecraft.world.item.ItemStack) (itemStack == null ? this.asNMSItem(null) : itemStack));
	}

	@Override
	public Object packetEntityMetadata(int entityId, Object dataWatcher, boolean bal) {
		return new PacketPlayOutEntityMetadata(entityId, (net.minecraft.network.syncher.DataWatcher) dataWatcher, bal);
	}

	@Override
	public Object packetEntityDestroy(int... ids) {
		return new PacketPlayOutEntityDestroy(ids);
	}

	@Override
	public Object packetSpawnEntity(Object entity, int id) {
		return new PacketPlayOutSpawnEntity((net.minecraft.world.entity.Entity) entity, id);
	}

	@Override
	public Object packetNamedEntitySpawn(Object player) {
		return new PacketPlayOutNamedEntitySpawn((EntityHuman) player);
	}

	@Override
	public Object packetSpawnEntityLiving(Object entityLiving) {
		return new PacketPlayOutSpawnEntityLiving((EntityLiving) entityLiving);
	}

	@Override
	public Object packetPlayerListHeaderFooter(String header, String footer) {
		return new PacketPlayOutPlayerListHeaderFooter(
				(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(header)),
				(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(footer)));
	}

	@Override
	public Object packetBlockChange(World world, Position position) {
		return new PacketPlayOutBlockChange((BlockPosition) position.getBlockPosition(),
				(IBlockData) position.getIBlockData());
	}

	@Override
	public Object packetBlockChange(World world, int x, int y, int z) {
		return new PacketPlayOutBlockChange(new BlockPosition(x, y, z),
				(IBlockData) this.getBlock(this.getChunk(world, x >> 4, z >> 4), x, y, z));
	}

	@Override
	public Object packetScoreboardObjective() {
		try {
			return v1_17_R1.unsafe.allocateInstance(PacketPlayOutScoreboardObjective.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object packetScoreboardDisplayObjective(int id, Object scoreboardObjective) {
		return new PacketPlayOutScoreboardDisplayObjective(id,
				scoreboardObjective == null ? null : (ScoreboardObjective) scoreboardObjective);
	}

	@Override
	public Object packetScoreboardTeam() {
		try {
			return v1_17_R1.unsafe.allocateInstance(PacketPlayOutScoreboardTeam.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object packetScoreboardScore(Action action, String player, String line, int score) {
		return new PacketPlayOutScoreboardScore(
				(net.minecraft.server.ScoreboardServer.Action) this.getScoreboardAction(action), player, line, score);
	}

	@Override
	public Object packetTitle(TitleAction action, String text, int fadeIn, int stay, int fadeOut) {
		switch (action) {
		case ACTIONBAR:
			return new ClientboundSetActionBarTextPacket(
					(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(text)));
		case TITLE:
			return new ClientboundSetTitleTextPacket(
					(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(text)));
		case SUBTITLE:
			return new ClientboundSetSubtitleTextPacket(
					(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(text)));
		case TIMES:
			return new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
		case CLEAR:
		case RESET:
			return new ClientboundClearTitlesPacket(true);
		}
		return null;
	}

	@Override
	public Object packetChat(ChatType type, Object chatBase, UUID uuid) {
		switch (type) {
		case CHAT:
			return new PacketPlayOutChat((IChatBaseComponent) chatBase, ChatMessageType.a, uuid);
		case GAME_INFO:
			return new PacketPlayOutChat((IChatBaseComponent) chatBase, ChatMessageType.c, uuid);
		case SYSTEM:
			return new PacketPlayOutChat((IChatBaseComponent) chatBase, ChatMessageType.b, uuid);
		}
		return null;
	}

	@Override
	public Object packetChat(ChatType type, String text, UUID uuid) {
		return this.packetChat(type, this.toIChatBaseComponent(ComponentAPI.fromString(text)), uuid);
	}

	@Override
	public void postToMainThread(Runnable runnable) {
		v1_17_R1.server.executeSync(runnable);
	}

	@Override
	public Object getMinecraftServer() {
		return v1_17_R1.server;
	}

	@Override
	public Thread getServerThread() {
		return v1_17_R1.server.an;
	}

	@Override
	public double[] getServerTPS() {
		return v1_17_R1.server.recentTps;
	}

	private IChatBaseComponent convert(Component c) {
		ChatComponentText current = new ChatComponentText(c.getText());
		ChatModifier modif = current.getChatModifier();
		if (c.getColor() != null && !c.getColor().isEmpty())
			if (c.getColor().startsWith("#"))
				modif = modif.setColor(ChatHexColor.a(c.getColor()));
			else
				modif = modif.setColor(EnumChatFormat.a(c.colorToChar()));
		if (c.getClickEvent() != null)
			modif = modif.setChatClickable(new ChatClickable(
					EnumClickAction.valueOf(c.getClickEvent().getAction().name()), c.getClickEvent().getValue()));
		if (c.getHoverEvent() != null)
			modif = modif.setChatHoverable(EnumHoverAction.a(c.getHoverEvent().getAction().name().toLowerCase())
					.a((IChatBaseComponent) this.toIChatBaseComponent(c.getHoverEvent().getValue())));
		modif = modif.setBold(c.isBold());
		modif = modif.setItalic(c.isItalic());
		modif = modif.setRandom(c.isObfuscated());
		modif = modif.setUnderline(c.isUnderlined());
		modif = modif.setStrikethrough(c.isStrikethrough());
		current.setChatModifier(modif);
		return current;
	}

	@Override
	public Object toIChatBaseComponents(List<Component> components) {
		List<IChatBaseComponent> chat = new ArrayList<>();
		chat.add(new ChatComponentText(""));
		for (Component c : components) {
			if (c.getText() == null || c.getText().isEmpty()) {
				if (c.getExtra() != null)
					this.addConverted(chat, c.getExtra());
				continue;
			}
			chat.add(this.convert(c));
			if (c.getExtra() != null)
				this.addConverted(chat, c.getExtra());
		}
		return chat.toArray(new IChatBaseComponent[0]);
	}

	private void addConverted(List<IChatBaseComponent> chat, List<Component> extra) {
		for (Component c : extra) {
			if (c.getText() == null || c.getText().isEmpty()) {
				if (c.getExtra() != null)
					this.addConverted(chat, c.getExtra());
				continue;
			}
			chat.add(this.convert(c));
		}
	}

	@Override
	public Object toIChatBaseComponents(Component co) {
		List<IChatBaseComponent> chat = new ArrayList<>();
		chat.add(new ChatComponentText(""));
		if (co.getText() != null && !co.getText().isEmpty())
			chat.add(this.convert(co));
		if (co.getExtra() != null)
			for (Component c : co.getExtra()) {
				if (c.getText() == null || c.getText().isEmpty()) {
					if (c.getExtra() != null)
						this.addConverted(chat, c.getExtra());
					continue;
				}
				chat.add(this.convert(c));
				if (c.getExtra() != null)
					this.addConverted(chat, c.getExtra());
			}
		return chat.toArray(new IChatBaseComponent[0]);
	}

	@Override
	public Object toIChatBaseComponent(Component co) {
		ChatComponentText main = new ChatComponentText("");
		List<IChatBaseComponent> chat = new ArrayList<>();
		if (co.getText() != null && !co.getText().isEmpty())
			chat.add(this.convert(co));
		if (co.getExtra() != null)
			for (Component c : co.getExtra()) {
				if (c.getText() == null || c.getText().isEmpty()) {
					if (c.getExtra() != null)
						this.addConverted(chat, c.getExtra());
					continue;
				}
				chat.add(this.convert(c));
				if (c.getExtra() != null)
					this.addConverted(chat, c.getExtra());
			}
		for (IChatBaseComponent d : chat)
			main.addSibling(d);
		return main.getSiblings().isEmpty() ? ChatComponentText.d : main;
	}

	@Override
	public Object toIChatBaseComponent(List<Component> cc) {
		ChatComponentText main = new ChatComponentText("");
		for (Component c : cc)
			main.addSibling((IChatBaseComponent) this.toIChatBaseComponent(c));
		return main.getSiblings().isEmpty() ? ChatComponentText.d : main;
	}

	@Override
	public Object chatBase(String json) {
		return IChatBaseComponent.ChatSerializer.a(json);
	}

	@Override
	public String fromIChatBaseComponent(Object component) {
		return CraftChatMessage.fromComponent((IChatBaseComponent) component);
	}

	@Override
	public TheMaterial toMaterial(Object blockOrItemOrIBlockData) {
		if (blockOrItemOrIBlockData == null)
			return new TheMaterial(Material.AIR);
		if (blockOrItemOrIBlockData instanceof Block) {
			Block b = (Block) blockOrItemOrIBlockData;
			return new TheMaterial(CraftItemStack.asNewCraftStack(Item.getItemOf(b)));
		}
		if (blockOrItemOrIBlockData instanceof Item) {
			Item b = (Item) blockOrItemOrIBlockData;
			return new TheMaterial(CraftItemStack.asNewCraftStack(b));
		}
		if (blockOrItemOrIBlockData instanceof IBlockData) {
			IBlockData b = (IBlockData) blockOrItemOrIBlockData;
			return new TheMaterial(CraftItemStack.asNewCraftStack(Item.getItemOf(b.getBlock())));
		}
		return null;
	}

	@Override
	public Object toIBlockData(TheMaterial material) {
		if (material == null || material.getType() == null || material.getType() == Material.AIR)
			return Blocks.a.getBlockData();
		return ((CraftBlockData) Bukkit.createBlockData(material.getType(), material.getData() + "")).getState();
	}

	@Override
	public Object toItem(TheMaterial material) {
		if (material == null || material.getType() == null || material.getType() == Material.AIR)
			return Item.getItemOf(Blocks.a);
		return Item.getItemOf(((CraftBlockData) Bukkit.createBlockData(material.getType(), material.getData() + ""))
				.getState().getBlock());
	}

	@Override
	public Object toBlock(TheMaterial material) {
		if (material == null || material.getType() == null || material.getType() == Material.AIR)
			return Blocks.a;
		return ((CraftBlockData) Bukkit.createBlockData(material.getType(), material.getData() + "")).getState()
				.getBlock();
	}

	@Override
	public Object getChunk(World world, int x, int z) {
		return ((CraftChunk) world.getChunkAt(x, z)).getHandle();
	}

	@Override
	public void setBlock(Object chunk, int x, int y, int z, Object IblockData, int data) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk) chunk;
		int yy = c.getSectionIndex(y - 1);
		ChunkSection sc = c.getSections()[yy];
		if (sc == null)
			c.getSections()[yy] = sc = new ChunkSection(y >> 4 << 4);
		BlockPosition pos = new BlockPosition(x, y, z);
		// REMOVE TILE ENTITY
		c.l.remove(pos);

		sc.getBlocks().b(x & 15, y & 15, z & 15, (IBlockData) IblockData);

		// ADD TILE ENTITY
		if (IblockData instanceof ITileEntity) {
			TileEntity ent = ((ITileEntity) IblockData).createTile(pos, (IBlockData) IblockData);
			c.l.put(pos, ent);
			Object packet = ent.getUpdatePacket();
			Bukkit.getOnlinePlayers().forEach(player -> BukkitLoader.getPacketHandler().send(player, packet));
		}
	}

	@Override
	public void updateLightAt(Object chunk, int x, int y, int z) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk) chunk;
		c.i.getChunkProvider().getLightEngine().a(new BlockPosition(x, y, z));
	}

	@Override
	public Object getBlock(Object chunk, int x, int y, int z) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk) chunk;
		int yy = c.getSectionIndex(y - 1);
		ChunkSection sc = c.getSections()[yy];
		if (sc == null)
			return Blocks.a.getBlockData();
		return sc.getBlocks().a(x & 15, y & 15, z & 15);
	}

	@Override
	public int getData(Object chunk, int x, int y, int z) {
		return 0;
	}

	@Override
	public int getCombinedId(Object IblockDataOrBlock) {
		return Block.getCombinedId((IBlockData) IblockDataOrBlock);
	}

	@Override
	public Object blockPosition(int blockX, int blockY, int blockZ) {
		return new BlockPosition(blockX, blockY, blockZ);
	}

	@Override
	public Object toIBlockData(Object data) {
		return ((CraftBlockData) data).getState();
	}

	@Override
	public Object toIBlockData(BlockState state) {
		return CraftMagicNumbers.getBlock(state.getType(), state.getRawData());
	}

	@Override
	public Object toBlock(Material type) {
		return CraftMagicNumbers.getBlock(type);
	}

	@Override
	public Object toItem(Material type, int data) {
		return CraftMagicNumbers.getItem(type, (short) data);
	}

	@Override
	public Object toIBlockData(Material type, int data) {
		return CraftMagicNumbers.getBlock(type, (byte) data);
	}

	@Override
	public Chunk toBukkitChunk(Object nmsChunk) {
		return ((net.minecraft.world.level.chunk.Chunk) nmsChunk).bukkitChunk;
	}

	@Override
	public int getPing(Player player) {
		return ((EntityPlayer) this.getPlayer(player)).e;
	}

	@Override
	public Object getPlayerConnection(Player player) {
		return ((EntityPlayer) this.getPlayer(player)).b;
	}

	@Override
	public Object getConnectionNetwork(Object playercon) {
		return ((PlayerConnection) playercon).a;
	}

	@Override
	public Object getNetworkChannel(Object network) {
		return ((NetworkManager) network).k;
	}

	@Override
	public Object packetOpenWindow(int id, String legacy, int size, String title) {
		Containers<?> windowType = Containers.a;
		switch (size) {
		case 0: {
			windowType = Containers.h;
			break;
		}
		case 18: {
			windowType = Containers.b;
			break;
		}
		case 27: {
			windowType = Containers.c;
			break;
		}
		case 36: {
			windowType = Containers.d;
			break;
		}
		case 45: {
			windowType = Containers.e;
			break;
		}
		case 54: {
			windowType = Containers.f;
			break;
		}
		}
		return new PacketPlayOutOpenWindow(id, windowType,
				(IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(title)));
	}

	@Override
	public void closeGUI(Player player, Object container, boolean closePacket) {
		if (closePacket)
			BukkitLoader.getPacketHandler().send(player,
					new PacketPlayOutCloseWindow(BukkitLoader.getNmsProvider().getContainerId(container)));
		EntityPlayer nmsPlayer = (EntityPlayer) this.getPlayer(player);
		nmsPlayer.bV = nmsPlayer.bU;
		((Container) container).transferTo(nmsPlayer.bV, (CraftPlayer) player);
	}

	@Override
	public void setSlot(Object container, int slot, Object item) {
		((Container) container).a(slot, (net.minecraft.world.item.ItemStack) item);
	}

	@Override
	public void setGUITitle(Player player, Object container, String legacy, int size, String title) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		int id = ((Container) container).j;
		NonNullList<net.minecraft.world.item.ItemStack> nmsItems = ((Container) container).k;
		BukkitLoader.getPacketHandler().send(player, this.packetOpenWindow(id, legacy, size, title));
		int i = 0;
		int statusId = this.incrementStateId(container);
		for (net.minecraft.world.item.ItemStack o : nmsItems)
			BukkitLoader.getPacketHandler().send(player, this.packetSetSlot(id, i++, statusId, o));
		nmsPlayer.bV = (Container) container;
		((Container) container).addSlotListener((ICrafting) nmsPlayer);
		((Container) container).checkReachable = false;
	}

	@Override
	public void openGUI(Player player, Object container, String legacy, int size, String title, ItemStack[] items) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		int id = ((Container) container).j;
		net.minecraft.world.item.ItemStack[] nmsItems = new net.minecraft.world.item.ItemStack[items.length];
		for (int i = 0; i < items.length; ++i) {
			ItemStack is = items[i];
			if (is == null || is.getType() == Material.AIR)
				continue;
			net.minecraft.world.item.ItemStack item = null;
			((Container) container).b(i, item = (net.minecraft.world.item.ItemStack) this.asNMSItem(is));
			nmsItems[i] = item;
		}
		BukkitLoader.getPacketHandler().send(player, this.packetOpenWindow(id, legacy, size, title));
		int i = 0;
		int statusId = this.incrementStateId(container);
		for (net.minecraft.world.item.ItemStack o : nmsItems)
			BukkitLoader.getPacketHandler().send(player, this.packetSetSlot(id, i++, statusId, o));
		nmsPlayer.bV.transferTo((Container) container, (CraftPlayer) player);
		nmsPlayer.bV = (Container) container;
		nmsPlayer.initMenu((Container) container);
		((Container) container).checkReachable = false;
	}

	@Override
	public void openAnvilGUI(Player player, Object con, String title, ItemStack[] items) {
		Container container = (Container) con;
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		int id = container.j;
		net.minecraft.world.item.ItemStack[] nmsItems = new net.minecraft.world.item.ItemStack[items.length];
		for (int i = 0; i < items.length; ++i) {
			ItemStack is = items[i];
			if (is == null || is.getType() == Material.AIR)
				continue;
			net.minecraft.world.item.ItemStack item = null;
			container.b(i, item = (net.minecraft.world.item.ItemStack) this.asNMSItem(is));
			nmsItems[i] = item;
		}
		BukkitLoader.getPacketHandler().send(player, this.packetOpenWindow(id, "minecraft:anvil", 0, title));
		int i = 0;
		int statusId = this.incrementStateId(container);
		for (net.minecraft.world.item.ItemStack o : nmsItems)
			BukkitLoader.getPacketHandler().send(player, this.packetSetSlot(id, i++, statusId, o));
		nmsPlayer.bV.transferTo(container, (CraftPlayer) player);
		nmsPlayer.bV = container;
		nmsPlayer.initMenu(container);
		container.checkReachable = false;
	}

	@Override
	public Object createContainer(Inventory inv, Player player) {
		return inv.getType() == InventoryType.ANVIL ? this.createAnvilContainer(inv, player)
				: new CraftContainer(inv, ((CraftPlayer) player).getHandle(),
						((CraftPlayer) player).getHandle().nextContainerCounter());
	}

	static BlockPosition zero = new BlockPosition(0, 0, 0);

	public Object createAnvilContainer(Inventory inv, Player player) {
		ContainerAnvil container = new ContainerAnvil(((CraftPlayer) player).getHandle().nextContainerCounter(),
				((CraftPlayer) player).getHandle().getInventory(),
				ContainerAccess.at(((CraftPlayer) player).getHandle().getWorld(), v1_17_R1.zero));
		for (int i = 0; i < 2; ++i)
			container.a(i, (net.minecraft.world.item.ItemStack) this.asNMSItem(inv.getItem(i)));
		return container;
	}

	@Override
	public Object getSlotItem(Object container, int slot) {
		return ((Container) container).getSlot(slot).getItem();
	}

	@Override
	public String getAnvilRenameText(Object anvil) {
		return ((ContainerAnvil) anvil).v;
	}

	@Override
	public boolean processInvClickPacket(Player player, HolderGUI gui, Object provPacket) {
		PacketPlayInWindowClick packet = (PacketPlayInWindowClick) provPacket;
		int slot = packet.c();
		if (slot == -999)
			return false;

		int id = packet.b();
		int mouseClick = packet.d();
		net.minecraft.world.inventory.InventoryClickType nmsType = packet.g();
		InventoryClickType type = InventoryClickType.values()[nmsType.ordinal()];

		Object container = gui.getContainer(player);
		ItemStack item = this.asBukkitItem(packet.e());
		if ((type == InventoryClickType.QUICK_MOVE || type == InventoryClickType.CLONE
				|| type == InventoryClickType.THROW || item.getType().isAir()) && item.getType().isAir())
			item = this.asBukkitItem(this.getSlotItem(container, slot));
		boolean cancel = false;
		if (InventoryClickType.SWAP == type) {
			item = player.getInventory().getItem(mouseClick);
			mouseClick = 0;
			cancel = true;
		}
		if (item == null)
			item = new ItemStack(Material.AIR);

		ItemStack before = player.getItemOnCursor();
		ClickType clickType = BukkitLoader.buildClick(item, type, slot, mouseClick);
		if (!cancel)
			cancel = BukkitLoader.useItem(player, item, gui, slot, clickType);
		if (!gui.isInsertable())
			cancel = true;

		int gameSlot = slot > gui.size() - 1 ? InventoryUtils.convertToPlayerInvSlot(slot - gui.size()) : slot;
		if (!cancel)
			cancel = gui.onIteractItem(player, item, clickType, gameSlot, slot < gui.size());
		else
			gui.onIteractItem(player, item, clickType, gameSlot, slot < gui.size());
		int position = 0;
		if (!(gui instanceof AnvilGUI) && !cancel && type == InventoryClickType.QUICK_MOVE) {
			ItemStack[] contents = slot < gui.size() ? player.getInventory().getStorageContents()
					: gui.getInventory().getStorageContents();
			List<Integer> modified = slot < gui.size()
					? InventoryUtils.shift(slot, player, gui, clickType,
							gui instanceof AnvilGUI ? DestinationType.PLAYER_INV_ANVIL
									: DestinationType.PLAYER_INV_CUSTOM_INV,
							null, contents, item)
					: InventoryUtils.shift(slot, player, gui, clickType, DestinationType.CUSTOM_INV,
							gui.getNotInterableSlots(player), contents, item);
			if (!modified.isEmpty())
				if (slot < gui.size()) {
					boolean canRemove = !modified.contains(-1);
					player.getInventory().setStorageContents(contents);
					if (canRemove)
						gui.remove(gameSlot);
					else
						gui.getInventory().setItem(gameSlot, item);
				} else {
					boolean canRemove = !modified.contains(-1);
					gui.getInventory().setStorageContents(contents);
					if (canRemove)
						player.getInventory().setItem(gameSlot, null);
					else
						player.getInventory().setItem(gameSlot, item);
				}
			return true;
		}
		if (cancel) {
			// MOUSE
			int statusId = ((Container) container).getStateId();
			BukkitLoader.getPacketHandler().send(player, this.packetSetSlot(-1, -1, statusId, this.asNMSItem(before)));
			switch (type) {
			case CLONE:
				return true;
			case SWAP:
			case QUICK_MOVE:
			case PICKUP_ALL:
				// TOP
				for (ItemStack cItem : gui.getInventory().getContents())
					BukkitLoader.getPacketHandler().send(player,
							this.packetSetSlot(id, position++, statusId, this.asNMSItem(cItem)));
				// BUTTON
				player.updateInventory();
				return true;
			default:
				BukkitLoader.getPacketHandler().send(player,
						this.packetSetSlot(id, slot, statusId, this.getSlotItem(container, slot)));
				if (gui instanceof AnvilGUI) {
					// TOP
					for (ItemStack cItem : gui.getInventory().getContents())
						if (position != slot)
							BukkitLoader.getPacketHandler().send(player,
									this.packetSetSlot(id, position++, statusId, this.asNMSItem(cItem)));
					// BUTTON
					player.updateInventory();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean processServerListPing(String player, Object channel, Object packet) {
		PacketStatusOutServerInfo status = (PacketStatusOutServerInfo) packet;
		ServerPing ping = status.b();
		List<PlayerProfile> players = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers())
			players.add(new PlayerProfile(p.getName(), p.getUniqueId()));
		ServerListPingEvent event = new ServerListPingEvent(Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers(),
				players, Bukkit.getMotd(), ping.d(),
				((InetSocketAddress) ((Channel) channel).remoteAddress()).getAddress(), ping.getServerData().a(),
				ping.getServerData().getProtocolVersion());
		EventManager.call(event);
		if (event.isCancelled())
			return true;
		ServerPingPlayerSample playerSample = new ServerPingPlayerSample(event.getMaxPlayers(),
				event.getOnlinePlayers());
		if (event.getPlayersText() != null) {
			GameProfile[] profiles = new GameProfile[event.getPlayersText().size()];
			int i = -1;
			for (PlayerProfile s : event.getPlayersText())
				profiles[++i] = new GameProfile(s.getUUID(), s.getName());
			playerSample.a(profiles);
		} else
			playerSample.a(new GameProfile[0]);
		ping.setPlayerSample(playerSample);

		if (event.getMotd() != null)
			ping.setMOTD((IChatBaseComponent) this.toIChatBaseComponent(ComponentAPI.fromString(event.getMotd())));
		else
			ping.setMOTD((IChatBaseComponent) BukkitLoader.getNmsProvider().chatBase("{\"text\":\"\"}"));
		if (event.getVersion() != null)
			ping.setServerInfo(new ServerData(event.getVersion(), event.getProtocol()));
		if (event.getFalvicon() != null)
			ping.setFavicon(event.getFalvicon());
		return false;
	}

	@Override
	public Object getNBT(Entity entity) {
		return ((CraftEntity) entity).getHandle().save(new NBTTagCompound());
	}

	@Override
	public Object setString(Object nbt, String path, String value) {
		((NBTTagCompound) nbt).setString(path, value);
		return nbt;
	}

	@Override
	public Object setInteger(Object nbt, String path, int value) {
		((NBTTagCompound) nbt).setInt(path, value);
		return nbt;
	}

	@Override
	public Object setDouble(Object nbt, String path, double value) {
		((NBTTagCompound) nbt).setDouble(path, value);
		return nbt;
	}

	@Override
	public Object setLong(Object nbt, String path, long value) {
		((NBTTagCompound) nbt).setLong(path, value);
		return nbt;
	}

	@Override
	public Object setShort(Object nbt, String path, short value) {
		((NBTTagCompound) nbt).setShort(path, value);
		return nbt;
	}

	@Override
	public Object setFloat(Object nbt, String path, float value) {
		((NBTTagCompound) nbt).setFloat(path, value);
		return nbt;
	}

	@Override
	public Object setBoolean(Object nbt, String path, boolean value) {
		((NBTTagCompound) nbt).setBoolean(path, value);
		return nbt;
	}

	@Override
	public Object setIntArray(Object nbt, String path, int[] value) {
		((NBTTagCompound) nbt).setIntArray(path, value);
		return nbt;
	}

	@Override
	public Object setByteArray(Object nbt, String path, byte[] value) {
		((NBTTagCompound) nbt).setByteArray(path, value);
		return nbt;
	}

	@Override
	public Object setNBTBase(Object nbt, String path, Object value) {
		((NBTTagCompound) nbt).set(path, (NBTBase) value);
		return nbt;
	}

	@Override
	public String getString(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getString(path);
	}

	@Override
	public int getInteger(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getInt(path);
	}

	@Override
	public double getDouble(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getDouble(path);
	}

	@Override
	public long getLong(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getLong(path);
	}

	@Override
	public short getShort(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getShort(path);
	}

	@Override
	public float getFloat(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getFloat(path);
	}

	@Override
	public boolean getBoolean(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getBoolean(path);
	}

	@Override
	public int[] getIntArray(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getIntArray(path);
	}

	@Override
	public byte[] getByteArray(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getByteArray(path);
	}

	@Override
	public Object getNBTBase(Object nbt, String path) {
		return ((NBTTagCompound) nbt).get(path);
	}

	@Override
	public Set<String> getKeys(Object nbt) {
		return ((NBTTagCompound) nbt).getKeys();
	}

	@Override
	public boolean hasKey(Object nbt, String path) {
		return ((NBTTagCompound) nbt).hasKey(path);
	}

	@Override
	public void removeKey(Object nbt, String path) {
		((NBTTagCompound) nbt).remove(path);
	}

	@Override
	public Object setByte(Object nbt, String path, byte value) {
		((NBTTagCompound) nbt).setByte(path, value);
		return nbt;
	}

	@Override
	public byte getByte(Object nbt, String path) {
		return ((NBTTagCompound) nbt).getByte(path);
	}

	@Override
	public Object getDataWatcher(Entity entity) {
		return ((CraftEntity) entity).getHandle().getDataWatcher();
	}

	@Override
	public Object getDataWatcher(Object entity) {
		return ((net.minecraft.world.entity.Entity) entity).getDataWatcher();
	}

	@Override
	public int incrementStateId(Object container) {
		return ((Container) container).incrementStateId();
	}

	@Override
	public Object packetEntityHeadRotation(Entity entity) {
		return new PacketPlayOutEntityHeadRotation((net.minecraft.world.entity.Entity) this.getEntity(entity),
				(byte) (entity.getLocation().getYaw() * 256F / 360F));
	}

	@Override
	public Object packetHeldItemSlot(int slot) {
		return new PacketPlayOutHeldItemSlot(slot);
	}

	@Override
	public Object packetExp(float exp, int total, int toNextLevel) {
		return new PacketPlayOutExperience(exp, total, toNextLevel);
	}

	@Override
	public Object packetPlayerInfo(PlayerInfoType type, Player player) {
		EnumPlayerInfoAction action = null;
		switch (type) {
		case ADD_PLAYER:
			action = EnumPlayerInfoAction.a;
			break;
		case REMOVE_PLAYER:
			action = EnumPlayerInfoAction.e;
			break;
		case UPDATE_DISPLAY_NAME:
			action = EnumPlayerInfoAction.d;
			break;
		case UPDATE_GAME_MODE:
			action = EnumPlayerInfoAction.b;
			break;
		case UPDATE_LATENCY:
			action = EnumPlayerInfoAction.c;
			break;
		}
		return new PacketPlayOutPlayerInfo(action, (EntityPlayer) this.getPlayer(player));
	}

	@Override
	public Object packetPosition(double x, double y, double z, float yaw, float pitch) {
		return new PacketPlayOutPosition(x, y, z, yaw, pitch, Collections.emptySet(), 0, false);
	}

	@Override
	public Object packetRespawn(Player player) {
		EntityPlayer entityPlayer = (EntityPlayer) this.getPlayer(player);
		WorldServer worldserver = entityPlayer.getWorldServer();
		return new PacketPlayOutRespawn(worldserver.getDimensionManager(), worldserver.getDimensionKey(),
				BiomeManager.a(worldserver.getSeed()), entityPlayer.d.getGameMode(), entityPlayer.d.c(),
				worldserver.isDebugWorld(), worldserver.isFlatWorld(), true);
	}

	@Override
	public String getProviderName() {
		return "1_17_R1 (1.17)";
	}

	@Override
	public int getContainerStateId(Object container) {
		return ((Container) container).getStateId();
	}

	@Override
	public void loadParticles() {
		for (Entry<ResourceKey<Particle<?>>, Particle<?>> s : IRegistry.ab.d())
			me.devtec.theapi.bukkit.game.particles.Particle.identifier.put(s.getKey().a().toString(), s.getValue());
	}
}
