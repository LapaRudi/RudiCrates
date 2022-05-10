package de.laparudi.rudicrates.utils.version;

import de.laparudi.rudicrates.RudiCrates;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Reflection {
    
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private final @Getter int versionInt = Integer.parseInt(version.substring(3, version.length() -3));
    private static Field taskPeriod;
    private static Object chestBlock;
    
    private static final @Getter Map<UUID, Object> connections = new HashMap<>();
    private static final Map<NMSType, Class<?>> classes = new HashMap<>();
    private static final Map<NMSType, Constructor<?>> constructors = new HashMap<>();
    private static final Map<NMSType, Method> methods = new HashMap<>();
    
    public void loadCacheLegacy() {
        final Class<?> connectionClass = this.getNMSClass("PlayerConnection");
        final Class<?> positionClass = this.getNMSClass("BlockPosition");
        final Class<?> packetClass = this.getNMSClass("PacketPlayOutBlockAction");
        final Class<?> blocksClass = this.getNMSClass("Blocks");
        final Class<?> blockClass = this.getNMSClass("Block");
        
        try {
            if (positionClass != null) {
                classes.put(NMSType.CLASS_BLOCKPOSITION, positionClass);
                constructors.put(NMSType.CONSTRUCTOR_BLOCKPOSITION, positionClass.getConstructor(int.class, int.class, int.class));
            }
            
            if (packetClass != null) {
                classes.put(NMSType.CLASS_PACKET, packetClass);
                constructors.put(NMSType.CONSTRUCTOR_PACKET, packetClass.getConstructor(positionClass, blockClass, int.class, int.class));
            }

            if (connectionClass != null) {
                classes.put(NMSType.CLASS_CONNECTION, connectionClass);
                methods.put(NMSType.METHOD_SENDPACKET, connectionClass.getMethod("sendPacket", this.getNMSClass("Packet")));
            }
            
            classes.put(NMSType.CLASS_BLOCK, blockClass);
            classes.put(NMSType.CLASS_BLOCKS, blocksClass);
            
            if (blocksClass != null) {
                chestBlock = blocksClass.getField("CHEST").get(null);
            }
            
        } catch (final NoSuchMethodException | NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
    
    public void loadCache() {
        final String sendPacket = this.getVersionInt() == 17 ? "sendPacket" : "a";
        final Class<?> connectionClass = this.getMinecraftClass("server.network.PlayerConnection");
        final Class<?> positionClass = this.getMinecraftClass("core.BlockPosition");
        final Class<?> packetClass = this.getMinecraftClass("network.protocol.game.PacketPlayOutBlockAction");
        final Class<?> blocksClass = this.getMinecraftClass("world.level.block.Blocks");
        final Class<?> blockClass = this.getMinecraftClass("world.level.block.Block");

        try {
            if (positionClass != null) {
                classes.put(NMSType.CLASS_BLOCKPOSITION, positionClass);
                constructors.put(NMSType.CONSTRUCTOR_BLOCKPOSITION, positionClass.getConstructor(int.class, int.class, int.class));
            }

            if (packetClass != null) {
                classes.put(NMSType.CLASS_PACKET, packetClass);
                constructors.put(NMSType.CONSTRUCTOR_PACKET, packetClass.getConstructor(positionClass, blockClass, int.class, int.class));
            }

            if (connectionClass != null) {
                classes.put(NMSType.CLASS_CONNECTION, connectionClass);
                methods.put(NMSType.METHOD_SENDPACKET, connectionClass.getMethod(sendPacket, this.getMinecraftClass("network.protocol.Packet")));
            }

            classes.put(NMSType.CLASS_BLOCK, blockClass);
            classes.put(NMSType.CLASS_BLOCKS, blocksClass);

            if (blocksClass != null) {
                chestBlock = blocksClass.getField("bX").get(null); // bX = CHEST (154)
            }

        } catch (final NoSuchMethodException | NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
    
    // https://stackoverflow.com/questions/25837873
    static {
        try {
            final Class<?> taskClass = Class.forName("org.bukkit.craftbukkit." + version + ".scheduler.CraftTask");
            taskPeriod = taskClass.getDeclaredField("period");
            taskPeriod.setAccessible(true);
            
        } catch (final NoSuchFieldException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    public void updateTaskPeriod(int id, long period) {
        Bukkit.getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getTaskId() == id).forEach(task -> {

                try {
                    taskPeriod.setLong(task, period);
                } catch (final IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            });
        });
    }

    private Class<?> getNMSClass(final String name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);

        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private Class<?> getMinecraftClass(final String name) {
        try {
            return Class.forName("net.minecraft." + name);

        } catch (final ClassNotFoundException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private Object getConnection(Player player) {
        final UUID uuid = player.getUniqueId();
        if (connections.containsKey(uuid)) return connections.get(uuid);
        final String connectionField = versionInt >= 17 ? "b" : "playerConnection";
        
        try {
            final Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            final Object connection = nmsPlayer.getClass().getField(connectionField).get(nmsPlayer);
            connections.put(uuid, connection);
            return connection;

        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void sendPacket(final Player player, final int x, final int y, final int z, final boolean opening) {
        final int id = opening ? 1 : 0;
        
        try {
            final Object blockPosition = constructors.get(NMSType.CONSTRUCTOR_BLOCKPOSITION).newInstance(x, y, z);
            final Object packet = constructors.get(NMSType.CONSTRUCTOR_PACKET).newInstance(blockPosition, chestBlock, 1, id);
            final Method sendPacket = methods.get(NMSType.METHOD_SENDPACKET);
            sendPacket.invoke(this.getConnection(player), packet);

        } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
            exception.printStackTrace();
        }
    }
}
