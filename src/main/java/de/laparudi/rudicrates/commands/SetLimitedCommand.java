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

public class SetLimitedCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.setlimited")) {
            Language.send(sender, "player.no_permission");
            return true;
        }

        if(args.length != 3) {
            Language.send(sender, "commands.setlimited.syntax");
            return true;
        }

        int amount;
        Crate crate;

        try {
            crate = Crate.getByName(args[0]);
        } catch (final NullPointerException exception) {
            Language.send(sender, "crate.unknown");
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());

        if(!config.contains(args[1])) {
            Language.send(sender, "crate.unknown_id");
            return true;
        }
        
        if(args[2].equalsIgnoreCase("#remove")) {
            config.set(args[2] + ".limited", null);
            try {
                config.save(crate.getFile());
                Language.send(sender, "commands.setlimited.removed", new String[] { "%id%", "%crate%" }, new String[] { args[1], crate.getName() });
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return true;
        }
        
        try {
            amount = Integer.parseInt(args[2]);
        } catch (final NumberFormatException exception) {
            Language.send(sender, "numbers.invalid");
            return true;
        }
        
        if(amount < 0) {
            Language.send(sender, "numbers.at_least_0");
            return true;
        }

        config.set(args[1] + ".limited", amount);
        Language.send(sender, "commands.setlimited.done", new String[] { "%id%", "%crate%", "%amount%" }, new String[] { args[1], crate.getName(), String.valueOf(amount) });

        try {
            config.save(crate.getFile());
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.setlimited")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], CrateUtils.getCrateNames(), complete);
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
