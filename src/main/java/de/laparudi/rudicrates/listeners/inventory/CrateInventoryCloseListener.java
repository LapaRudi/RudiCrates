package de.laparudi.rudicrates.listeners.inventory;

import com.cryptomorin.xseries.XSound;
import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.UUID;

public class CrateInventoryCloseListener implements Listener {

    @EventHandler
    public void onCrateInventoryClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!CrateUtils.inCrateMenu.containsKey(uuid)) return;

        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
            if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) return;
            final Location location = CrateUtils.inCrateMenu.get(uuid);
            if (location == null) return;
            
            RudiCrates.getPlugin().getReflection().sendPacket(player, location.getBlockX(), location.getBlockY(), location.getBlockZ(), false);
            XSound.play(player, "ENTITY_ILLUSIONER_CAST_SPELL");
            CrateUtils.inCrateMenu.remove(uuid);
        }, 2);
    }
}
