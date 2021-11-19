package de.laparudi.rudicrates.utils.version;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.Blocks;
import net.minecraft.server.v1_14_R1.PacketPlayOutBlockAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_14_R1.scheduler.CraftTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class VersionUtils_1_14 implements VersionUtils {

    private static Field taskPeriod;
    
    static {
        try {
            taskPeriod = CraftTask.class.getDeclaredField("period");
            taskPeriod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTaskPeriod(int id, long period) {
        CompletableFuture.runAsync(() -> {
            CraftScheduler scheduler = (CraftScheduler) Bukkit.getScheduler();
            scheduler.getPendingTasks().forEach(task -> {
                if (task.getTaskId() != id) return;

                try {
                    taskPeriod.setLong(task, period);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public void openAnimation(Player player, Location location) {
        if(location == null) return;
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(position, Blocks.CHEST, 1, 1);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.playSound(location, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 2.0F, 1.0F);
    }

    @Override
    public void closeAnimation(Player player, Location location) {
        if(location == null) return;
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(position, Blocks.CHEST, 2, 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.playSound(location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 2.0F, 1.0F);
    }

    @Override
    public ItemStack getPlayersItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override @Deprecated
    public ComponentBuilder component(String syntax, String command, String description) {
        return new ComponentBuilder("§f" + syntax).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f§l" + command + "\n§6" + description).create()));
    }

    @Override
    public Sound pling() {
        return Sound.BLOCK_NOTE_BLOCK_PLING;
    }

    @Override
    public Sound winSound() {
        return Sound.BLOCK_NOTE_BLOCK_BELL;
    }

    @Override
    public Sound blazeShoot() {
        return Sound.ENTITY_BLAZE_SHOOT;
    }
}