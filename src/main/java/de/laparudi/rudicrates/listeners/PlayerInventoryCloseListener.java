package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class PlayerInventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = player.getOpenInventory().getTopInventory();
        
        if(CrateUtils.currentlyOpening.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> player.openInventory(inventory), 1);
        }
    }
}
