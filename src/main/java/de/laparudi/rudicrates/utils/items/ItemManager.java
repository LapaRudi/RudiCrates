package de.laparudi.rudicrates.utils.items;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.language.TranslationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemManager {
    
    public final ItemStack back = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName(RudiCrates.getPlugin().getLanguage().backItemName).toItem();
    public final ItemStack closeMenu = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName(RudiCrates.getPlugin().getLanguage().closeMenuItemName).toItem();
    public final ItemStack previousPage = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setName(RudiCrates.getPlugin().getLanguage().previousPageItemName).toItem();
    public final ItemStack nextPage = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName(RudiCrates.getPlugin().getLanguage().nextPageItemName).toItem();
    public final ItemStack currentPage(int page) {
        return new ItemBuilder(Material.PAPER).setName(RudiCrates.getPlugin().getLanguage().currentPageItemName + " " + page).unique().toItem();
    }
    
    public final ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").toItem();
    public final ItemStack blueGlass = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setName(" ").toItem();
    public final ItemStack greenGlass = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName(" ").toItem();
    
    private ItemStack getCrateBase(Material material, String name, int amount) {
        return new ItemBuilder(material).setName(name).setLore(RudiCrates.getPlugin().getLanguage().replaceFromList(RudiCrates.getPlugin().getLanguage().itemCrateLore, "%amount%", String.valueOf(amount))).toItem();
    }
    
    public final ItemStack getCrateItem(Player player, Crate crate) {
        final int keyItemAmount = RudiCrates.getPlugin().getCrateUtils().getKeyItemAmount(player, crate);
        return getCrateBase(crate.getMaterial(), crate.getDisplayname(), RudiCrates.getPlugin().getDataUtils().getCrateAmount(player.getUniqueId(), crate) + keyItemAmount);
    }
    
    public final ItemStack getCrateKeyItem(Crate crate, int amount) {
        final String crateName = crate.getName();
        final ConfigurationSection section = RudiCrates.getPlugin().getConfig().getConfigurationSection("crates." + crateName + ".key");
        if(section == null) return null;
        
        return new ItemBuilder(Material.getMaterial(Objects.requireNonNull(section.getString("material")).toUpperCase()))
                .setName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(section.getString("name"))))
                .setLore(TranslationUtils.translateChatColor(section.getStringList("lore"))).setAmount(amount).invisibleEnchant(section.getBoolean("enchant")).toItem();
    }
    
    public final ItemStack crateBlock = new ItemBuilder(Material.CHEST).setName(RudiCrates.getPlugin().getLanguage().blockCrateName)
            .setLore(TranslationUtils.translateChatColor(RudiCrates.getPlugin().getLanguage().blockCrateLore)).toItem();
}
