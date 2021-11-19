package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import de.laparudi.rudicrates.utils.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class PreviewInventoryListener extends ItemManager implements Listener {

    @EventHandler
    public void onWinPageChange(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!event.getView().getTitle().contains("→ Gewinne")) return;

        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        final ItemStack page = event.getClickedInventory().getItem(49);
        
        if(page == null || page.getItemMeta() == null || !page.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) return;
        final String displayName = ChatColor.stripColor(page.getItemMeta().getDisplayName());
        final int currentPage = Integer.parseInt(displayName.replace("Seite ", "")) -1;
        
        event.setCancelled(true);
        if (item == null) return;
        
        if (item.isSimilar(back)) {
            player.closeInventory();
            RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player);
            return;
        }

        for (Crate crate : CrateUtils.getCrates()) {
            if (!event.getView().getTitle().startsWith(crate.getDisplayname())) continue;
            
            if(item.isSimilar(previousPage) && currentPage > 0) {
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(currentPage -1));
                
            } else if(item.isSimilar(nextPage) && currentPage < PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).size() -1) {
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(currentPage +1));
            }
        }
    }
}
