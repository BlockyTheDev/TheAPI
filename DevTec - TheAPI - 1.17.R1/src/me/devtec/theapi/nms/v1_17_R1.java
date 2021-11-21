 package me.devtec.theapi.nms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import me.devtec.theapi.TheAPI;
import me.devtec.theapi.utils.Position;
import me.devtec.theapi.utils.TheMaterial;
import me.devtec.theapi.utils.components.Component;
import me.devtec.theapi.utils.components.ComponentAPI;
import me.devtec.theapi.utils.nms.NmsProvider;
import me.devtec.theapi.utils.nms.datawatcher.DataWatcher;
import me.devtec.theapi.utils.reflections.Ref;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.MojangsonParser;
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
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
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
	private static final sun.misc.Unsafe unsafe = (sun.misc.Unsafe) Ref.getNulled(Ref.field(sun.misc.Unsafe.class, "theUnsafe"));

	@Override
	public Object getEntity(Entity entity) {
		return ((CraftEntity)entity).getHandle();
	}

	@Override
	public Object getEntityLiving(LivingEntity entity) {
		return ((CraftLivingEntity)entity).getHandle();
	}

	@Override
	public Object getPlayer(Player player) {
		return ((CraftPlayer)player).getHandle();
	}

	@Override
	public Object getWorld(World world) {
		return ((CraftWorld)world).getHandle();
	}

	@Override
	public Object getChunk(Chunk chunk) {
		return ((CraftChunk)chunk).getHandle();
	}

	@Override
	public Object getScoreboardAction(Action type) {
		return type==Action.CHANGE?ScoreboardServer.Action.a:ScoreboardServer.Action.b;
	}

	@Override
	public Object getEnumScoreboardHealthDisplay(DisplayType type) {
		return type==DisplayType.INTEGER?EnumScoreboardHealthDisplay.a:EnumScoreboardHealthDisplay.b;
	}

	@Override
	public Object getNBT(ItemStack itemStack) {
		return ((net.minecraft.world.item.ItemStack)asNMSItem(itemStack)).getOrCreateTag();
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
		net.minecraft.world.item.ItemStack i = (net.minecraft.world.item.ItemStack)asNMSItem(stack);
		i.setTag((NBTTagCompound) nbt);
		return asBukkitItem(stack);
	}

	@Override
	public Object asNMSItem(ItemStack stack) {
		return CraftItemStack.asNMSCopy(stack);
	}

	@Override
	public ItemStack asBukkitItem(Object stack) {
		return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) stack);
	}

	@Override
	public Object packetEntityMetadata(int entityId, DataWatcher dataWatcher, boolean bal) {
		return new PacketPlayOutEntityMetadata(entityId, (net.minecraft.network.syncher.DataWatcher) dataWatcher.getDataWatcher(), bal);
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
		return new PacketPlayOutNamedEntitySpawn((EntityHuman)player);
	}

	@Override
	public Object packetSpawnEntityLiving(Object entityLiving) {
		return new PacketPlayOutSpawnEntityLiving((EntityLiving)entityLiving);
	}

	@Override
	public Object packetPlayerListHeaderFooter(String header, String footer) {
		return new PacketPlayOutPlayerListHeaderFooter((IChatBaseComponent)toIChatBaseComponent(ComponentAPI.toComponent(header, true)), (IChatBaseComponent)toIChatBaseComponent(ComponentAPI.toComponent(footer, true)));
	}

	@Override
	public Object packetBlockChange(World world, Position position) {
		return new PacketPlayOutBlockChange((net.minecraft.world.level.World)getWorld(world), (BlockPosition)position.getBlockPosition());
	}

	@Override
	public Object packetBlockChange(World world, int x, int y, int z) {
		return new PacketPlayOutBlockChange((net.minecraft.world.level.World)getWorld(world), new BlockPosition(x,y,z));
	}

	@Override
	public Object packetScoreboardObjective() {
		try {
			return unsafe.allocateInstance(PacketPlayOutScoreboardObjective.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object packetScoreboardDisplayObjective(int id, Object scoreboardObjective) {
		return new PacketPlayOutScoreboardDisplayObjective(id, scoreboardObjective==null?null:(ScoreboardObjective)scoreboardObjective);
	}

	@Override
	public Object packetScoreboardTeam() {
		try {
			return unsafe.allocateInstance(PacketPlayOutScoreboardTeam.class);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Object packetScoreboardScore(Action action, String player, String line, int score) {
		return new PacketPlayOutScoreboardScore((net.minecraft.server.ScoreboardServer.Action) getScoreboardAction(action), player, line, score);
	}

	@Override
	public Object packetTitle(TitleAction action, String text, int fadeIn, int stay, int fadeOut) {
		switch(action) {
		case ACTIONBAR:
			return new ClientboundSetActionBarTextPacket((IChatBaseComponent)ComponentAPI.toIChatBaseComponent(ComponentAPI.toComponent(text, true)));
		case TITLE:
			return new ClientboundSetTitleTextPacket((IChatBaseComponent)ComponentAPI.toIChatBaseComponent(ComponentAPI.toComponent(text, true)));
		case SUBTITLE:
			return new ClientboundSetSubtitleTextPacket((IChatBaseComponent)ComponentAPI.toIChatBaseComponent(ComponentAPI.toComponent(text, true)));
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
		switch(type) {
		case CHAT:
			return new PacketPlayOutChat((IChatBaseComponent)chatBase, ChatMessageType.a, uuid);
		case GAME_INFO:
			return new PacketPlayOutChat((IChatBaseComponent)chatBase, ChatMessageType.c, uuid);
		case SYSTEM:
			return new PacketPlayOutChat((IChatBaseComponent)chatBase, ChatMessageType.b, uuid);
		}
		return null;
	}

	@Override
	public Object packetChat(ChatType type, String text, UUID uuid) {
		return packetChat(type, ComponentAPI.toIChatBaseComponent(ComponentAPI.toComponent(text, false)), uuid);
	}

	@Override
	public void postToMainThread(Runnable runnable) {
		server.executeSync(runnable);
	}

	@Override
	public Object getMinecraftServer() {
		return server;
	}

	@Override
	public Thread getServerThread() {
		return server.an;
	}

	@Override
	public double[] getServerTPS() {
		return server.recentTps;
	}

	@Override
	public Object toIChatBaseComponents(List<Component> components) {
		List<IChatBaseComponent> chat = new ArrayList<>();
		chat.add(new ChatComponentText(""));
		for(Component c : components) {
			if(c.getText()==null||c.getText().isEmpty()) {
				c=c.getExtra();
				continue;
			}
			ChatComponentText current = new ChatComponentText(c.getText());
			chat.add(current);
			ChatModifier modif = current.getChatModifier();
			if(c.getColor()!=null && !c.getColor().isEmpty()) {
				if(c.getColor().startsWith("#"))
					modif=modif.setColor(ChatHexColor.a(c.getColor()));
				else
					modif=modif.setColor(EnumChatFormat.a(c.getColor().charAt(0)));
			}
			if(c.getClickEvent()!=null)
				modif.setChatClickable(new ChatClickable(EnumClickAction.valueOf(c.getClickEvent().getAction().name()), c.getClickEvent().getValue()));
			if(c.getHoverEvent()!=null)
				modif.setChatHoverable(EnumHoverAction.a(c.getHoverEvent().getAction().name()).a((IChatBaseComponent)toIChatBaseComponent(c.getHoverEvent().getValue())));
			modif.setBold(c.isBold());
			modif.setItalic(c.isItalic());
			modif.setRandom(c.isObfuscated());
			modif.setUnderline(c.isUnderlined());
			modif.setStrikethrough(c.isStrikethrough());
			current.setChatModifier(modif);
		}
		return chat.toArray(new IChatBaseComponent[0]);
	}

	@Override
	public Object toIChatBaseComponents(Component c) {
		List<IChatBaseComponent> chat = new ArrayList<>();
		chat.add(new ChatComponentText(""));
		while(c!=null) {
			if(c.getText()==null||c.getText().isEmpty()) {
				c=c.getExtra();
				continue;
			}
			ChatComponentText current = new ChatComponentText(c.getText());
			chat.add(current);
			ChatModifier modif = current.getChatModifier();
			if(c.getColor()!=null && !c.getColor().isEmpty()) {
				if(c.getColor().startsWith("#"))
					modif=modif.setColor(ChatHexColor.a(c.getColor()));
				else
					modif=modif.setColor(EnumChatFormat.a(c.getColor().charAt(0)));
			}
			if(c.getClickEvent()!=null)
				modif.setChatClickable(new ChatClickable(EnumClickAction.valueOf(c.getClickEvent().getAction().name()), c.getClickEvent().getValue()));
			if(c.getHoverEvent()!=null)
				modif.setChatHoverable(EnumHoverAction.a(c.getHoverEvent().getAction().name()).a((IChatBaseComponent)toIChatBaseComponent(c.getHoverEvent().getValue())));
			modif.setBold(c.isBold());
			modif.setItalic(c.isItalic());
			modif.setRandom(c.isObfuscated());
			modif.setUnderline(c.isUnderlined());
			modif.setStrikethrough(c.isStrikethrough());
			current.setChatModifier(modif);
			c=c.getExtra();
		}
		return chat.toArray(new IChatBaseComponent[0]);
	}

	@Override
	public Object toIChatBaseComponent(Component c) {
		ChatComponentText main = new ChatComponentText("");
		while(c!=null) {
			if(c.getText()==null||c.getText().isEmpty()) {
				c=c.getExtra();
				continue;
			}
			ChatComponentText current = new ChatComponentText(c.getText());
			main.addSibling(current);
			ChatModifier modif = current.getChatModifier();
			if(c.getColor()!=null && !c.getColor().isEmpty()) {
				if(c.getColor().startsWith("#"))
					modif=modif.setColor(ChatHexColor.a(c.getColor()));
				else
					modif=modif.setColor(EnumChatFormat.a(c.getColor().charAt(0)));
			}
			if(c.getClickEvent()!=null)
				modif.setChatClickable(new ChatClickable(EnumClickAction.valueOf(c.getClickEvent().getAction().name()), c.getClickEvent().getValue()));
			if(c.getHoverEvent()!=null)
				modif.setChatHoverable(EnumHoverAction.a(c.getHoverEvent().getAction().name()).a((IChatBaseComponent)toIChatBaseComponent(c.getHoverEvent().getValue())));
			modif.setBold(c.isBold());
			modif.setItalic(c.isItalic());
			modif.setRandom(c.isObfuscated());
			modif.setUnderline(c.isUnderlined());
			modif.setStrikethrough(c.isStrikethrough());
			current.setChatModifier(modif);
			c=c.getExtra();
		}
		return main.getSiblings().isEmpty()?ChatComponentText.d:main;
	}

	@Override
	public Object toIChatBaseComponent(List<Component> cc) {
		ChatComponentText main = new ChatComponentText("");
		for(Component c : cc) {
			if(c.getText()==null||c.getText().isEmpty()) {
				c=c.getExtra();
				continue;
			}
			ChatComponentText current = new ChatComponentText(c.getText());
			main.addSibling(current);
			ChatModifier modif = current.getChatModifier();
			if(c.getColor()!=null && !c.getColor().isEmpty()) {
				if(c.getColor().startsWith("#"))
					modif=modif.setColor(ChatHexColor.a(c.getColor()));
				else
					modif=modif.setColor(EnumChatFormat.a(c.getColor().charAt(0)));
			}
			if(c.getClickEvent()!=null)
				modif.setChatClickable(new ChatClickable(EnumClickAction.valueOf(c.getClickEvent().getAction().name()), c.getClickEvent().getValue()));
			if(c.getHoverEvent()!=null)
				modif.setChatHoverable(EnumHoverAction.a(c.getHoverEvent().getAction().name()).a((IChatBaseComponent)toIChatBaseComponent(c.getHoverEvent().getValue())));
			modif.setBold(c.isBold());
			modif.setItalic(c.isItalic());
			modif.setRandom(c.isObfuscated());
			modif.setUnderline(c.isUnderlined());
			modif.setStrikethrough(c.isStrikethrough());
			current.setChatModifier(modif);
		}
		return main.getSiblings().isEmpty()?ChatComponentText.d:main;
	}

	@Override
	public Object chatBase(String json) {
		return IChatBaseComponent.ChatSerializer.a(json);
	}

	@Override
	public String fromIChatBaseComponent(Object component) {
		return CraftChatMessage.fromComponent((IChatBaseComponent)component);
	}

	@Override
	public TheMaterial toMaterial(Object blockOrItemOrIBlockData) {
		if(blockOrItemOrIBlockData instanceof Block) {
			Block b = (Block)blockOrItemOrIBlockData;
			return new TheMaterial((ItemStack)CraftItemStack.asNewCraftStack(b.getItem()));
		}
		if(blockOrItemOrIBlockData instanceof Item) {
			Item b = (Item)blockOrItemOrIBlockData;
			return new TheMaterial((ItemStack)CraftItemStack.asNewCraftStack(b));
		}
		if(blockOrItemOrIBlockData instanceof IBlockData) {
			IBlockData b = (IBlockData)blockOrItemOrIBlockData;
			return new TheMaterial((ItemStack)CraftItemStack.asNewCraftStack(b.getBlock().getItem()));
		}
		return null;
	}

	@Override
	public Object toIBlockData(TheMaterial material) {
		return CraftMagicNumbers.getBlock(material.toItemStack().getData());
	}

	@Override
	public Object toItem(TheMaterial material) {
		return CraftItemStack.asNMSCopy(material.toItemStack()).getItem();
	}

	@Override
	public Object toBlock(TheMaterial material) {
		return CraftMagicNumbers.getBlock(new MaterialData(material.getType(),(byte)material.getData()));
	}

	@Override
	public Object getChunk(World world, int x, int z) {
		return ((CraftChunk)world.getChunkAt(x, z)).getHandle();
	}

	@Override
	public void setBlock(Object chunk, int x, int y, int z, Object IblockData, int data) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk)chunk;
		int yy = c.getSectionIndex(y-1);
		ChunkSection sc = c.getSections()[yy];
		if(sc==null) {
			c.getSections()[yy]=sc=new ChunkSection(y >> 4 << 4);
		}
		BlockPosition pos = new BlockPosition(x,y,z);
		//REMOVE TILE ENTITY
		c.l.remove(pos);
		
		sc.getBlocks().b(x&15, y&15, z&15, (IBlockData)IblockData);
		
		//ADD TILE ENTITY
		if(IblockData instanceof ITileEntity) {
			TileEntity ent = ((ITileEntity)IblockData).createTile(pos, (IBlockData)IblockData);
			c.l.put(pos,ent);
			Ref.sendPacket(TheAPI.getOnlinePlayers(), ent.getUpdatePacket());
		}
	}

	@Override
	public void updateLightAt(Object chunk, int x, int y, int z) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk)chunk;
		c.i.getChunkProvider().getLightEngine().a(new BlockPosition(x,y,z));
	}

	@Override
	public Object getBlock(Object chunk, int x, int y, int z) {
		net.minecraft.world.level.chunk.Chunk c = (net.minecraft.world.level.chunk.Chunk)chunk;
		int yy = c.getSectionIndex(y-1);
		ChunkSection sc = c.getSections()[yy];
		if(sc==null)return Blocks.a.getBlockData();
		return sc.getBlocks().a(x&15, y&15, z&15);
	}

	@Override
	public int getData(Object chunk, int x, int y, int z) {
		return 0;
	}

	@Override
	public int getCombinedId(Object IblockDataOrBlock) {
		return Block.getCombinedId((IBlockData)IblockDataOrBlock);
	}

	@Override
	public Object blockPosition(int blockX, int blockY, int blockZ) {
		return new BlockPosition(blockX, blockY, blockZ);
	}

	@Override
	public Object toIBlockData(Object data) {
		return ((CraftBlockData)data).getState();
	}

	@Override
	public Object toIBlockData(BlockState state) {
		return CraftMagicNumbers.getBlock(state.getType(),state.getRawData());
	}

	@Override
	public Object toBlock(Material type) {
		return CraftMagicNumbers.getBlock(type);
	}

	@Override
	public Object toItem(Material type, int data) {
		return CraftMagicNumbers.getItem(type, (short)data);
	}

	@Override
	public Object toIBlockData(Material type, int data) {
		return CraftMagicNumbers.getBlock(type, (byte)data);
	}

	@Override
	public Chunk toBukkitChunk(Object nmsChunk) {
		return ((net.minecraft.world.level.chunk.Chunk)nmsChunk).bukkitChunk;
	}

	@Override
	public int getPing(Player player) {
		return ((EntityPlayer)getPlayer(player)).e;
	}

	@Override
	public Object getPlayerConnection(Player player) {
		return ((EntityPlayer)getPlayer(player)).b;
	}

	@Override
	public Object getConnectionNetwork(Object playercon) {
		return ((PlayerConnection)playercon).a;
	}

	@Override
	public Object getNetworkChannel(Object network) {
		return ((NetworkManager)network).k;
	}

}
