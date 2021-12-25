package de.laparudi.rudicrates;

import com.rylinaux.plugman.util.PluginUtil;
import de.laparudi.rudicrates.commands.*;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.data.DataUtils;
import de.laparudi.rudicrates.data.MySQL;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.language.TranslationUtils;
import de.laparudi.rudicrates.listeners.*;
import de.laparudi.rudicrates.utils.PreviewInventoryUtils;
import de.laparudi.rudicrates.utils.items.ItemManager;
import de.laparudi.rudicrates.utils.version.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class RudiCrates extends JavaPlugin {

    private MySQL mySQL;
    private VersionUtils versionUtils;
    private static @Getter RudiCrates plugin;

    private TranslationUtils translationUtils;
    private Language language;
    private ItemManager itemManager;
    private CrateUtils crateUtils;
    private DataUtils dataUtils;
    private PreviewInventoryUtils inventoryUtils;

    private final File configFile = new File(this.getDataFolder(), "config.yml");
    private final File messagesFile = new File(this.getDataFolder(), "messages.yml");
    private final File locationsFile = new File(this.getDataFolder(), "cratelocations.yml");
    private final File playerData = new File(this.getDataFolder(), "Playerdata");
    private final File crateFolder = new File(this.getDataFolder(), "Crates");
    private FileConfiguration config;
    private FileConfiguration messagesConfig;

    private final String version = this.getDescription().getVersion();
    private final String serverVersion = Bukkit.getBukkitVersion();

    @Override
    public void onEnable() {
        plugin = this;

        this.loadFiles();
        this.initialize();
        this.connectMySQL();
        this.setupVersionUtils();
        this.loadListeners();
        this.loadCommands();

        inventoryUtils.setupCrateMenu();
        inventoryUtils.loadPreviewInventories();
        translationUtils.setupTranslations();
        dataUtils.reloadCache();
        crateUtils.setupKeyItemList();
        crateUtils.loadChancesResult();
        
        Bukkit.getConsoleSender().sendMessage(language.prefix + "§2RudiCrates-" + version + " enabled.");
    }

    @Override
    public void onDisable() {
        if (mySQL != null) mySQL.disconnect();
        unloadPlugin();
        Bukkit.getConsoleSender().sendMessage(language.prefix + "§4RudiCrates-" + version + " disabled.");
    }

    private void initialize() {
        this.translationUtils = new TranslationUtils();
        this.language = new Language();
        this.itemManager = new ItemManager();
        this.dataUtils = new DataUtils();
        this.inventoryUtils = new PreviewInventoryUtils();
    }

    private void loadCommands() {
        Objects.requireNonNull(this.getCommand("rudicrates")).setExecutor(new RudiCratesCommand());
        Objects.requireNonNull(this.getCommand("getcaseblock")).setExecutor(new GetCrateBlockCommand());
        Objects.requireNonNull(this.getCommand("addtocrate")).setExecutor(new AddToCrateCommand());
        Objects.requireNonNull(this.getCommand("removefromcrate")).setExecutor(new RemoveFromCrateCommand());
        Objects.requireNonNull(this.getCommand("editchance")).setExecutor(new EditChanceCommand());
        Objects.requireNonNull(this.getCommand("bindcommand")).setExecutor(new BindCommandCommand());
        Objects.requireNonNull(this.getCommand("setvirtual")).setExecutor(new SetVirtualCommand());
        Objects.requireNonNull(this.getCommand("setlimited")).setExecutor(new SetLimitedCommand());
        Objects.requireNonNull(this.getCommand("key")).setExecutor(new KeyCommand());
    }

    private void loadListeners() {
        final Listener[] listeners = new Listener[] { new CrateBreakListener(), new CrateClickListener(),
                new CratePlaceListener(), new CrateInventoryListener(), new PlayerJoinListener(),
                new PreviewInventoryListener(), new PlayerInventoryCloseListener(),
                new CrateInventoryCloseListener(), new PlayerInteractListener() };
        
        Arrays.stream(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void loadFiles() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", false);
        }
        
        this.config = YamlConfiguration.loadConfiguration(configFile);

        if (!locationsFile.exists()) {
            this.saveResource("cratelocations.yml", false);
        }

        if (!YamlConfiguration.loadConfiguration(configFile).getBoolean("usemysql")) {
            if (!playerData.exists()) {
                playerData.mkdir();
            }
        }

        if (!crateFolder.exists()) {
            crateFolder.mkdir();
        }

        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }

        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        this.crateUtils = new CrateUtils();
        Arrays.stream(crateUtils.getCrates()).forEach(crate -> {
            if (crate.getFile().exists()) return;

            try {
                crate.getFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectMySQL() {
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
        if (serverVersion.contains("1.8")) {
            versionUtils = new VersionUtils_1_8();

        } else if (serverVersion.contains("1.12")) {
            versionUtils = new VersionUtils_1_12();

        } else if (serverVersion.contains("1.14")) {
            versionUtils = new VersionUtils_1_14();

        } else if (serverVersion.contains("1.16")) {
            versionUtils = new VersionUtils_1_16();

        } else {
            Bukkit.getConsoleSender().sendMessage(language.prefix + "§cYour version (" + serverVersion + ") is not supported by RudiCrates.");
            Bukkit.getConsoleSender().sendMessage(language.prefix + "§eThe following versions are currently supported: §21.8.x, 1.12.x, 1.14.x, 1.16.x");
            unloadPlugin();
        }
    }
    
    public void reloadLanguage() {
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.language = new Language();
    }

    public void unloadPlugin() {
        if (Bukkit.getPluginManager().getPlugin("PlugMan") != null) {
            PluginUtil.unload(this);
        } else Bukkit.getPluginManager().disablePlugin(this);
    }
}
