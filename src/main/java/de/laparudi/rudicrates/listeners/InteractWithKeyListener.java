package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractWithKeyListener implements Listener {

    @EventHandler
    public void onKeyInteract(final PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        final ItemStack item = event.getItem();
        if (block != null && block.getType() == Material.CHEST) return;
        if (item == null) return;
        
        if (CrateUtils.keyItems.stream().anyMatch(keyItem -> keyItem.isSimilar(item))) {
            event.setCancelled(true);
        }
    }
}
