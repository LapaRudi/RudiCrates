package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Objects;
import java.util.UUID;

public class CrateInventoryCloseListener implements Listener {

    @EventHandler
    public void onCrateInventoryClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String view = event.getView().getTitle();
        final String title = ChatColor.translateAlternateColorCodes('&',
                Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("inventorytitle")));
        
        if (!view.equals(title) && !view.contains("→ Gewinne") && !view.contains("wird geöffnet...")) return;
        if (!RudiCrates.getPlugin().getCrateUtils().isCrateOpeningInventory(player) && !CrateUtils.inCrateMenu.containsKey(uuid)) return;
        
        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
            if (player.getOpenInventory().getTopInventory().isEmpty()) {
                RudiCrates.getPlugin().getVersionUtils().closeAnimation(player, CrateUtils.inCrateMenu.get(uuid));
                CrateUtils.inCrateMenu.remove(uuid);
            }
        }, 2);
    }
}
