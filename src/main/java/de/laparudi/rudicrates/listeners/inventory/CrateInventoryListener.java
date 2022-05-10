package de.laparudi.rudicrates.listeners.inventory;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class CrateInventoryListener implements Listener {
    
    @EventHandler
    public void onCrateMenuClick(final InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        if (CrateUtils.currentlyOpening.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        
        final String title = ChatColor.stripColor(event.getView().getTitle());
        final String configValue = Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("inventories.menu_title"));
        
        if (!title.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', configValue)))) return;

        if (event.getRawSlot() >= RudiCrates.getPlugin().getConfig().getInt("inventories.menu_rows") *9) {
            if (event.isShiftClick()) event.setCancelled(true);
            return;
        }

        final ItemStack item = event.getCurrentItem();
        final Player player = (Player) event.getWhoClicked();
        
        event.setCancelled(true);
        if (item == null) return;
        if (item.isSimilar(RudiCrates.getPlugin().getItemManager().close)) player.closeInventory();

        for (final Crate crate : CrateUtils.getCrates()) {
            if (!item.isSimilar(RudiCrates.getPlugin().getItemManager().getCrateItem(player, crate))) {
                continue;
            }
            
            if (event.isLeftClick()) {
                RudiCrates.getPlugin().getCrateUtils().openCrate(player, crate, !event.isShiftClick());

            } else if (event.isRightClick()) {
                if (PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).isEmpty()) {
                    Language.send(player, "crate.empty");
                    if (player.hasPermission("rudicrates.addtocrate")) Language.send(player, "crate.empty_addon");
                   return;
                }

                player.closeInventory();
                player.openInventory(PreviewInventoryUtils.cratePreviewInventoriesMap.get(crate.getName()).get(0));
            }
            break;
        }
    }
    
    @EventHandler
    public void onItemAdd(final InventoryDragEvent event) {
        
        if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("inventories.menu_title"))))) {
            return;
        }
        
        final int size = RudiCrates.getPlugin().getConfig().getInt("inventories.menu_rows") *9;
        if (event.getRawSlots().stream().anyMatch(slot -> slot < size)) event.setCancelled(true);
    }
}
