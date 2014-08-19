
import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.EntityEnderDragon;
import net.minecraft.server.PacketPlayOutEntityDestroy;
import net.minecraft.server.PacketPlayOutEntityMetadata;
import net.minecraft.server.PacketPlayOutEntityTeleport;
import net.minecraft.server.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Created by chasechocolate, with classes created by CaptainBern.
 */
public class BarAPI {
	public static class FakeDragon {
		public static final int ENTITY_ID = 696969;
		public static final float MAX_HEALTH = 200.0F;
		EntityEnderDragon ent;
		private Player player;

		private String name = "null";

		private float health = FakeDragon.MAX_HEALTH;

		private boolean invisible = false;
		private boolean exists = false;

		private DataWatcher dataWatcher;

		public FakeDragon(Player player) {
			this.player = player;
			ent = new EntityEnderDragon(
					((CraftWorld) player.getWorld()).getHandle());
			ent.d(ENTITY_ID);
		}

		public Player getPlayer() {
			return player;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;

			spawn();
			update();
		}

		public float getHealth() {
			return health;
		}

		public void setHealth(float health) {
			this.health = health;

			spawn();
			update();
		}

		public boolean isInvisible() {
			return invisible;
		}

		public void setInvisible(boolean invisible) {
			this.invisible = invisible;

			spawn();
			update();
		}

		public boolean doesExist() {
			return exists;
		}

		private static Field getPrivateValue(Object o, String name)
				throws NoSuchFieldException, SecurityException {
			Field f = null;
			try {
				f = o.getClass().getField(name);
			} catch (Exception e) {
				f = o.getClass().getDeclaredField(name);
				f.setAccessible(true);
			}
			return f;
		}

		@SuppressWarnings("deprecation")
		public void spawn() {
			if (!(exists)) {
				updateWatcher();

				PacketPlayOutSpawnEntityLiving mobSpawnPacket = new PacketPlayOutSpawnEntityLiving();
				Location loc = player.getLocation().clone().subtract(0, 200, 0);
				Vector velocity = new Vector(0, 0, 0);

				try {
					getPrivateValue(mobSpawnPacket, "a").setInt(mobSpawnPacket,
							FakeDragon.ENTITY_ID);
					getPrivateValue(mobSpawnPacket, "b").setByte(
							mobSpawnPacket,
							(byte) EntityType.ENDER_DRAGON.getTypeId());
					getPrivateValue(mobSpawnPacket, "c").setInt(mobSpawnPacket,
							(int) Math.floor(loc.getBlockX() * 32.0D));
					getPrivateValue(mobSpawnPacket, "d").setInt(mobSpawnPacket,
							(int) Math.floor(loc.getBlockY() * 32.0D));
					getPrivateValue(mobSpawnPacket, "e").setInt(mobSpawnPacket,
							(int) Math.floor(loc.getBlockZ() * 32.0D));
					getPrivateValue(mobSpawnPacket, "f")
							.setByte(
									mobSpawnPacket,
									(byte) ((int) ((loc.getPitch() * 256.0F) / 360.0F)));
					getPrivateValue(mobSpawnPacket, "g").setByte(
							mobSpawnPacket, (byte) 0);
					getPrivateValue(mobSpawnPacket, "h").setByte(
							mobSpawnPacket,
							(byte) ((int) ((loc.getYaw() * 256.0F) / 360.0F)));

					getPrivateValue(mobSpawnPacket, "i").setByte(
							mobSpawnPacket, (byte) velocity.getX());
					getPrivateValue(mobSpawnPacket, "j").setByte(
							mobSpawnPacket, (byte) velocity.getY());
					getPrivateValue(mobSpawnPacket, "k").setByte(
							mobSpawnPacket, (byte) velocity.getZ());

					getPrivateValue(mobSpawnPacket, "l").set(mobSpawnPacket,
							dataWatcher);
				} catch (Exception e) {
					e.printStackTrace();
				}

				((CraftPlayer) player).getHandle().playerConnection
						.sendPacket(mobSpawnPacket);

				exists = true;
			}
		}

