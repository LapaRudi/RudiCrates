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

public class EditChanceCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.editchance")) {
            Language.send(sender, "player.no_permission");
            return true;
        }

        if (args.length == 0) {
            Language.send(sender, "commands.editchance.syntax");
            return true;
        }
        
        if (args.length == 3) {
            Crate crate;
            double chance;

            try {
                crate = Crate.getByName(args[0]);
            } catch (final NullPointerException exception) {
                Language.send(sender, "crate.unknown");
                return true;
            }

            try {
                chance = Double.parseDouble(args[2].replace(',', '.'));
            } catch (final NumberFormatException exception) {
                Language.send(sender, "numbers.invalid");
                return true;
            }

            final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
            if (!crateConfig.getKeys(false).contains(args[1])) {
                Language.send(sender, "crate.unknown_id");
                return true;
            }

            crateConfig.set(args[1] + ".chance", chance);
            try {
                crateConfig.save(crate.getFile());
                Language.send(sender, "commands.editchance.done", new String[] { "%crate%", "%id%", "%chance%" }, new String[] { crate.getName(), args[1], String.valueOf(chance) });
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
            return true;
        }
        
        Language.send(sender, "commands.editchance.syntax");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.editchance")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], CrateUtils.getCrateNames(), complete);
            return complete;
        
        } else if (args.length == 2) {
            final FileConfiguration config = Crate.getCrateConfigCache().get(args[0]);
            if (config == null) return Collections.emptyList();
            StringUtil.copyPartialMatches(args[1], config.getKeys(false), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
