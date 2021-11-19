package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.items.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class GetCaseBlockCommand extends ItemManager implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!(sender.hasPermission("rudicrates.getcaseblock")) || !(sender instanceof Player)) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }
        
        final Player player = (Player) sender;
        player.getInventory().addItem(crateBlock);
        player.sendMessage(Messages.PREFIX + "§7Du hast ein §fCrate Opening §7erhalten.");
        return true;
    }
}
