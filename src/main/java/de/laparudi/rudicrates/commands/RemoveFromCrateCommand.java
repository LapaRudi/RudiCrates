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

public class RemoveFromCrateCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!(sender.hasPermission("rudicrates.removefromcrate"))) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }
        
        if(args.length != 2) {
            sender.sendMessage(Messages.SYNTAX_REMOVEFROMCRATE.toString());
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
        int id;
        
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Messages.PREFIX + "§7Du musst einen Zahlenwert angeben.");
            return true;
        }
        
        if(!config.getKeys(false).contains(String.valueOf(id))) {
            sender.sendMessage(Messages.PREFIX + "§7Ungültige Item-ID.");
            return true;
        }
        
        config.set(String.valueOf(id), null);
        
        try {
            config.save(crate.getFile());
            sender.sendMessage(Messages.PREFIX + "§7Item §f" + id + "§7 aus der Crate §f" + crate.getName() + "§7 entfernt.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.addtocrate")) return null;
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
