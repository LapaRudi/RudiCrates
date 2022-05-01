package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CrateClickListener implements Listener {

    @EventHandler
    public void onCrateClick(final PlayerInteractEvent event) {
        if (CrateUtils.currentlyOpening.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST) return;

        final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> list = locations.getStringList("locations");
        final Location location = event.getClickedBlock().getLocation();
        final Player player = event.getPlayer();
        final ItemStack item = RudiCrates.getPlugin().getVersionUtils().getPlayersItemInHand(player);

        if (player.isSneaking() && item != null && item.getType() != Material.AIR) return;
        if (!list.contains(LocationNameUtils.toLocationString(location))) return;
        event.setCancelled(true);
        if (CrateUtils.inCrateMenu.containsKey(player.getUniqueId())) return;

        RudiCrates.getPlugin().getVersionUtils().openAnimation(player, location);
        CrateUtils.inCrateMenu.put(player.getUniqueId(), location);
        
        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () ->
                RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player), 10);
    }
}
