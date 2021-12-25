package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CrateInventoryListener implements Listener {
    
    @EventHandler
    public void onCrateMenuClick(final InventoryClickEvent event) {
        if(CrateUtils.currentlyOpening.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("inventorytitle"))))) return;
        
        if(event.getRawSlot() > RudiCrates.getPlugin().getConfig().getInt("inventoryrows") *9) return;
        
        final ItemStack item = event.getCurrentItem();
        final Player player = (Player) event.getWhoClicked();
        
        event.setCancelled(true);
        if (item == null) return;
        if(item.isSimilar(RudiCrates.getPlugin().getItemManager().closeMenu)) player.closeInventory();

        for (Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {
            if (!item.isSimilar(RudiCrates.getPlugin().getItemManager().getCrateItem(player, crate))) {
                continue;
            }
            
            if (event.isLeftClick()) {
                RudiCrates.getPlugin().getCrateUtils().openCrate(player, crate, !event.isShiftClick());

            } else if (event.isRightClick()) {
                if(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).isEmpty()) {
                   player.sendMessage(RudiCrates.getPlugin().getLanguage().noWinsInCrate);
                   return;
                }

                player.closeInventory();
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(0));
            }
            break;
        }
    }
}
