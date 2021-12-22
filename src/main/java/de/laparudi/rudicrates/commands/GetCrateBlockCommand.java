package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class GetCrateBlockCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!(sender.hasPermission("rudicrates.getcrateblock")) || !(sender instanceof Player)) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }
        
        final Player player = (Player) sender;
        player.getInventory().addItem(RudiCrates.getPlugin().getItemManager().crateBlock);
        player.sendMessage(RudiCrates.getPlugin().getLanguage().crateOpeningReceived);
        return true;
    }
}
