package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class GetCrateBlockCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!(sender.hasPermission("rudicrates.getcrateblock")) || !(sender instanceof Player)) {
            Language.send(sender, "player.no_permission");
            return true;
        }

        ((Player) sender).getInventory().addItem(RudiCrates.getPlugin().getItemManager().crateBlock);
        Language.send(sender, "commands.getcrateblock.done");
        return true;
    }
}
