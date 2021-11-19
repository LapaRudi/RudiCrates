package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if(CrateUtils.currentlyOpening.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
