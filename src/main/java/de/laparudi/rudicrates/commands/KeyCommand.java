package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
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
            Language.send(sender, "player.no_permission");
            return true;
        }

        if (args.length < 2 || args.length == 3 || args.length > 5) {
            Language.send(sender, "commands.key.syntax");
            return true;
        }

        final UUID uuid = UUIDFetcher.getUUID(args[1]);
        final String name = UUIDFetcher.getName(uuid);

        if (uuid == null || name == null) {
            Language.send(sender, "player.unknown");
            return true;
        }

        final Player target = Bukkit.getPlayer(uuid);
        final boolean online = target != null;

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                sender.sendMessage("");
                Language.send(sender, "commands.key.header", "%player%", name);

                Arrays.stream(CrateUtils.getCrates()).forEach(crate -> {
                    final int itemKeyAmount = target != null ? RudiCrates.getPlugin().getCrateUtils().getKeyItemAmount(target, crate, true) : 0;
                    final int keyAmount = RudiCrates.getPlugin().getDataUtils().getCrateAmount(uuid, crate);
                    final String addon = itemKeyAmount != 0 ? " " + Language.withoutPrefix("commands.key.info_addon", "%amount%", String.valueOf(itemKeyAmount)) : "";
                    sender.sendMessage(Language.getPrefix() + crate.getDisplayname() + " §7→ §a" + keyAmount + addon);
                });

                sender.sendMessage("");
                return true;
            }

            if (args[0].equalsIgnoreCase("reset")) {
                if (!RudiCrates.getPlugin().getDataUtils().playerExists(uuid)) {
                    Language.send(sender, "players.unknown");
                    return true;
                }

                RudiCrates.getPlugin().getDataUtils().resetPlayer(uuid);
                Language.send(sender, "commands.key.reset", "%player%", name);
                return true;
            }

            Language.send(sender, "commands.key.syntax");
            return true;
        }

        final boolean item = args.length == 5 && args[4].equalsIgnoreCase("item");
        final int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (final NumberFormatException exception) {
            Language.send(sender, "numbers.no_integer");
            return true;
        }

        if (amount < 1 || amount > RudiCrates.getPlugin().getConfig().getInt("max_key_amount")) {
            Language.send(sender, "numbers.not_in_range", "%highest%", String.valueOf(RudiCrates.getPlugin().getConfig().getInt("max_key_amount")));
            return true;
        }

        Crate crate;

        try {
            crate = Crate.getByName(args[2]);
        } catch (final NullPointerException exception) {
            Language.send(sender, "crate.unknown");
            return true;
        }

        final String crateName = crate.getDisplayname();

        if (args[0].equalsIgnoreCase("add")) {
            RudiCrates.getPlugin().getDataUtils().createPlayer(uuid);
            if (item) {
                if (!online) {
                    Language.send(sender, "commands.key.player_offline");
                    return true;
                }

                final Map<Integer, ItemStack> map = target.getInventory().addItem(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, amount));
                if (!map.isEmpty()) {
                    map.values().forEach(items -> target.getWorld().dropItemNaturally(target.getLocation(), items));
                }

            } else
                RudiCrates.getPlugin().getDataUtils().addCrates(uuid, crate, amount);

            Language.send(sender, "commands.key.add_executor", new String[] {"%player%", "%amount%", "%crate%"}, new String[] { name, String.valueOf(amount), crateName });
            if (sender != target && target != null)
                Language.send(target, "commands.key.add_target", new String[] {"%player%", "%amount%", "%crate%"}, new String[] { name, String.valueOf(amount), crateName });

            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            RudiCrates.getPlugin().getDataUtils().createPlayer(uuid);
            RudiCrates.getPlugin().getDataUtils().removeCrates(uuid, crate, amount);

            Language.send(sender, "commands.key.remove_executor", new String[]{"%player%", "%amount%", "%crate%"}, new String[]{name, String.valueOf(amount), crateName});
            if (online && target != sender)
                Language.send(target, "commands.key.remove_target", new String[]{"%player%", "%amount%", "%crate%"}, new String[]{name, String.valueOf(amount), crateName});

            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            RudiCrates.getPlugin().getDataUtils().createPlayer(uuid);
            RudiCrates.getPlugin().getDataUtils().setCrateAmount(uuid, crate, amount);

            Language.send(sender, "commands.key.set_executor", new String[]{"%player%", "%amount%", "%crate%"}, new String[]{name, String.valueOf(amount), crateName});
            if (online && target != sender)
                Language.send(target, "commands.key.set_target", new String[]{"%player%", "%amount%", "%crate%"}, new String[]{name, String.valueOf(amount), crateName});

            return true;
        }

        Language.send(sender, "commands.key.syntax");
        return true;
    }

    private final String[] commandArgs = new String[] { "add", "set", "remove", "info", "reset" };
    
    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.key")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commandArgs), complete);
            return complete;

        } else if (args.length == 2) {
            return null; // To show player names

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[2], CrateUtils.getCrateNames(), complete);
                return complete;
            }

        } else if (args.length == 5) {
            StringUtil.copyPartialMatches(args[4], Collections.singleton("item"), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}