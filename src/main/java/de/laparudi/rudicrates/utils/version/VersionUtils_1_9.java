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
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class VersionUtils_1_9 implements VersionUtils {
    
    @Override
    public void formatConfig() {
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("crates.wood.material", "LOG");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.fill.material", "STAINED_GLASS_PANE");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.close.material", "STAINED_GLASS_PANE");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("inventories.menu_title", "&0Crate Opening");
        RudiCrates.getPlugin().getFileUtils().setEmptyConfigValue("items.display.name", "&6Your win");
        RudiCrates.getPlugin().saveAndReloadConfig();

        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("inventories.preview", "%crate% &8→ Preview");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.rudicrates.header", "&6%version% &8- &6Commands &c↓");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.rudicrates.footer", "&6%version% &8- &6Commands &c↑");
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("items.crate_lore", Arrays.asList("&8→ &aLeft-click to open", "&8→ &aRight-click to show possible wins", "", "&8→ &aYou have &2%amount% &aleft", "", "&7Use &ashift+left-click &7to skip the animation"));
        RudiCrates.getPlugin().getFileUtils().setEmptyMessage("commands.bindcommand.help", Arrays.asList("&7Placeholders:", "&c%crate% &8→ &fThe opened crate", "&c%player% &8→ &fThe player who opened the crate", "&c%chance% &8→ &fThe win chance of the won item", ""));
        RudiCrates.getPlugin().reloadMessagesConfig();
    }
    
    @Override
    public ItemStack getPlayersItemInHand(final Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override @Deprecated
    public ItemStack getConfigItem(final String path) {
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        final String material = config.getString(path + ".material");
        final String name = config.getString(path + ".name") == null ? config.getString(path + ".displayname") : config.getString(path + ".name");

        final boolean enchanted = config.getBoolean(path + ".enchant");
        final List<String> lore = TranslationUtils.translateChatColor(config.getStringList(path + ".lore"));
        final short durability = (short) config.getInt(path + ".durability");

        if (material == null || name == null) return new ItemBuilder(Material.COBBLESTONE).setName("§7Empty config value? :/")
                .setLore("§8→ §cThe value at '" + path + ".material'", "§8→ §cor '" + path + ".name'", "§8→ §cseems to be empty.").toItem();

        final ItemStack item = XMaterial.matchXMaterial(material).orElse(XMaterial.WHITE_STAINED_GLASS).parseItem();
        return new ItemBuilder(item).setName(ChatColor.translateAlternateColorCodes('&', name))
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
}