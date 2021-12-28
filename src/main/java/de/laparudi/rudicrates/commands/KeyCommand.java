package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class KeyCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.key")) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().noPermission);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().keySyntax);
            return true;
        }
        
        if (args.length == 2) {
            final UUID uuid = UUIDFetcher.getUUID(args[1]);
            final String name = UUIDFetcher.getName(uuid);
            
            if(uuid == null || name == null || !RudiCrates.getPlugin().getDataUtils().playerExists(uuid)) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownPlayer);
                return true;
            }
            
            final Player target = Bukkit.getPlayer(uuid);
            
            if (args[0].equalsIgnoreCase("info")) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyHeader.replace("%player%", name));
                sender.sendMessage("");
                
                Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> {
                    final int keyAmount = target != null ? RudiCrates.getPlugin().getCrateUtils().getKeyItemAmount(target, crate) +
                            RudiCrates.getPlugin().getDataUtils().getCrateAmount(uuid, crate) : RudiCrates.getPlugin().getDataUtils().getCrateAmount(uuid, crate);
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + crate.getDisplayname() + " §8» §a" + keyAmount);
                });
                sender.sendMessage("");

            } else if (args[0].equalsIgnoreCase("reset")) {
                if (RudiCrates.getPlugin().getDataUtils().playerExists(uuid)) {
                    RudiCrates.getPlugin().getDataUtils().resetPlayer(uuid);
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyReset.replace("%player%", name));

                } else
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownPlayer);
            } else
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().keySyntax);
            
            return true;
        }
        
        if (args.length == 4 || args.length == 5) {
            final boolean item = args.length == 5 && args[4].equalsIgnoreCase("item");
            final int amount;

            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().noNumber);
                return true;
            }

            if (amount < 1 || amount > RudiCrates.getPlugin().getConfig().getInt("max_key_amount")) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().toLowOrToHigh.replace("%highest%", String.valueOf(RudiCrates.getPlugin().getConfig().getInt("max_key_amount"))));
                return true;
            }

            Crate crate;
            
            try {
                crate = Crate.getByName(args[2]);
            } catch (NullPointerException e) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownCrate);
                return true;
            }
            
            final String crateName = crate.getDisplayname();
            final UUID uuid = UUIDFetcher.getUUID(args[1]);
            final String name = UUIDFetcher.getName(uuid);
            
            if(uuid == null || name == null || !RudiCrates.getPlugin().getDataUtils().playerExists(uuid)) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().unknownPlayer);
                return true;
            }
            
            final Player target = Bukkit.getPlayer(uuid);
            final boolean online = target != null;

            if (args[0].equalsIgnoreCase("add")) {
                if(item) {
                    if(!online) {
                        sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyPlayerNotOnline);
                        return true;
                    }
                    
                    final Map<Integer, ItemStack> map = target.getInventory().addItem(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, amount));
                    if(!map.isEmpty()) {
                        map.values().forEach(items -> target.getWorld().dropItemNaturally(target.getLocation(), items));
                    }
                    
                } else
                    RudiCrates.getPlugin().getDataUtils().addCrates(uuid, crate, amount);

                sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneAddExecutor.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));
                if(sender != target && target != null) target.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneAddTarget.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));

            } else if (args[0].equalsIgnoreCase("remove")) {
                RudiCrates.getPlugin().getDataUtils().removeCrates(uuid, crate, amount);
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneRemoveExecutor.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));
                if (online && target != sender) target.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneRemoveTarget.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));

            } else if (args[0].equalsIgnoreCase("set")) {
                RudiCrates.getPlugin().getDataUtils().setCrateAmount(uuid, crate, amount);
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneSetExecutor.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));
                if (online && target != sender) target.sendMessage(RudiCrates.getPlugin().getLanguage().keyDoneSetTarget.replace("%player%", name).replace("%amount%", String.valueOf(amount)).replace("%crate%", crateName));
            }
            
        } else
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().keySyntax);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.key")) return Collections.emptyList();
        
        final List<String> complete = new ArrayList<>();
        final String[] commandArgs = new String[] { "add", "set", "remove", "info", "reset" };
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> crateArgs.add(crate.getName()));
        
        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commandArgs), complete);
            return complete;

        } else if(args.length == 2) {
            return null; // To show player names
            
        } else if(args.length == 3) {
            if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[2], crateArgs, complete);
                return complete;
            }
            
        } else if(args.length == 5) {
            StringUtil.copyPartialMatches(args[4], Collections.singleton("item"), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}