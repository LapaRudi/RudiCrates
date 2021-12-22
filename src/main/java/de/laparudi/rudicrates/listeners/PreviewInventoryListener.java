package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class PreviewInventoryListener implements Listener {
    
    @EventHandler
    public void onWinPageChange(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!event.getView().getTitle().contains(RudiCrates.getPlugin().getLanguage().preview)) return;

        final Player player = (Player) event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();
        final ItemStack page = event.getClickedInventory().getItem(49);
        
        if(page == null || page.getItemMeta() == null || !page.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) return;
        final String displayName = page.getItemMeta().getDisplayName().replace(RudiCrates.getPlugin().getLanguage().currentPageItemName + " ", "");
        final int currentPage = Integer.parseInt(displayName) -1;
        
        event.setCancelled(true);
        if (item == null) return;
        
        if (item.isSimilar(RudiCrates.getPlugin().getItemManager().back)) {
            player.closeInventory();
            RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player);
            return;
        }

        for (Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {
            if (!event.getView().getTitle().startsWith(crate.getDisplayname())) continue;
            
            if(item.isSimilar(RudiCrates.getPlugin().getItemManager().previousPage) && currentPage > 0) {
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(currentPage -1));
                
            } else if(item.isSimilar(RudiCrates.getPlugin().getItemManager().nextPage) && currentPage < PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).size() -1) {
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(currentPage +1));
            }
        }
    }
}
