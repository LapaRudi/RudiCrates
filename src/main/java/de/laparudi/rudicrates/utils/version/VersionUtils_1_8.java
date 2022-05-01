package de.laparudi.rudicrates.utils.version;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.language.TranslationUtils;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scheduler.CraftScheduler;
import org.bukkit.craftbukkit.v1_8_R3.scheduler.CraftTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VersionUtils_1_8 implements VersionUtils {

    private static Field taskPeriod;
    
    static {
        try {
            taskPeriod = CraftTask.class.getDeclaredField("period");
            taskPeriod.setAccessible(true);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public void updateTaskPeriod(int id, long period) {
        CompletableFuture.runAsync(() -> {
            CraftScheduler scheduler = (CraftScheduler) Bukkit.getScheduler();
            scheduler.getPendingTasks().forEach(task -> {
                if (task.getTaskId() != id) return;

                try {
                    taskPeriod.setLong(task, period);
                } catch (final Exception exception) {
                    exception.printStackTrace();
                }
            });
        });
    }

    @Override
    public void openAnimation(final Player player, final Location location) {
        if (location == null) return;
        final BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(position, Blocks.CHEST, 1, 1);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.playSound(location, Sound.valueOf("ENDERMAN_TELEPORT"), 2.0F, 1.0F);
    }

    @Override
    public void closeAnimation(final Player player, final Location location) {
        if (location == null) return;
        final BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(position, Blocks.CHEST, 1, 0);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        player.playSound(location, Sound.valueOf("ENDERMAN_TELEPORT"), 2.0F, 1.0F);
    }
    
    @Override
    public void formatConfig() {
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("crates.wood.material", "OAK_LOG");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.fill.material", "STAINED_GLASS_PANE");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.close.material", "STAINED_GLASS_PANE");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("inventories.menu_title", "&0Crate Opening");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.display.name", "&6Your win");
        RudiCrates.getPlugin().saveAndReloadConfig();
        
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("inventories.preview", "%crate% &8-> Preview");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.rudicrates.header", "&6%version% &8- &6Commands");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.rudicrates.footer", "&6%version% &8- &6Commands");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("items.crate_lore", Arrays.asList("&8-> &aLeft-click to open", "&8-> &aRight-click to show possible wins", "", "&8-> &aYou have &2%amount% &aleft", "", "&7Use &ashift+left-click &7to skip the animation"));
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.bindcommand.help", Arrays.asList("&7Placeholders:", "&c%crate% &7-> &fThe opened crate", "&c%player% &7-> &fThe player who opened the crate", "&c%chance% &7-> &fThe win chance of the won item", ""));
        RudiCrates.getPlugin().reloadMessagesConfig();
    }

    @Override @Deprecated
    public ItemStack getPlayersItemInHand(final Player player) {
        return player.getItemInHand();
    }

    @Override @Deprecated
    public ItemStack getConfigItem(final String path) {
        final String materialPath = RudiCrates.getPlugin().getConfig().getString(path + ".material");
        final String namePath = RudiCrates.getPlugin().getConfig().getString(path + ".name") == null ?
                RudiCrates.getPlugin().getConfig().getString(path + ".displayname") : RudiCrates.getPlugin().getConfig().getString(path + ".name");
        
        final boolean enchanted = RudiCrates.getPlugin().getConfig().getBoolean(path + ".enchant");
        final List<String> lore = TranslationUtils.translateChatColor(RudiCrates.getPlugin().getConfig().getStringList(path + ".lore"));
        final short durability = (short) RudiCrates.getPlugin().getConfig().getInt(path + ".durability");

        if (materialPath == null || namePath == null) return new ItemBuilder(Material.COBBLESTONE).setName("§7Fehlerhaftes Item :/")
                .setLore("§8→ §cDer Wert bei '" + path + ".material'", "§8→ §coder '" + path + ".name'", "§8→ §cscheint leer zu sein.").toItem();

        final ItemStack item = XMaterial.matchXMaterial(materialPath).orElse(XMaterial.WHITE_STAINED_GLASS).parseItem();
        return new ItemBuilder(item).setName(ChatColor.translateAlternateColorCodes('&', namePath))
                .setDurability(durability).setLore(lore).invisibleEnchant(enchanted).toItem();
    }
    
    @Override @Deprecated
    public BaseComponent[] builder(final String text, final String hoverText, final String displayText) {
        return new ComponentBuilder(Language.getPrefix()).append(text).append(" ")
                .append(hoverText).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(displayText).create())).create();
    }
    
    @Override @Deprecated
    public BaseComponent[] component(final String syntax, final String command, final String description) {
        return new ComponentBuilder("§f" + syntax).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("§f§l" + command + "\n§6" + description).create())).create();
    }

    @Override
    public Sound pling() {
        return Sound.valueOf("NOTE_PLING");
    }

    @Override
    public Sound winSound() {
        return Sound.valueOf("NOTE_SNARE_DRUM");
    }

    @Override
    public Sound blazeShoot() {
        return Sound.valueOf("BLAZE_BREATH");
    }
}