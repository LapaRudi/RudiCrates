package de.laparudi.rudicrates.data;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataUtils {
    
    private static final Map<UUID, Bundle<File, FileConfiguration>> playerConfigCache = new HashMap<>();

    public void reloadCache() {
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) return;
        
        Bukkit.getOnlinePlayers().forEach(player -> {
            final UUID uuid = player.getUniqueId();
            if (playerConfigCache.containsKey(uuid)) return;
            playerConfigCache.put(uuid, new Bundle<>(this.getPlayerFile(uuid), this.getPlayerConfig(uuid)));
        });
    }
    
    public boolean playerExists(final UUID uuid) {
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            return RudiCrates.getPlugin().getMySQL().playerExists(uuid);

        } else {
            if (playerConfigCache.containsKey(uuid)) return true;
            return new File(RudiCrates.getPlugin().getPlayerData(), uuid + ".yml").exists();
        }
    }
    
    public void createPlayer(final UUID uuid) {
        if (this.playerExists(uuid)) return;
        
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().createPlayer(uuid);
            return;
        }
        
        final File playerFile = this.getPlayerFile(uuid);
        final FileConfiguration playerConfig = this.getPlayerConfig(uuid);
        playerConfigCache.put(uuid, Bundle.of(this.getPlayerFile(uuid), this.getPlayerConfig(uuid)));
        if (playerFile.exists()) return;

        try {
            if (playerFile.createNewFile()) {
                Arrays.stream(CrateUtils.getCrates()).forEach(crate -> playerConfig.set(crate.getName(), 0));
                playerConfig.save(playerFile);
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public void addCrates(final UUID uuid, final Crate crate, final int amount) {
        if (!this.playerExists(uuid)) this.createPlayer(uuid);
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

    public void removeCrates(final UUID uuid, final Crate crate, final int amount) {
        if (!this.playerExists(uuid)) this.createPlayer(uuid);
        int oldAmount;
        int newAmount;
        
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            oldAmount = RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);
            newAmount = oldAmount - amount;
            if (newAmount < 0) return;
            RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, newAmount);
        
        } else {
            final FileConfiguration playerConfig = playerConfigCache.get(uuid).getValue();
            oldAmount = playerConfig.getInt(crate.getName());
            newAmount = oldAmount - amount;

            if (newAmount < 0) newAmount = 0;
            playerConfig.set(crate.getName(), newAmount);
            this.save(playerConfig, playerConfigCache.get(uuid).getKey());
        }
    }

    public int getCrateAmount(final UUID uuid, final Crate crate) {
        if (!this.playerExists(uuid)) return 0;
        
        if (RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            return RudiCrates.getPlugin().getMySQL().getValue(uuid, crate);

        } else {
            if (playerConfigCache.containsKey(uuid)) return playerConfigCache.get(uuid).getValue().getInt(crate.getName());
            final FileConfiguration playerConfig = this.getPlayerConfig(uuid);
            playerConfigCache.put(uuid, Bundle.of(this.getPlayerFile(uuid), playerConfig));
            return playerConfig.getInt(crate.getName());
        }
    }

    public void setCrateAmount(final UUID uuid, final Crate crate, final int amount) {
        if (!this.playerExists(uuid)) this.createPlayer(uuid);
        
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().setValue(uuid, crate, amount);
        
        } else {
            playerConfigCache.get(uuid).getValue().set(crate.getName(), amount);
            this.save(playerConfigCache.get(uuid).getValue(), playerConfigCache.get(uuid).getKey());
        }
    }
    
    public void resetPlayer(final UUID uuid) {
        if (!this.playerExists(uuid)) return;
        
        if(RudiCrates.getPlugin().getConfig().getBoolean("usemysql")) {
            RudiCrates.getPlugin().getMySQL().resetPlayer(uuid);
        
        } else {
            Arrays.stream(CrateUtils.getCrates()).forEach(crate -> playerConfigCache.get(uuid).getValue().set(crate.getName(), 0));
            this.save(playerConfigCache.get(uuid).getValue(), playerConfigCache.get(uuid).getKey());
        }
    }
    
    public File getPlayerFile(final UUID uuid) {
        if (playerConfigCache.containsKey(uuid)) return playerConfigCache.get(uuid).getKey();
        final File file = new File(RudiCrates.getPlugin().getPlayerData(), uuid + ".yml");
        this.create(file);
        
        playerConfigCache.put(uuid, new Bundle<>(file, YamlConfiguration.loadConfiguration(file)));
        return file;
    }

    public FileConfiguration getPlayerConfig(final UUID uuid) {
        if (playerConfigCache.containsKey(uuid)) return playerConfigCache.get(uuid).getValue();
        return YamlConfiguration.loadConfiguration(this.getPlayerFile(uuid));
    }
    
    private void create(final File file) {
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    Arrays.stream(CrateUtils.getCrates()).forEach(crate -> config.set(crate.getName(), 0));
                    this.save(config, file);
                }
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    
    private void save(final FileConfiguration config, final File file) {
        try {
            config.save(file);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
}
