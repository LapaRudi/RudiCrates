package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class CratePlaceListener implements Listener {
    
    @EventHandler
    public void onCratePlace(final BlockPlaceEvent event) throws IOException {
        final ItemStack item = new ItemBuilder(event.getItemInHand().clone()).setAmount(1).toItem();
        
        if(CrateUtils.keyItems.contains(item)) {
            event.setCancelled(true);
            return;
        }
        
        if(!item.isSimilar(RudiCrates.getPlugin().getItemManager().crateBlock)) return;
        final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> list = locations.getStringList("locations");
        
        list.add(LocationNameUtils.toLocationString(event.getBlockPlaced().getLocation()));
        locations.set("locations", list);
        locations.save(RudiCrates.getPlugin().getLocationsFile());
        event.getPlayer().sendMessage(RudiCrates.getPlugin().getLanguage().crateOpeningPlaced);
    }
}
