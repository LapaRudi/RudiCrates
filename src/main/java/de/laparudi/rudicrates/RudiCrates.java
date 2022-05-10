package de.laparudi.rudicrates;

import com.rylinaux.plugman.util.PluginUtil;
import de.laparudi.rudicrates.commands.*;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.data.DataUtils;
import de.laparudi.rudicrates.data.FileUtils;
import de.laparudi.rudicrates.data.MySQL;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.language.TranslationUtils;
import de.laparudi.rudicrates.listeners.*;
import de.laparudi.rudicrates.listeners.inventory.*;
import com.tchristofferson.configupdater.ConfigUpdater;
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

    // TODO /setpreview <crate> <id> change the preview to the current in hand
    
    private MySQL mySQL;
    private Reflection reflection;
    private VersionUtils versionUtils;
    private static @Getter RudiCrates plugin;

    private TranslationUtils translationUtils;
    private Language language;
    private ItemManager itemManager;
    private CrateUtils crateUtils;
    private DataUtils dataUtils;
    private PreviewInventoryUtils inventoryUtils;
    private FileUtils fileUtils;

    private final File configFile = new File(this.getDataFolder(), "config.yml");
    private final File messagesFile = new File(this.getDataFolder(), "messages.yml");
    private final File locationsFile = new File(this.getDataFolder(), "cratelocations.yml");
    private final File playerData = new File(this.getDataFolder(), "playerdata");
    private final File crateFolder = new File(this.getDataFolder(), "crates");
    private FileConfiguration messagesConfig;
    private FileConfiguration locationsConfig;

    private final String version = this.getDescription().getVersion();
    private final String serverVersion = Bukkit.getBukkitVersion();
    private boolean isLegacy = false;

    @Override
    public void onEnable() {
        final long timestamp = System.currentTimeMillis();
        plugin = this;

        this.loadFiles();
        this.initialize();
        this.setup();
        this.connectMySQL();
        this.loadListeners();
        this.loadCommands();
        
        Bukkit.getConsoleSender().sendMessage(Language.getPrefix() + "§2RudiCrates-" + version + " enabled in " + (System.currentTimeMillis() - timestamp) + "ms.");
    }

    @Override
    public void onDisable() {
        if (mySQL != null) mySQL.disconnect();
        this.unloadPlugin();
        Bukkit.getConsoleSender().sendMessage(Language.getPrefix() + "§4RudiCrates-" + version + " disabled.");
    }

    private void initialize() {
        this.reflection = new Reflection();
        this.translationUtils = new TranslationUtils();
        this.language = new Language();
        language.loadMessages();
        
        this.fileUtils = new FileUtils();
        this.setupVersionUtils();
        this.itemManager = new ItemManager();
        this.crateUtils = new CrateUtils();
        CrateUtils.loadCrates();
        
        this.dataUtils = new DataUtils();
        this.inventoryUtils = new PreviewInventoryUtils();
    }

    public void setup() {
        this.reloadMessagesConfig();
        
        if (reflection.getVersionInt() >= 17) {
            reflection.loadCache();
        } else {
            reflection.loadCacheLegacy();
        }
        
        inventoryUtils.setupCrateMenu();
        inventoryUtils.loadPreviewInventories();
        translationUtils.setupTranslations();
        dataUtils.reloadCache();
        crateUtils.loadChancesResult();
        crateUtils.loadCrateBlockTypes();
        fileUtils.deleteUnusedCrateFiles();
    }

    private void loadCommands() {
        Objects.requireNonNull(this.getCommand("rudicrates")).setExecutor(new RudiCratesCommand());
        Objects.requireNonNull(this.getCommand("getcrateblock")).setExecutor(new GetCrateBlockCommand());
        Objects.requireNonNull(this.getCommand("addtocrate")).setExecutor(new AddToCrateCommand());
        Objects.requireNonNull(this.getCommand("removefromcrate")).setExecutor(new RemoveFromCrateCommand());
        Objects.requireNonNull(this.getCommand("editchance")).setExecutor(new EditChanceCommand());
        Objects.requireNonNull(this.getCommand("bindcommand")).setExecutor(new BindCommandCommand());
        Objects.requireNonNull(this.getCommand("setvirtual")).setExecutor(new SetVirtualCommand());
        Objects.requireNonNull(this.getCommand("setlimited")).setExecutor(new SetLimitedCommand());
        Objects.requireNonNull(this.getCommand("key")).setExecutor(new KeyCommand());
    }

    private void loadListeners() {
        final Listener[] listeners = new Listener[] {
                new CrateBreakListener(), new CrateClickListener(), new CratePlaceListener(), new CrateInventoryListener(),
                new PlayerQuitListener(), new PreviewInventoryListener(), new PlayerInventoryCloseListener(),
                new CrateInventoryCloseListener(), new InteractWithKeyListener(), new SelectCrateBlockListener()
        };
        Arrays.stream(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void loadFiles() {
        if (!configFile.exists()) {
            this.saveDefaultConfig();
            ConfigUpdater.update(this, "config.yml", configFile);
            this.reloadConfig();
        }

        if (!locationsFile.exists()) {
            this.saveResource("cratelocations.yml", false);
        }

        if (!this.getConfig().getBoolean("usemysql")) {
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
        this.locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Arrays.stream(CrateUtils.getCrates()).forEach(crate -> {
                if (crate.getFile().exists()) return;

                try {
                    crate.getFile().createNewFile();
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }
            });
        }, 10);
    }

    private void connectMySQL() {
        if (!this.getConfig().getBoolean("usemysql")) return;

        mySQL = new MySQL(this.getConfig().getString("mysql.host"), this.getConfig().getInt("mysql.port"), this.getConfig().getString("mysql.username"), this.getConfig().getString("mysql.password"), this.getConfig().getString("mysql.database"));
        mySQL.create();
        mySQL.connect();
        
        if (mySQL.isConnected()) {
            mySQL.setUpdate("CREATE TABLE IF NOT EXISTS `rudicrates` (`player_uuid` CHAR(36) UNIQUE, " + mySQL.createTableString() + ")");
            mySQL.updateTable();
        }
    }

    private void setupVersionUtils() {
        final int version = reflection.getVersionInt();
        
        if (version >= 16) {
            versionUtils = new VersionUtils_1_16();

        } else if (version >= 13) {
            versionUtils = new VersionUtils_1_13();

        } else if (version >= 9) {
            versionUtils = new VersionUtils_1_9();
            isLegacy = true;

        } else {
            versionUtils = new VersionUtils_1_8();
            isLegacy = true;
        }

        versionUtils.formatConfig();
    }

    public void saveAndReloadConfig() {
        this.saveConfig();
        ConfigUpdater.update(plugin, "config.yml", configFile);
        this.reloadConfig();
    }

    public void reloadMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
            ConfigUpdater.update(this, "messages.yml", messagesFile);
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            language.loadMessages();
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public void reloadLocationsConfig() {
        this.locationsConfig = YamlConfiguration.loadConfiguration(locationsFile);
    }

    public void unloadPlugin() {
        if (Bukkit.getPluginManager().getPlugin("PlugMan") != null) {
            PluginUtil.unload(this);
        } else
            Bukkit.getPluginManager().disablePlugin(this);
    }
}
