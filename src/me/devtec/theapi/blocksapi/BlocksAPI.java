package me.devtec.theapi.blocksapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.spigotmc.AsyncCatcher;

import me.devtec.theapi.TheAPI;
import me.devtec.theapi.scheduler.Tasker;
import me.devtec.theapi.utils.PercentageList;
import me.devtec.theapi.utils.Position;
import me.devtec.theapi.utils.StringUtils;
import me.devtec.theapi.utils.TheMaterial;
import me.devtec.theapi.utils.reflections.Ref;
import me.devtec.theapi.utils.thapiutils.Validator;

public class BlocksAPI {
	private static interface Blocking {
		public void set(Position pos);
	}
	
	private static void set(Shape form, Position where, int radius, Blocking task) {
		String w = where.getWorld().getName();
		int Xx = where.getBlockX();
		int Yy = where.getBlockY();
		int Zz = where.getBlockZ();
		switch (form) {
		case SQAURE:
			for (int x = Xx - radius; x <= Xx + radius; x++)
				for (int y = Yy - radius; y <= Yy + radius; y++)
					for (int z = Zz - radius; z <= Zz + radius; z++)
						task.set(new Position(w, x, y, z));
			break;
			default: break;
		}
		if(form!=Shape.SQAURE) {
			for(int x = Xx - radius; x <= Xx + radius; x++)
		           for(int y = Yy - radius; y <= Yy + radius; y++)
		               for(int z = Zz - radius; z <= Zz + radius; z++) {
		                   double distance = ((Xx-x) * (Xx-x) + ((Zz-z) * (Zz-z)) + ((Yy-y) * (Yy-y)));
		                   if(distance < radius * radius && !(form==Shape.HOLLOW_SPHERE && distance < ((radius - 1) * (radius - 1))))
		                	   task.set(new Position(w, x, y, z));
		               }
		}
	}

	public static Block getLookingBlock(Player player, int range) {
		org.bukkit.util.BlockIterator iter = new org.bukkit.util.BlockIterator(player, range);
		Block lastBlock = iter.next();
		while (iter.hasNext()) {
			lastBlock = iter.next();
			if (lastBlock.getType() == Material.AIR)
				continue;
			break;
		}
		return lastBlock;
	}

	private static void set(Position from, Position to, Blocking task) {
		BlockIterator g = new BlockIterator(from, to);
		while (g.has())
			task.set(g.get());
	}

	public static enum Shape {
		HOLLOW_SPHERE, SPHERE, SQAURE
	}

	public static Schemate getSchemate(String name) {
		return new Schemate(name);
	}

	public static String getLocationAsString(Location loc) {
		return StringUtils.getLocationAsString(loc);
	}

	public static Location getLocationFromString(String saved) {
		return StringUtils.getLocationFromString(saved);
	}

	public static List<Entity> getNearbyEntities(Location l, int radius) {
		return getNearbyEntities(new Position(l), radius);
	}

