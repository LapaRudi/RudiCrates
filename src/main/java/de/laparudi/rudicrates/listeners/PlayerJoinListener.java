package de.laparudi.rudicrates.listeners;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        final FileConfiguration config = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getConfigFile());
        final UUID uuid = event.getPlayer().getUniqueId();

        if (config.getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().createPlayer(uuid);

        } else {
            final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid + ".yml");
            if (playerFile.exists()) return; // update file

            if (playerFile.createNewFile()) {
                final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                Arrays.stream(CrateUtils.getCrates()).forEach(crate -> playerConfig.set(crate.getName(), 0));

                playerConfig.save(playerFile);
                Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "Datei für " + event.getPlayer().getName() + " wurde erstellt.");
            }
        }
    }
}