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
            Language.send(sender, "player.no_permission");
            return true;
        }

        if (args.length < 3) {
            Language.getList("commands.bindcommand.help").forEach(sender::sendMessage);
            Language.send(sender, "commands.bindcommand.syntax");
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
        final List<String> commands = config.getStringList(args[1] + ".commands");
        final StringBuilder builder = new StringBuilder();

        if (!config.contains(args[1])) {
            Language.send(sender, "crate.unknown_id");
            return true;
        }

        switch (args[2].toLowerCase()) {
            case "add":
                if (args.length < 4) {
                    Language.send(sender, "commands.bindcommand.syntax");
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    builder.append(" ").append(args[i]);
                }

                final String add = builder.toString().trim();
                final String addCommand = add.startsWith("/") ? add.replaceFirst("/", "") : add;

                if (commands.contains(addCommand)) {
                    Language.send(sender, "commands.bindcommand.already_added");
                    return true;
                }

                commands.add(addCommand);
                config.set(args[1] + ".commands", commands);
                this.save(crate.getFile(), config);
                Language.send(sender, "commands.bindcommand.add", new String[]{"%command%", "%id%", "%crate%"}, new String[]{"/" + addCommand, args[1], crate.getName()});
                break;

            case "remove":
                if (args.length < 4) {
                    Language.send(sender, "commands.bindcommand.syntax");
                    return true;
                }

                for (int i = 3; i < args.length; i++) {
                    builder.append(" ").append(args[i]);
                }

                final String remove = builder.toString().trim();
                final String removeCommand = remove.startsWith("/") ? remove.replaceFirst("/", "") : remove;

                if (!commands.contains(removeCommand)) {
                    Language.send(sender, "commands.bindcommand.cannot_remove");
                    return true;
                }

                commands.remove(removeCommand);
                config.set(args[1] + ".commands", commands);
                this.save(crate.getFile(), config);
                Language.send(sender, "commands.bindcommand.removed", new String[]{"%command%", "%id%", "%crate%"}, new String[]{"/" + removeCommand, args[1], crate.getName()});
                break;

            case "info":
                if (args.length != 3) {
                    Language.send(sender, "commands.bindcommand.syntax");
                    return true;
                }

                if (commands.isEmpty()) {
                    Language.send(sender, "commands.bindcommand.empty");
                    return true;
                }

                Language.send(sender, "commands.bindcommand.info", new String[]{"%id%", "%crate%"}, new String[]{args[1], crate.getName()});
                commands.forEach(infoCommand -> sender.sendMessage(Language.getPrefix() + "§8» §6/" + infoCommand));
                break;
        }

        return true;
    }

    private void save(final File file, final FileConfiguration config) {
        try {
            config.save(file);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.bindcommand")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        final List<String> bindArgs = Arrays.asList("add", "remove", "info");
        final FileConfiguration config = Crate.getCrateConfigCache().get(args[0]);

        switch (args.length) {
            case 1:
                StringUtil.copyPartialMatches(args[0], CrateUtils.getCrateNames(), complete);
                return complete;

            case 2:
                if (config == null) return Collections.emptyList();
                StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
                return complete;
                
            case 3:
                StringUtil.copyPartialMatches(args[2], bindArgs, complete);
                return complete;

            case 4:
                if (!args[2].equalsIgnoreCase("remove")) return Collections.emptyList();
                if (config == null) return Collections.emptyList();
                
                StringUtil.copyPartialMatches(args[3], config.getStringList(args[1] + ".commands"), complete);
                return complete;
                
            default:
                return Collections.emptyList();
        }
    }
}