package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
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
        final FileConfiguration locations = RudiCrates.getPlugin().getLocationsConfig();
        final List<String> list = locations.getStringList("locations");
        final String location = LocationNameUtils.toLocationString(event.getBlock().getLocation());
        if (!list.contains(location)) return;
        
        final Player player = event.getPlayer();
        final ItemStack item = RudiCrates.getPlugin().getVersionUtils().getPlayersItemInHand(player);
        
        if (CrateUtils.keyItems.stream().anyMatch(keyItem -> keyItem.isSimilar(item))) {
            event.setCancelled(true);
            return;
        }
        
        list.remove(location);
        locations.set("locations", list);
        locations.save(RudiCrates.getPlugin().getLocationsFile());
        RudiCrates.getPlugin().reloadLocationsConfig();

        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);

        if (player.getGameMode() != GameMode.CREATIVE) {
            final Block block = event.getBlock();
            block.getWorld().dropItemNaturally(block.getLocation(), RudiCrates.getPlugin().getItemManager().getCrateBlock(block.getType()));
        }

        Language.send(player, "listeners.crateblock.removed");
    }
}