	public static List<Entity> getNearbyEntities(Position l, int radius) {
		if (radius > 256) {
			Validator.send("The radius cannot be greater than 256");
			return new ArrayList<>();
		}
		int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
		List<Entity> radiusEntities = new ArrayList<>();
		for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++)
			for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
				Chunk c = new Location(l.getWorld(), l.getX() + (chX * 16), l.getY(), l.getZ() + (chZ * 16)).getChunk();
				if (c != null)
					for (Entity e : c.getEntities())
						if (l.distance(e.getLocation()) <= radius)
							radiusEntities.add(e);
			}
		return radiusEntities;
	}

	public static List<Entity> getNearbyEntities(Entity ed, int radius) {
		return getNearbyEntities(new Position(ed.getLocation()), radius);
	}

	public static List<Entity> getNearbyEntities(World world, double x, double y, double z, int radius) {
		return getNearbyEntities(new Position(world, x, y, z), radius);
	}

	public static BlockSave getBlockSave(Position b) {
		return new BlockSave(b);
	}

	public static BlockIterator get(Location from, Location to) {
		return new BlockIterator(from, to);
	}

	public static BlockIterator get(Position from, Position to) {
		return new BlockIterator(from, to);
	}

	public static float count(Location from, Location to) {
		return count(new Position(from), new Position(to));
	}

	public static float count(Position from, Position to) {
		return new BigDecimal("" + (((from.getBlockX() < to.getBlockX() ? to.getBlockX() : from.getBlockX())
				- (from.getBlockX() > to.getBlockX() ? to.getBlockX() : from.getBlockX())) + 1)).multiply(
						new BigDecimal("" + (((from.getBlockZ() < to.getBlockZ() ? to.getBlockZ() : from.getBlockZ())
								- (from.getBlockZ() > to.getBlockZ() ? to.getBlockZ() : from.getBlockZ())) + 1)))
						.multiply(new BigDecimal(
								"" + (((from.getBlockY() < to.getBlockY() ? to.getBlockY() : from.getBlockY())
										- (from.getBlockY() > to.getBlockY() ? to.getBlockY() : from.getBlockY()))
										+ 1)))
						.floatValue();
	}

	public static List<Position> get(Position from, Position to, TheMaterial ignore) {
		return gt(from, to, Arrays.asList(ignore));
	}

	public static List<Position> get(Position from, Position to, List<TheMaterial> ignore) {
		return gt(from, to, ignore);
	}

	private static List<Position> gt(Position from, Position to, List<TheMaterial> ignore) {
		List<Position> blocks = new ArrayList<>();
		BlockIterator getter = get(from, to);
		while (getter.has()) {
			Position s = getter.get();
			if (ignore == null || !ignore.contains(s.getType()))
				blocks.add(s);
		}
		return blocks;
	}

	public static List<BlockSave> getBlockSaves(List<Position> a) {
		List<BlockSave> b = new ArrayList<>();
		for (Position s : a)
			b.add(getBlockSave(s));
		return b;
	}

	public static void set(Position loc, Material material) {
		set(loc, new TheMaterial(material));
	}

	public static void set(Block loc, Material material) {
		set(new Position(loc), new TheMaterial(material));
	}

	public static void set(Position loc, TheMaterial material) {
		if (!material.getType().isBlock())
			return;
		Object old = loc.getType().getIBlockData();
		loc.setType(material);
		Position.updateBlockAt(loc, old);
		Position.updateLightAt(loc);
	}

	public static void set(Block loc, TheMaterial material) {
		set(new Position(loc), material);
	}

	public static void set(Position loc, List<TheMaterial> material) {
		set(loc, TheAPI.getRandomFromList(material));
	}

	public static void set(Block loc, List<TheMaterial> material) {
		set(loc, TheAPI.getRandomFromList(material));
	}

	public static void set(Position loc, PercentageList<TheMaterial> material) {
		set(loc, material.getRandom());
	}

	public static void set(Block loc, PercentageList<TheMaterial> material) {
		set(new Position(loc), material);
	}

	public static void loadBlockSave(Position pos, BlockSave s) {
		s.load(pos, true);
	}

	public static void pasteBlockSave(Position pos, BlockSave s) {
		s.load(pos, true);
	}

	public static List<Position> get(Shape form, Position where, int radius) {
		return g(form, where, radius, null);
	}

	public static List<Position> get(Shape form, Position where, int radius, TheMaterial ignore) {
		return g(form, where, radius, Arrays.asList(ignore));
	}

	private static List<Position> g(Shape form, Position where, int radius, List<TheMaterial> ignore) {
		List<Position> blocks = new ArrayList<>();
		String w = where.getWorld().getName();
		int Xx = where.getBlockX();
		int Yy = where.getBlockY();
		int Zz = where.getBlockZ();
		switch (form) {
		case SQAURE:
			for (int x = Xx - radius; x <= Xx + radius; x++)
				for (int y = Yy - radius; y <= Yy + radius; y++)
					for (int z = Zz - radius; z <= Zz + radius; z++) {
						Position s = (new Position(w, x, y, z));
	                	   if (ignore == null || !ignore.contains(s.getType()))blocks.add(s);
					}
			break;
			default: break;
		}
		if(form!=Shape.SQAURE) {
			for(int x = Xx - radius; x <= Xx + radius; x++)
		           for(int y = Yy - radius; y <= Yy + radius; y++)
		              for(int z = Zz - radius; z <= Zz + radius; z++) {
		            	  double distance = ((Xx-x) * (Xx-x) + ((Zz-z) * (Zz-z)) + ((Yy-y) * (Yy-y)));
		                  if(distance < radius * radius && !(form==Shape.HOLLOW_SPHERE && distance < ((radius - 1) * (radius - 1)))) {
		                	   Position s = (new Position(w, x, y, z));
		                	   if (ignore == null || !ignore.contains(s.getType()))blocks.add(s);
		                  }
		              }
		}
		return blocks;
	}

	public static List<Position> get(Shape form, Position where, int radius, List<TheMaterial> ignore) {
		return g(form, where, radius, ignore);
	}

	public static void replace(Position from, Position to, TheMaterial block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (pos.getType() == block)
					pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, TheMaterial block, TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, TheMaterial block, List<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void replace(Position from, Position to, TheMaterial block, PercentageList<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with.getRandom());
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, TheMaterial block,
			PercentageList<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block == pos.getType())
					pos.setType(with.getRandom());
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					if (TheAPI.generateChance(block.getChance(block.getRandom())))
						pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, PercentageList<TheMaterial> block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					if (TheAPI.generateChance(block.getChance(block.getRandom())))
						pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) {
					TheMaterial random = block.getRandom();
					if (TheAPI.generateChance(block.getChance(random)))
						pos.setType(with.getRandom());
				}
			}
		});
	}

	public static void replace(Position from, Position to, PercentageList<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType())) {
					TheMaterial random = block.getRandom();
					if (TheAPI.generateChance(block.getChance(random)))
						pos.setType(with.getRandom());
				}
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, List<TheMaterial> block, TheMaterial with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					pos.setType(with);
			}
		});
	}

	public static void replace(Shape form, Position where, int radius, List<TheMaterial> block,
			List<TheMaterial> with) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					pos.setType(TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void replace(Position from, Position to, List<TheMaterial> block, TheMaterial with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					pos.setType(with);
			}
		});
	}

	public static void replace(Position from, Position to, List<TheMaterial> block, List<TheMaterial> with) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (block.contains(pos.getType()))
					pos.setType(TheAPI.getRandomFromList(with));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block, List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, TheMaterial block, TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(block);
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block, List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, List<TheMaterial> block, TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			List<TheMaterial> ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Shape form, Position where, int radius, PercentageList<TheMaterial> block,
			TheMaterial ignore) {
		set(form, where, radius, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, TheMaterial block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(block);
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, List<TheMaterial> block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(TheAPI.getRandomFromList(block));
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block, List<TheMaterial> ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (!ignore.contains(pos.getType()))
					pos.setType(block.getRandom());
			}
		});
	}

	public static void set(Position from, Position to, PercentageList<TheMaterial> block, TheMaterial ignore) {
		set(from, to, new Blocking() {
			@Override
			public void set(Position pos) {
				if (ignore != pos.getType())
					pos.setType(block.getRandom());
			}
		});
	}

	public static boolean isInside(Position loc, Position a, Position b) {
		int xMin = Math.min(a.getBlockX(), b.getBlockX());
		int yMin = Math.min(a.getBlockY(), b.getBlockY());
		int zMin = Math.min(a.getBlockZ(), b.getBlockZ());
		int xMax = Math.max(a.getBlockX(), b.getBlockX());
		int yMax = Math.max(a.getBlockY(), b.getBlockY());
		int zMax = Math.max(a.getBlockZ(), b.getBlockZ());
		return loc.getWorld() == a.getWorld() && loc.getBlockX() >= xMin && loc.getBlockX() <= xMax
				&& loc.getBlockY() >= yMin && loc.getBlockY() <= yMax && loc.getBlockZ() >= zMin
				&& loc.getBlockZ() <= zMax;
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList());
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with,
			TheMaterial ignore) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), Arrays.asList(ignore));
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, TheMaterial with,
			List<TheMaterial> ignore) {
		asynchronizedSet(a, b, onFinish, Arrays.asList(with), ignore);
	}

	private static boolean ww = StringUtils.getInt(TheAPI.getServerVersion().split("_")[1]) >= 14,
			palet = StringUtils.getInt(TheAPI.getServerVersion().split("_")[1]) >= 9;

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with,
			List<TheMaterial> ignore) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (!ignore.contains(before)) {
						Object c = pos.getNMSChunk();
						if (!chunks.containsKey(pos.getChunkKey()))
							chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = TheAPI.getRandomFromList(with).getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF,
									cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, List<TheMaterial> with) {
		asynchronizedSet(a, b, onFinish, with, Arrays.asList());
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with) {
		asynchronizedSet(a, b, onFinish, with, Arrays.asList());
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with,
			List<TheMaterial> ignore) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (!ignore.contains(before)) {
						Object c = pos.getNMSChunk();
						if (!chunks.containsKey(pos.getChunkKey()))
							chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = with.getRandom().getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF,
									cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	public static void asynchronizedSet(Position a, Position b, Runnable onFinish, PercentageList<TheMaterial> with,
			TheMaterial ignore) {
		asynchronizedSet(a, b, onFinish, with, Arrays.asList(ignore));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block,
			TheMaterial with) {
		asynchronizedReplace(a, b, onFinish, block, Arrays.asList(with));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block,
			TheMaterial with) {
		asynchronizedReplace(a, b, onFinish, Arrays.asList(block), Arrays.asList(with));
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block,
			PercentageList<TheMaterial> with) {
		asynchronizedReplace(a, b, onFinish, Arrays.asList(block), with);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block,
			PercentageList<TheMaterial> with) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (block.contains(before)) {
						Object c = pos.getNMSChunk();
						if (!chunks.containsKey(pos.getChunkKey()))
							chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = with.getRandom().getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF,
									cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, TheMaterial block,
			List<TheMaterial> with) {
		asynchronizedReplace(a, b, onFinish, Arrays.asList(block), with);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish, List<TheMaterial> block,
			List<TheMaterial> with) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (block.contains(before)) {
						Object c = pos.getNMSChunk();
						if (!chunks.containsKey(pos.getChunkKey()))
							chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = TheAPI.getRandomFromList(with).getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF,
									cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	private static Constructor<?> aw = Ref.constructor(Ref.nms("ChunkSection"), int.class);
	private static Method a, get = Ref.method(Ref.nms("Chunk"), "getSections"),
			blocks = Ref.method(Ref.nms("ChunkSection"), "getBlocks"), type = Ref.method(Ref.nms("ChunkSection"),
					"setType", int.class, int.class, int.class, Ref.nms("IBlockData"));
	static {
		a = Ref.method(Ref.nms("DataPaletteBlock"), "b", int.class, int.class, int.class, Object.class);
		if (a == null)
			a = Ref.method(Ref.nms("DataPaletteBlock"), "setBlock", int.class, int.class, int.class,
					Ref.nms("IBlockData"));
		if (a == null)
			a = Ref.method(Ref.nms("DataPaletteBlock"), "setBlock", int.class, int.class, int.class, Object.class);
		if (aw == null)
			aw = Ref.constructor(Ref.nms("ChunkSection"), int.class, boolean.class);
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish,
			PercentageList<TheMaterial> block, List<TheMaterial> with) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (block.contains(before)) {
						Object c = pos.getNMSChunk();
						if (!chunks.containsKey(pos.getChunkKey()))
							chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = TheAPI.getRandomFromList(with).getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF,
									cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish,
			PercentageList<TheMaterial> block, PercentageList<TheMaterial> with) {
		try {
			if (AsyncCatcher.enabled)
				AsyncCatcher.enabled = false;
		} catch (Exception | NoSuchFieldError | NoSuchMethodError notEx) {
		}
		new Tasker() {
			public void run() {
				HashMap<Long, Object> chunks = new HashMap<>();
				for(Position pos : get(a, b)) {
					TheMaterial before = pos.getType();
					if (block.contains(before)) {
						Object c = pos.getNMSChunk();
						chunks.put(pos.getChunkKey(), c);
						Object sc = ((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4];
						if (sc == null) {
							if (ww)
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4);
							else
								sc = Ref.newInstance(aw, pos.getBlockY() >> 4 << 4, true);
							((Object[]) Ref.invoke(c, get))[pos.getBlockY() >> 4] = sc;
						}
						Object cr = block.getRandom().getIBlockData();
						if (palet)
							Ref.invoke(Ref.invoke(sc, blocks), BlocksAPI.a, pos.getBlockX() & 0xF,
									pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						else
							Ref.invoke(sc, type, pos.getBlockX() & 0xF, pos.getBlockY() & 0xF, pos.getBlockZ() & 0xF, cr);
						Position.updateBlockAt(pos, before.getIBlockData());
					}
				}
				chunks.clear();
				if (onFinish != null)
					onFinish.run();
			}
		}.runTask();
	}

	public static void asynchronizedReplace(Position a, Position b, Runnable onFinish,
			PercentageList<TheMaterial> block, TheMaterial with) {
		asynchronizedReplace(a, b, onFinish, block, Arrays.asList(with));
	}

}