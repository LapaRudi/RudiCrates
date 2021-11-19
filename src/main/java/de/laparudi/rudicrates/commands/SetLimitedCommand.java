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

public class SetLimitedCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.setlimited")) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }

        if(args.length != 3) {
            sender.sendMessage(Messages.SYNTAX_SETLIMITED.toString());
            return true;
        }

        int amount;
        Crate crate;

        try {
            crate = Crate.getByName(args[0]);
        } catch (NullPointerException e) {
            sender.sendMessage(Messages.UNKNOWN_CRATE.toString());
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());

        if(!config.contains(args[1])) {
            sender.sendMessage(Messages.UNKNOWN_ID.toString());
            return true;
        }
        
        if(args[2].equalsIgnoreCase("#remove")) {
            config.set(args[2] + ".limited", null);
            try {
                config.save(crate.getFile());
                sender.sendMessage(Messages.PREFIX + "§7Die Limitierung von Item §f" + args[1] + " §7aus der Crate §f" + crate.getName() + "§7 wurde entfernt.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.NO_NUMBER.toString());
            return true;
        }
        
        if(amount < 0) {
            sender.sendMessage(Messages.PREFIX + "§7Der Wert darf nicht unter 0 sein.");
            return true;
        }

        config.set(args[1] + ".limited", amount);
        sender.sendMessage(Messages.PREFIX + "§7Item §f" + args[1] + "§7 aus der Crate §f" + crate.getName() + "§7 wurde auf §f" + amount + "§7 Stück limitiert.");

        try {
            config.save(crate.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.setlimited")) return null;
        final List<String> complete = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, complete);
            return complete;
        }

        if(args.length == 2) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}
