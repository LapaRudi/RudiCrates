package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoveFromCrateCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!(sender.hasPermission("rudicrates.removefromcrate"))) {
            Language.send(sender, "player.no_permission");
            return true;
        }

        if (args.length != 2) {
            Language.send(sender, "commands.removefromcrate.syntax");
            return true;
        }

        Crate crate;

        try {
            crate = Crate.getByName(args[0]);
        } catch (final NullPointerException exception) {
            Language.send(sender, "crate.unknown");
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
        int id;

        try {
            id = Integer.parseInt(args[1]);
        } catch (final NumberFormatException exception) {
            Language.send(sender, "numbers.invalid");
            return true;
        }

        if (!config.getKeys(false).contains(String.valueOf(id))) {
            Language.send(sender, "crate.unknown_id");
            return true;
        }

        config.set(String.valueOf(id), null);

        try {
            config.save(crate.getFile());
            Language.send(sender, "commands.removefromcrate.done", new String[] { "%id%", "%crate%" }, new String[] { String.valueOf(id), crate.getName() });
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.removefromcrate")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        final List<String> crateArgs = CrateUtils.getCrateNames();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, complete);
            return complete;
            
        } else if (args.length == 2) {
            final FileConfiguration config = Crate.getCrateConfigMap().get(args[0]);
            if (config == null) return Collections.emptyList();
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}
