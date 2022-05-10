package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;

public class CratePlaceListener implements Listener {
    
    @EventHandler
    public void onCratePlace(final BlockPlaceEvent event) {
        final ItemStack item = new ItemBuilder(event.getItemInHand().clone()).setAmount(1).toItem();
        
        if (CrateUtils.keyItems.contains(item)) {
            event.setCancelled(true);
            return;
        }

        if (item.getItemMeta() == null || !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) return;
        final Block block = event.getBlockPlaced();

        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
            if (!RudiCrates.getPlugin().getConfig().getStringList("available_crate_blocks")
                    .contains(block.getWorld().getBlockAt(block.getLocation()).getType().name())) return;

            final FileConfiguration locations = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
            final List<String> list = locations.getStringList("locations");
            final String location = LocationNameUtils.toLocationString(block.getLocation());
            if (list.contains(location)) return;

            list.add(LocationNameUtils.toLocationString(event.getBlockPlaced().getLocation()));
            locations.set("locations", list);

            try {
                locations.save(RudiCrates.getPlugin().getLocationsFile());
                RudiCrates.getPlugin().reloadLocationsConfig();
                Language.send(event.getPlayer(), "listeners.crateblock.placed");
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }, 4);
    }
}
