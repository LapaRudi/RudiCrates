package de.laparudi.rudicrates.utils;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class FileUtils {

    public static boolean playerExists(UUID uuid) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        return playerFile.exists();
    }
    
    public static void addCrates(UUID uuid, Crate crate, int amount) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        final int oldAmount = playerConfig.getInt(crate.getName());
        
        playerConfig.set(crate.getName(), oldAmount + amount);
        save(playerConfig, playerFile);
    }

    public static void removeCrates(UUID uuid, Crate crate, int amount) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        final int oldAmount = playerConfig.getInt(crate.getName());
        int newAmount = oldAmount - amount;
        
        if(newAmount < 0) newAmount = 0;
        playerConfig.set(crate.getName(), newAmount);
        save(playerConfig, playerFile);
    }

    public static int getCrateAmount(UUID uuid, Crate crate) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        return playerConfig.getInt(crate.getName());
    }

    public static void setCrateAmount(UUID uuid, Crate crate, int amount) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerConfig.set(crate.getName(), amount);
        save(playerConfig, playerFile);
    }
    
    public static void resetPlayer(UUID uuid) {
        final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), uuid.toString() + ".yml");
        final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> playerConfig.set(crate.getName(), 0));
        save(playerConfig, playerFile);
    }
    
    private static void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
