package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.items.ItemManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;
import java.util.List;

public class CrateBreakListener extends ItemManager implements Listener {

    @EventHandler
    public void onCrateBreak(final BlockBreakEvent event) throws IOException {
        if(event.getBlock().getType() != Material.CHEST) return;

        final Player player = event.getPlayer();
        final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> list = locations.getStringList("locations");
        final String location = LocationNameUtils.toLocationString(event.getBlock().getLocation());
        
        if(!list.contains(location)) return;
        list.remove(location);
        locations.set("locations", list);
        locations.save(RudiCrates.getPlugin().getLocationsFile());
        
        if(player.getGameMode() != GameMode.CREATIVE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), crateBlock);
        }
        player.sendMessage(Messages.PREFIX + "§fCrate Opening §7abgebaut.");
    }
}
