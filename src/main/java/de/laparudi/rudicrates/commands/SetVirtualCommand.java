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

public class SetVirtualCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.setvirtual")) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }
        
        if ((args.length != 3) || (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().setVirtualSyntax);
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
        
        if(!config.contains(args[1])) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownID);
            return true;
        }
        
        config.set(args[1] + ".virtual", Boolean.valueOf(args[2]));
        sender.sendMessage(RudiCrates.getPlugin().getLanguage().setVirtualDone.replace("%id%", args[1]).replace("%crate%", crate.getName()).replace("%value%", args[2].toLowerCase()));
        
        if(Boolean.parseBoolean(args[2]) && config.getString(args[1] + ".command") == null) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().setVirtualWarning);
        }
        
        try {
            config.save(crate.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.setvirtual")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, complete);
            return complete;
            
        } else if(args.length == 2) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
            
        } else if(args.length == 3) {
            StringUtil.copyPartialMatches(args[2], Arrays.asList("true", "false"), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
