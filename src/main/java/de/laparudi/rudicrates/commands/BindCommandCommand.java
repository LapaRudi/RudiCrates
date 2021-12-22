package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
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

public class BindCommandCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.bindcommand")) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }

        if (args.length < 3) {
            RudiCrates.getPlugin().getLanguage().bindCommandHelp.forEach(sender::sendMessage);
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandSyntax);
            return true;
        }

        Crate crate;

        try {
            crate = Crate.getByName(args[0]);
        } catch (NullPointerException e) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownCrate);
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());

        if (!config.contains(args[1])) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownID);
            return true;
        }

        if (args[2].equalsIgnoreCase("#remove")) {
            if (config.getString(args[1] + ".command") == null) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().noBoundCommand);
                return true;
            }

            try {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandRemoved.replace("%command%", "/" + config.getString(args[1] + command))
                        .replace("%id%", args[1]).replace("%crate%", crate.getName()));
                config.set(args[1] + ".command", null);
                config.save(crate.getFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return true;
        }

        final StringBuilder builder = new StringBuilder(args[2]);
        for (int i = 3; i < args.length; i++) {
            builder.append(" ").append(args[i]);
        }

        final String bindCommand = args[2].startsWith("/") ? builder.toString().trim().replaceFirst("/", "") : builder.toString().trim();
        config.set(args[1] + ".command", bindCommand);

        try {
            config.save(crate.getFile());
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandDone.replace("%command%", bindCommand).replace("%id%", args[1]).replace("%crate%", crate.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.bindcommand")) return Collections.emptyList();
        final List<String> completions = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, completions);
            return completions;
        }

        if (args.length == 2) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), completions);
            return completions;
        }

        return Collections.emptyList();
    }
}