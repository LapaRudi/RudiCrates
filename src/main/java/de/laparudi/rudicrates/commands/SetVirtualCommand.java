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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetVirtualCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.setvirtual")) {
            Language.send(sender, "player.no_permission");
            return true;
        }
        
        if ((args.length != 3) || (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            Language.send(sender, "commands.setvirtual.syntax");
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
        
        if (!config.contains(args[1])) {
            Language.send(sender, "crate.unknown_id");
            return true;
        }
        
        config.set(args[1] + ".virtual", Boolean.valueOf(args[2]));
        Language.send(sender, "commands.setvirtual.done", new String[] { "%id%", "%crate%", "%value%" }, new String[] { args[1], crate.getName(), args[2].toLowerCase()});
        
        if (Boolean.parseBoolean(args[2]) && config.getStringList(args[1] + ".commands").isEmpty()) {
            Language.send(sender, "commands.setvirtual.warning");
        }
        
        try {
            config.save(crate.getFile());
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.setvirtual")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], CrateUtils.getCrateNames(), complete);
            return complete;

        } else if (args.length == 2) {
            final FileConfiguration config = Crate.getCrateConfigMap().get(args[0]);
            if (config == null) return Collections.emptyList();
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
            
        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
