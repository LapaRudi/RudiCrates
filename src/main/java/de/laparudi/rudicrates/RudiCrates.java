package de.laparudi.rudicrates;

import com.rylinaux.plugman.util.PluginUtil;
import de.laparudi.rudicrates.commands.*;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.listeners.*;
import de.laparudi.rudicrates.mysql.MySQL;
import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import de.laparudi.rudicrates.utils.TranslationUtils;
import de.laparudi.rudicrates.utils.version.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public final class RudiCrates extends JavaPlugin {

    private MySQL mySQL;
    private static RudiCrates plugin;
    private VersionUtils versionUtils;
    private final CrateUtils crateUtils = new CrateUtils();
    private final PreviewInventoryUtils inventoryUtils = new PreviewInventoryUtils();
    private final TranslationUtils translationUtils = new TranslationUtils();
    
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    private final File locationsFile = new File(this.getDataFolder(), "cratelocations.yml");
    private final File playerData = new File(this.getDataFolder(), "Spielerdaten");
    private final File crateFolder = new File(this.getDataFolder(), "Crates");
    
    private final String version = this.getDescription().getVersion();
    private final String serverVersion = Bukkit.getBukkitVersion();
    
    @Override
    public void onEnable() {
        plugin = this;

        this.connectMySQL();
        this.loadFiles();
        this.setupVersionUtils();
        this.loadListeners();
        this.loadCommands();
        
        inventoryUtils.setupCrateMenu();
        inventoryUtils.loadPreviewInventories();
        translationUtils.setupTranslations();
        Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§2RudiCrates-" + version + " wurde gestartet.");
    }

    @Override
    public void onDisable() {
        if(mySQL != null) mySQL.disconnect();
        unloadPlugin();
        Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§4RudiCrates-" + version + " wurde beendet.");
    }
    
    private void loadCommands() {
        Objects.requireNonNull(this.getCommand("rudicrates")).setExecutor(new RudiCratesCommand());
        Objects.requireNonNull(this.getCommand("getcaseblock")).setExecutor(new GetCaseBlockCommand());
        Objects.requireNonNull(this.getCommand("addtocrate")).setExecutor(new AddToCrateCommand());
        Objects.requireNonNull(this.getCommand("removefromcrate")).setExecutor(new RemoveFromCrateCommand());
        Objects.requireNonNull(this.getCommand("editchance")).setExecutor(new EditChanceCommand());
        Objects.requireNonNull(this.getCommand("bindcommand")).setExecutor(new BindCommandCommand());
        Objects.requireNonNull(this.getCommand("setvirtual")).setExecutor(new SetVirtualCommand());
        Objects.requireNonNull(this.getCommand("setlimited")).setExecutor(new SetLimitedCommand());
        
        if(YamlConfiguration.loadConfiguration(configFile).getBoolean("usemysql")) {
            Objects.requireNonNull(this.getCommand("key")).setExecutor(new SQLKeyCommand());
        } else
            Objects.requireNonNull(this.getCommand("key")).setExecutor(new FileKeyCommand());
    }
    
    private void loadListeners() {
        final PluginManager manager = Bukkit.getPluginManager();
        final Listener[] listeners = new Listener[] {
                new CrateBreakListener(), new CrateClickListener(), new CratePlaceListener(),
                new CrateInventoryListener(), new PlayerJoinListener(), new PreviewInventoryListener(),
                new PlayerInventoryCloseListener(), new CrateInventoryCloseListener(), new PlayerInteractListener()
        };
        Arrays.stream(listeners).forEach(listener -> manager.registerEvents(listener, this));
    }
    
    private void loadFiles() {
        if(!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
        
        if(!locationsFile.exists()) {
            this.saveResource("cratelocations.yml", false);
        }

        if(!YamlConfiguration.loadConfiguration(configFile).getBoolean("usemysql")) {
            if(!playerData.exists()) {
                playerData.mkdir();
            }
        }
        
        if(!crateFolder.exists()) {
            crateFolder.mkdir();
        }

        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> {
            if(!crate.getFile().exists()) {
                try {
                    crate.getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void connectMySQL() {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.getBoolean("usemysql")) return;

        mySQL = new MySQL(config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.username"), config.getString("mysql.password"), config.getString("mysql.database"));
        mySQL.create();
        mySQL.connect();

        if (mySQL.isConnected()) {
            mySQL.setUpdate("CREATE TABLE IF NOT EXISTS `rudicrates` (`player_uuid` CHAR(36) UNIQUE, " + mySQL.createTableString() + ")");
            mySQL.updateTable();
        }
    }
    
    private void setupVersionUtils() {
        if(serverVersion.contains("1.8")) {
            versionUtils = new VersionUtils_1_8();
            
        } else if(serverVersion.contains("1.12")) {
            versionUtils = new VersionUtils_1_12();
            
        } else if(serverVersion.contains("1.14")) {
            versionUtils = new VersionUtils_1_14();
            
        } else if(serverVersion.contains("1.16")) {
            versionUtils = new VersionUtils_1_16();
            
        } else {
            Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§cDie Serverversion (" + serverVersion + ") wird nicht von RudiCrates unterstützt.");
            Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§eFolgende Versionen werden unterstützt: §21.8.x, 1.12.x, 1.14.x, 1.16,x");
            unloadPlugin();
        }
    }

    public void unloadPlugin() {
        if(Bukkit.getPluginManager().getPlugin("PlugMan") != null) {
            PluginUtil.unload(this);
        } else
            Bukkit.getPluginManager().disablePlugin(this);
    }
    
    public static RudiCrates getPlugin() {
        return plugin;
    }
    
    public MySQL getMySQL() {
        return mySQL;
    }
    
    public CrateUtils getCrateUtils() {
        return crateUtils;
    }
    
    public PreviewInventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }
    
    public File getConfigFile() {
        return configFile;
    }
    
    public File getLocationsFile() {
        return locationsFile;
    }
    
    public File getPlayerData() {
        return playerData;
    }
    
    public File getCrateFolder() {
        return crateFolder;
    }
    
    public VersionUtils getVersionUtils() {
        return versionUtils;
    }

    public TranslationUtils getTranslationUtils() {
        return translationUtils;
    }
}
