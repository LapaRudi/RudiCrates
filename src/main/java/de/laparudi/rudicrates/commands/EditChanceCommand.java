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

public class EditChanceCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.editchance")) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().editChanceSyntax);

        } else if (args.length == 3) {
            Crate crate;
            double chance;

            try {
                crate = Crate.getByName(args[0]);
            } catch (NullPointerException e) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownCrate);
                return true;
            }

            try {
                chance = Double.parseDouble(args[2].replace(',', '.'));
            } catch (NumberFormatException e) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().noNumber);
                return true;
            }

            final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
            if (!crateConfig.getKeys(false).contains(args[1])) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownID);
                return true;
            }

            crateConfig.set(args[1] + ".chance", chance);
            try {
                crateConfig.save(crate.getFile());
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().editChanceDone.replace("%crate%", crate.getName()).replace("%id%", args[1]).replace("%chance%", String.valueOf(chance)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.editchance")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, complete);
            return complete;

        } else if (args.length == 2) {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(Crate.getByName(args[0]).getFile());
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
