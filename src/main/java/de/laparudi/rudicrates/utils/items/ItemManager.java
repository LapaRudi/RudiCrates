package de.laparudi.rudicrates.utils.items;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    public ItemStack back = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.back")).toItem();
    public ItemStack close = RudiCrates.getPlugin().getVersionUtils().getConfigItem("items.close");
    public ItemStack fill = RudiCrates.getPlugin().getVersionUtils().getConfigItem("items.fill");
    public ItemStack previousPage = new ItemBuilder(XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.previous_page")).toItem();
    public ItemStack nextPage = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.next_page")).toItem();

    public ItemStack currentPage(final int page) {
        return new ItemBuilder(Material.PAPER).setName(Language.withoutPrefix("items.current_page") + " " + page).unique().toItem();
    }

    public final ItemStack blueGlass = new ItemBuilder(XMaterial.BLUE_STAINED_GLASS_PANE.parseItem()).setName(" ").toItem();
    public final ItemStack greenGlass = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).setName(" ").toItem();
    public final ItemStack blackGlass = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()).setName(" ").toItem();

    public final ItemStack error = new ItemBuilder(XMaterial.RED_WOOL.parseItem()).setName("§cError? :/")
            .setLore("§8→ §7Something went wrong here, check your configs.").toItem();

    public final ItemStack getCrateItem(final Player player, final Crate crate) {
        final int keyItemAmount = RudiCrates.getPlugin().getCrateUtils().getKeyItemAmount(player, crate, false);
        return new ItemBuilder(crate.getMaterial()).setName(crate.getDisplayname())
                .setLore(Language.listWithoutPrefix("items.crate_lore", "%amount%",
                 String.valueOf(RudiCrates.getPlugin().getDataUtils().getCrateAmount(player.getUniqueId(), crate) + keyItemAmount))).toItem();
    }

    public ItemStack getCrateKeyItem(final Crate crate, final int amount) {
        return new ItemBuilder(RudiCrates.getPlugin().getVersionUtils().getConfigItem("crates." + crate.getName() + ".key")).setAmount(amount).toItem();
    }

    public ItemStack getCrateBlock(final Material material) {
        return new ItemBuilder(material).setName(Language.withoutPrefix("items.opening_name"))
                .setLore(Language.listWithoutPrefix("items.opening_lore")).unique().toItem();
    }

    public void reloadItems() {
        back = new ItemBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.back")).toItem();
        close = RudiCrates.getPlugin().getVersionUtils().getConfigItem("items.close");
        fill = RudiCrates.getPlugin().getVersionUtils().getConfigItem("items.fill");
        previousPage = new ItemBuilder(XMaterial.ORANGE_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.previous_page")).toItem();
        nextPage = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE.parseItem()).setName(Language.withoutPrefix("items.next_page")).toItem();
    }
    
    public void fillInventory(final Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, blackGlass);
            }
        }
    }
}
