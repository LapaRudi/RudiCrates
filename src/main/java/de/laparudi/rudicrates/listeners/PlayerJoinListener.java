package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.data.DataUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) throws IOException {
        final UUID uuid = event.getPlayer().getUniqueId();

        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().createPlayer(uuid);

        } else {
            if(DataUtils.playerConfigCache.containsKey(uuid)) return;
            final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid + ".yml");
            final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            
            DataUtils.playerConfigCache.put(uuid, Pair.of(playerFile, playerConfig));
            if (playerFile.exists()) return;

            if (playerFile.createNewFile()) {
                Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> playerConfig.set(crate.getName(), 0));
                playerConfig.save(playerFile);
            }
        }
    }
}