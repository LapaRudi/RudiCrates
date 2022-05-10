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

        RudiCrates.getPlugin().getCrateUtils().openCrateBlocksInventory(((Player) sender));
        return true;
    }
}
