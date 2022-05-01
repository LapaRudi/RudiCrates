package de.laparudi.rudicrates.crate;

import com.cryptomorin.xseries.XMaterial;
import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.language.Language;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Crate {
    
    private final @Getter String name, displayname;
    private final @Getter Material material;
    private final @Getter File file;
    private static final @Getter Map<String, Map<ItemStack, Double[]>> map = new HashMap<>();
    private static final @Getter Map<String, FileConfiguration> crateConfigMap = new HashMap<>();
    private final @Getter Map<ItemStack, Double[]> crateMap;
    
    private static final String[] crateValues = new String[] { ".displayname", ".slot", ".material" };
    private static final String[] keyValues = new String[] { ".material", ".name" };

    public Crate(final String name, final String displayname, final Material material) {
        this.name = name;
        this.displayname = displayname;
        this.material = material;
        file = new File(RudiCrates.getPlugin().getCrateFolder(), name + ".yml");
        crateMap = new HashMap<>();
                
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException exception) {
                exception.printStackTrace();
                return;
            }
        }
        
        if (map.containsKey(name)) return;
        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), Crate::reloadCrateMaps, 4);
    }

    private static boolean everyValueGiven(final String crateName) {
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        return Arrays.stream(crateValues).allMatch(value -> config.getKeys(true).contains("crates." + crateName + value));
    }
    
    public static Crate getByName(final String name) {
        if (!everyValueGiven(name)) {
            Language.send(Bukkit.getConsoleSender(), "crate.incomplete", "%crate%", name);
            throw new NullPointerException("Crate '" + name + "' not found.");
        }
        
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        final String materialPath = config.getString("crates." + name + ".material");
        final Material material = materialPath != null && Material.getMaterial(materialPath) != null ? Material.getMaterial(materialPath) : XMaterial.END_PORTAL_FRAME.parseMaterial();
        return new Crate(name, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("crates." + name + ".displayname"))), material);
    }
    
    public static void reloadCrateMaps() {
        for (final Crate crate : CrateUtils.getCrates()) {
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

                } catch (final NumberFormatException exception) {
                    Language.send(Bukkit.getConsoleSender(), "crate.incorrect_chance_value", new String[] { "%id%", "%crate%" }, new String[] { key + crate.getName() });
                }
            }
            map.put(crate.getName(), crate.getCrateMap());
        }
    }
}
