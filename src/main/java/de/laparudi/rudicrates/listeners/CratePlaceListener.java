package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.items.ItemManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.IOException;
import java.util.List;

public class CratePlaceListener extends ItemManager implements Listener {

    @EventHandler
    public void onCratePlace(final BlockPlaceEvent event) throws IOException {
        if(event.isCancelled()) return;
        if(!event.getItemInHand().isSimilar(crateBlock)) return;
        
        final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> list = locations.getStringList("locations");
        
        list.add(LocationNameUtils.toLocationString(event.getBlockPlaced().getLocation()));
        locations.set("locations", list);
        locations.save(RudiCrates.getPlugin().getLocationsFile());
        event.getPlayer().sendMessage(Messages.PREFIX + "§fCrate Opening §7platziert.");
    }
}
