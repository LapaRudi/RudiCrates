package de.laparudi.rudicrates.crate;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Crate {

    private final String name, displayname;
    private final Material material;
    private final File file;
    private static final Map<String, Map<ItemStack, Double[]>> map = new HashMap<>();
    private final Map<ItemStack, Double[]> crateMap;

    public Crate(String name, String displayname, Material material) {
        this.name = name;
        this.displayname = displayname;
        this.material = material;
        file = new File(RudiCrates.getPlugin().getCrateFolder(), name + ".yml");
        crateMap = new HashMap<>();
                
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        
        if(map.containsKey(name)) return;
        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
            final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(file);
            double count;
            double amount = 0;

            for (String key : crateConfig.getKeys(false)) {
                if (crateConfig.get(key + ".limited") != null && crateConfig.getInt(key + ".limited") < 1) continue;
                final ItemStack item = crateConfig.getItemStack(key + ".item");

                try {
                    count = crateConfig.getDouble(key + ".chance");
                    crateMap.put(item, new Double[] { amount, amount + count, Double.parseDouble(key) });
                    amount = amount + count;

                } catch (NumberFormatException e) {
                    Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§fFehlerhafte Gewinnchance für Item " + key + " aus " + name + " Crate");
                }
            }
            map.put(name, crateMap);
        }, 4);
    }

    public static Crate getByName(String name) {
        if(!RudiCrates.getPlugin().getConfig().contains("crates." + name)) {
            throw new NullPointerException("Crate not found");
        }
        
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        final String materialPath = config.getString("crates." + name + ".material");
        final Material material = materialPath != null && Material.getMaterial(materialPath) != null ? Material.getMaterial(materialPath) : Material.END_PORTAL_FRAME;
        return new Crate(name, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("crates." + name + ".displayname"))), material);
    }
    
    public static void reloadCrateMaps() {
        for(Crate crate : CrateUtils.getCrates()) {
            final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
            double count;
            double amount = 0;

            for (String key : crateConfig.getKeys(false)) {
                if (crateConfig.get(key + ".limited") != null && crateConfig.getInt(key + ".limited") < 1) continue;
                final ItemStack item = crateConfig.getItemStack(key + ".item");

                try {
                    count = crateConfig.getDouble(key + ".chance");
                    crate.getCrateMap().put(item, new Double[] { amount, amount + count, Double.parseDouble(key) });
                    amount = amount + count;

                } catch (NumberFormatException e) {
                    Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§fFehlerhafte Gewinnchance für Item " + key + " aus " + crate.getName() + " Crate");
                }
            }
            map.put(crate.getName(), crate.getCrateMap());
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayname() {
        return displayname;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public File getFile() {
        return file;
    }

    public Map<ItemStack, Double[]> getCrateMap() {
        return crateMap;
    }

    public Map<String, Map<ItemStack, Double[]>> getMap() {
        return map;
    }
}
