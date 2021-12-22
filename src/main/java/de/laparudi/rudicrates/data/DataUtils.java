package de.laparudi.rudicrates.data;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataUtils {
    
    public static Map<UUID, Pair<File, FileConfiguration>> playerConfigCache = new HashMap<>();

    public void reloadCache() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(playerConfigCache.containsKey(player.getUniqueId())) return;
            final File playerFile = new File(RudiCrates.getPlugin().getPlayerData(), player.getUniqueId() + ".yml");
            final FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            playerConfigCache.put(player.getUniqueId(), Pair.of(playerFile, playerConfig));
        });
    }
    
    public boolean playerExists(UUID uuid) {
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            return RudiCrates.getPlugin().getMySQL().playerExists(uuid);

        } else
            return playerConfigCache.containsKey(uuid);
    }
    
    public void addCrates(UUID uuid, Crate crate, int amount) {
        int oldAmount;
        
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            oldAmount = RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
            RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, oldAmount + amount);
        
        } else {
            final FileConfiguration playerConfig = playerConfigCache.get(uuid).getValue();
            oldAmount = playerConfig.getInt(crate.getName());
            playerConfig.set(crate.getName(), oldAmount + amount);
            this.save(playerConfig, playerConfigCache.get(uuid).getKey());
        }
    }

    public void removeCrates(UUID uuid, Crate crate, int amount) {
        int oldAmount;
        int newAmount;
        
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            oldAmount = RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
            newAmount = oldAmount - amount;
            if (newAmount < 0) return;
            RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, newAmount);
        
        } else {
            final FileConfiguration playerConfig = playerConfigCache.get(uuid).getValue();
            oldAmount = playerConfig.getInt(crate.getName());
            newAmount = oldAmount - amount;

            if(newAmount < 0) newAmount = 0;
            playerConfig.set(crate.getName(), newAmount);
            this.save(playerConfig, playerConfigCache.get(uuid).getKey());
        }
    }

    public int getCrateAmount(UUID uuid, Crate crate) {
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            return RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);

        } else
            return playerConfigCache.get(uuid).getValue().getInt(crate.getName());
    }

    public void setCrateAmount(UUID uuid, Crate crate, int amount) {
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, amount);
        
        } else {
            playerConfigCache.get(uuid).getValue().set(crate.getName(), amount);
            this.save(playerConfigCache.get(uuid).getValue(), playerConfigCache.get(uuid).getKey());
        }
    }
    
    public void resetPlayer(UUID uuid) {
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().resetPlayer(uuid);
        
        } else {
            Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> playerConfigCache.get(uuid).getValue().set(crate.getName(), 0));
            this.save(playerConfigCache.get(uuid).getValue(), playerConfigCache.get(uuid).getKey());
        }
    }
    
    private void save(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
