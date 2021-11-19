package de.laparudi.rudicrates.utils.items;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.mysql.SQLUtils;
import de.laparudi.rudicrates.utils.FileUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    public final ItemStack back = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("§cZurück").toItem();
    public final ItemStack closeMenu = new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setName("§cMenü schliessen").toItem();
    public final ItemStack previousPage = new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE).setName("§cVorherige Seite").toItem();
    public final ItemStack currentPage(int page) { return new ItemBuilder(Material.PAPER).setName("§6Seite " + page).unique().toItem(); }
    public final ItemStack nextPage = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName("§2Nächste Seite").toItem();
    
    public final ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").toItem();
    public final ItemStack blueGlass = new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setName(" ").toItem();
    public final ItemStack greenGlass = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setName(" ").toItem();
    
    private ItemStack getCrateBase(Material material, String name, int amount) {
        return new ItemBuilder(material).setName(name).setLore("§8» §aLinksklick zum öffnen", "§8» §aRechtsklick um mögliche Gewinne anzuzeigen", "",
                "§8» §aDu hast noch §2" + amount, "", "§7Benutze §aShift+Linksklick§7, um die Animation zu überspringen.").toItem();
    }
    
    public final ItemStack getCrateItem(Player player, Crate crate) {
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            return getCrateBase(crate.getMaterial(), crate.getDisplayname(), SQLUtils.getCrateAmount(player.getUniqueId(), crate));
            
        } else
            return getCrateBase(crate.getMaterial(), crate.getDisplayname(), FileUtils.getCrateAmount(player.getUniqueId(), crate));
    }
    
    public final ItemStack crateBlock = new ItemBuilder(Material.CHEST).setName("§4§lC§c§lr§6§la§e§lte §6§lO§c§lp§4§le§c§ln§6§li§e§lng")
                .setLore("§7» §2Platziert ein Crate-Opening", "§7» §2Kann wieder abgebaut werden.").toItem();
}
