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
import java.io.File;
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
        final List<String> commands = config.getStringList(args[1] + ".commands");
        final StringBuilder builder = new StringBuilder();

        if (!config.contains(args[1])) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownID);
            return true;
        }

        switch (args[2].toLowerCase()) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandSyntax);
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    builder.append(" ").append(args[i]);
                }

                final String add = builder.toString().trim();
                final String addCommand = add.startsWith("/") ? add.replaceFirst("/", "") : add;
                
                if(commands.contains(addCommand)) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandAlreadyAdded);
                    return true;
                }
                
                commands.add(addCommand);
                config.set(args[1] + ".commands", commands);
                this.save(crate.getFile(), config);
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandAdd
                        .replace("%command%", "/" + addCommand).replace("%id%", args[1]).replace("%crate%", crate.getName()));
                break;

            case "remove":
                if (args.length < 4) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandSyntax);
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    builder.append(" ").append(args[i]);
                }

                final String remove = builder.toString().trim();
                final String removeCommand = remove.startsWith("/") ? remove.replaceFirst("/", "") : remove;

                if (!commands.contains(removeCommand)) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandCannotRemove);
                    return true;
                }

                commands.remove(removeCommand);
                config.set(args[1] + ".commands", commands);
                this.save(crate.getFile(), config);
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandRemoved
                        .replace("%command%", "/" + removeCommand).replace("%id%", args[1]).replace("%crate%", crate.getName()));
                break;

            case "info":
                if (args.length != 3) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandSyntax);
                    return true;
                }

                if (commands.isEmpty()) {
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandEmpty);
                    return true;
                }

                sender.sendMessage(RudiCrates.getPlugin().getLanguage().bindCommandInfo.replace("%id%", args[1]).replace("%crate%", crate.getName()));
                commands.forEach(infoCommand -> sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + "§8» §6/" + infoCommand));
                break;
        }

        return true;
    }

    private void save(File file, FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.bindcommand")) return Collections.emptyList();
        final List<String> completions = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        final List<String> bindArgs = Arrays.asList("add", "remove", "info");
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, completions);
            return completions;

        } else if (args.length == 2) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), completions);
            return completions;

        } else if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], bindArgs, completions);
            return completions;

        } else if (args.length == 4) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            
            if (args[2].equalsIgnoreCase("remove")) {
                StringUtil.copyPartialMatches(args[3], config.getStringList(args[1] + ".commands"), completions);
                return completions;
            }
        }
        
        return Collections.emptyList();
    }
}