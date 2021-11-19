package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.utils.Messages;
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
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Messages.PREFIX + "§7Platzhalter:");
            sender.sendMessage(Messages.PREFIX + "§c%crate% §f: §7Die geöffnete Crate.");
            sender.sendMessage(Messages.PREFIX + "§c%player% §f: §7Spieler der das Item gewonnen hat.");
            sender.sendMessage(Messages.PREFIX + "§c%chance% §f: §7Die Gewinnchance des gewonnenen Items.");
            sender.sendMessage("");
            sender.sendMessage(Messages.PREFIX + "§7Benutze '#remove' als 3. Argument um den Befehl zu entfernen.");
            sender.sendMessage(Messages.SYNTAX_BINDCOMMAND.toString());
            return true;
        }

        Crate crate;

        try {
            crate = Crate.getByName(args[0]);
        } catch (NullPointerException e) {
            sender.sendMessage(Messages.UNKNOWN_CRATE.toString());
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());

        if (!config.contains(args[1])) {
            sender.sendMessage(Messages.UNKNOWN_ID.toString());
            return true;
        }

        if (args[2].equalsIgnoreCase("#remove")) {
            if (config.getString(args[1] + ".command") == null) {
                sender.sendMessage(Messages.PREFIX + "Dieses Item hat keinen zugehörigen Befehl.");
                return true;
            }

            try {
                sender.sendMessage(Messages.PREFIX + "§7Befehl §f/" + config.getString(args[1] + ".command") + "§7 wurde von Item §f" + args[1] + "§7 aus Crate §f" + crate.getName() + "§7 entfernt.");
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

        final String cmd = args[2].startsWith("/") ? builder.toString().trim().replaceFirst("/", "") : builder.toString().trim();
        config.set(args[1] + ".command", cmd);

        try {
            config.save(crate.getFile());
            sender.sendMessage(Messages.PREFIX + "§7Befehl §f/" + cmd + "§7 zu Item §f" + args[1] + "§7 in Crate §f" + crate.getName() + "§7 hinzugefügt.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.bindcommand")) return null;
        final List<String> completions = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

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