package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class CrateBreakListener implements Listener {
    
    @EventHandler
    public void onCrateBreak(final BlockBreakEvent event) throws IOException {
        if (event.getBlock().getType() != Material.CHEST) return;

        final Player player = event.getPlayer();
        final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> list = locations.getStringList("locations");
        final String location = LocationNameUtils.toLocationString(event.getBlock().getLocation());

        if (!list.contains(location)) return;
        final ItemStack item = RudiCrates.getPlugin().getVersionUtils().getPlayersItemInHand(player);
        
        if (CrateUtils.keyItems.stream().anyMatch(keyItem -> keyItem.isSimilar(item))) {
            event.setCancelled(true);
            return;
        }
        
        list.remove(location);
        locations.set("locations", list);
        locations.save(RudiCrates.getPlugin().getLocationsFile());

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        if (player.getGameMode() != GameMode.CREATIVE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), RudiCrates.getPlugin().getItemManager().crateBlock);
        }

        Language.send(player, "listeners.crateblock.removed");
    }
}