		public void destroy() {
			if (exists) {
				PacketPlayOutEntityDestroy destroyEntityPacket = new PacketPlayOutEntityDestroy(
						new int[] { FakeDragon.ENTITY_ID });
				((CraftPlayer) player).getHandle().playerConnection
						.sendPacket(destroyEntityPacket);
				exists = false;
			}
		}

		public void update() {
			updateWatcher();

			if (exists) {
				// Metadata packet
				PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(
						FakeDragon.ENTITY_ID, dataWatcher, true);
				((CraftPlayer) player).getHandle().playerConnection
						.sendPacket(metadataPacket);

				// Teleport packet
				Location loc = player.getLocation().clone().subtract(0, 200, 0);
				PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(
						FakeDragon.ENTITY_ID,
						(int) Math.floor(loc.getBlockX() * 32.0D),
						(int) Math.floor(loc.getBlockY() * 32.0D),
						(int) Math.floor(loc.getBlockZ() * 32.0D),
						(byte) ((int) ((loc.getYaw() * 256.0F) / 360.0F)),
						(byte) ((int) ((loc.getPitch() * 256.0F) / 360.0F)));

				((CraftPlayer) player).getHandle().playerConnection
						.sendPacket(teleportPacket);
			}
		}

		public void updateWatcher() {
			dataWatcher = new DataWatcher(ent);

			dataWatcher
					.a(0, (invisible ? (Byte) (byte) 0x20 : (Byte) (byte) 0));
			dataWatcher.a(6, (Float) (float) health);
			dataWatcher.a(7, (Integer) (int) 0);
			dataWatcher.a(8, (Byte) (byte) 0);
			dataWatcher.a(10, (String) name);
			dataWatcher.a(11, (Byte) (byte) 1);
		}
	}

	private static HashMap<String, FakeDragon> playerDragons = new HashMap<String, FakeDragon>();

	public static String getMessage(Player player) {
		FakeDragon dragon = getDragon(player);

		return dragon.getName();
	}

	public static void setMessage(Player player, String msg, boolean override) {
		if (hasBar(player)) {
			if (!(override)) {
				return;
			}
		}

		FakeDragon dragon = getDragon(player);

		if (msg.length() > 64) {
			msg = msg.substring(0, 63);
		}

		dragon.setName(msg);
	}

	public static float getHealth(Player player) {
		FakeDragon dragon = getDragon(player);

		return dragon.getHealth();
	}

	public static void setHealth(Player player, float health, boolean override) {
		if (hasBar(player)) {
			if (!(override)) {
				return;
			}
		}

		FakeDragon dragon = getDragon(player);

		dragon.setHealth(health);
	}

	public static void setPercent(Player player, float percent, boolean override) {
		if (hasBar(player)) {
			if (!(override)) {
				return;
			}
		}

		FakeDragon dragon = getDragon(player);
		float health = (percent / 100.0F) * FakeDragon.MAX_HEALTH;

		dragon.setHealth(health);
	}

	public static void displayBar(final JavaPlugin plugin, final Player player,
			String msg, float percent, boolean override) {
		setMessage(player, msg, override);
		setPercent(player, percent, override);
		getDragon(player).update();
	}

	public static void removeBar(Player player) {
		if (hasBar(player)) {
			FakeDragon dragon = getDragon(player);

			dragon.destroy();
			playerDragons.remove(player.getName());
		}
	}

	public static boolean hasBar(Player player) {
		return playerDragons.containsKey(player.getName());
	}

	public static FakeDragon getDragon(Player player) {
		if (hasBar(player)) {
			return playerDragons.get(player.getName());
		} else {
			FakeDragon dragon = new FakeDragon(player);

			dragon.setInvisible(true);
			playerDragons.put(player.getName(), dragon);

			return dragon;
		}
	}
}