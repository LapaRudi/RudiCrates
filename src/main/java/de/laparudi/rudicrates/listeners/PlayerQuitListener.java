package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.utils.version.Reflection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.UUID;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        Reflection.getConnections().remove(uuid);
        if (!RudiCrates.getPlugin().getDataUtils().playerExists(uuid)) return;
        
        final File playerFile = RudiCrates.getPlugin().getDataUtils().getPlayerFile(uuid);
        final FileConfiguration playerConfig = RudiCrates.getPlugin().getDataUtils().getPlayerConfig(uuid);
        
        for (final String key : playerConfig.getKeys(false)) {
            if (playerConfig.getInt(key) != 0) {
                return;
            }
        }
        
        playerFile.delete();
    }

}
