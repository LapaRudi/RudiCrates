package de.laparudi.rudicrates.data;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public void setEmptyConfigValue(final String path, final Object value) {
        final String mainPath = path.substring(0, path.lastIndexOf("."));
        if (!RudiCrates.getPlugin().getConfig().getKeys(true).contains(mainPath)) return;
        final String inConfig = RudiCrates.getPlugin().getConfig().getString(path);

        if (inConfig == null || inConfig.equals("<#value#>")) {
            RudiCrates.getPlugin().getConfig().set(path, value);
        }
    }

    public void setEmptyMessage(final String path, final Object message) {
        final String inConfig = RudiCrates.getPlugin().getMessagesConfig().getString(path);
  
        if (inConfig == null || inConfig.equals("<#value#>")) {
            RudiCrates.getPlugin().getMessagesConfig().set(path, message);
        }
    }

    public void removeUnusedConfigValue(final String path) {
        RudiCrates.getPlugin().getConfig().set(path, null);
    }

    public void deleteUnusedCrateFiles() {
        if (!RudiCrates.getPlugin().getConfig().getBoolean("delete_unused_crate_files")) return;

        try (final Stream<Path> pathStream = Files.walk(Paths.get(RudiCrates.getPlugin().getCrateFolder().toURI()))) {
            final List<String> names = new ArrayList<>();
            pathStream.filter(Files::isRegularFile).collect(Collectors.toList()).forEach(path -> names.add(path.getFileName().toString()));

            Arrays.stream(CrateUtils.getCrates()).forEach(crate -> names.remove(crate.getName() + ".yml"));
            names.forEach(name -> {
                if (new File(RudiCrates.getPlugin().getCrateFolder(), name).delete()) {
                    Language.send(Bukkit.getConsoleSender(), "updater.deleted_crate_file", "%file%", name);
                }
            });
            
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
}
