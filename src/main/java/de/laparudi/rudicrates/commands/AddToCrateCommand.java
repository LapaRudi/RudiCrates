package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class AddToCrateCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!(sender.hasPermission("rudicrates.addtocrate")) || !(sender instanceof Player)) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }
        
        if(args.length != 2) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().addToCrateSyntax);
            return true;
        }
        
        final Player player = (Player) sender;
        final ItemStack item = RudiCrates.getPlugin().getVersionUtils().getPlayersItemInHand(player);
        Crate crate;
        double chance;
        
        try {
            crate = Crate.getByName(args[0]);
        } catch (NullPointerException e) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().unknownCrate);
            return true;
        }
        
        if(item.getType() == Material.AIR) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().noItemInHand);
            return true;
        }
        
        try {
            chance = Double.parseDouble(args[1].replace(',', '.'));
        } catch (NumberFormatException e) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().noNumber);
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
        for(String key : config.getKeys(false)) {
            if(Objects.requireNonNull(config.getItemStack(key + ".item")).isSimilar(item)) {
                player.sendMessage(RudiCrates.getPlugin().getLanguage().itemAlreadyInCrate);
                return true;
            }
        }
        
        final int keySize = config.getKeys(false).size() == 0 ? 2 : config.getKeys(false).size()+1;
        for(int id = 0; id < keySize; id++) {
            if(!config.getKeys(false).contains(String.valueOf(id))) {
                config.set(id + ".item", item);
                config.set(id + ".chance", chance);
                break;
            }
        }
        
        try {
            config.save(crate.getFile());
            player.sendMessage(RudiCrates.getPlugin().getLanguage().addToCrateDone.replace("%crate%", crate.getName()).replace("%chance%", String.valueOf(chance)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.addtocrate")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], crateArgs, complete);
            return complete;
        }

        return Collections.emptyList();
    }
}
