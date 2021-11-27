package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.mysql.SQLUtils;
import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class SQLKeyCommand implements CommandExecutor, TabCompleter {
    
    private void createIfNotExists(UUID uuid) {
        if(!RudiCrates.getPlugin().getMySQL().playerExists(uuid)) {
            RudiCrates.getPlugin().getMySQL().createPlayer(uuid);
        }
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Messages.PREFIX + "§cFehlerhafte Syntax, benutze /key für eine Übersicht.");
    }

    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.key")) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("");
            sender.sendMessage(Messages.PREFIX + "§f/key add §7<§fSpieler§7> <§fCrate§7> <§fAnzahl§7>");
            sender.sendMessage(Messages.PREFIX + "§f/key set §7<§fSpieler§7> <§fCrate§7> <§fAnzahl§7>");
            sender.sendMessage(Messages.PREFIX + "§f/key remove §7<§fSpieler§7> <§fCrate§7> <§fAnzahl§7>");
            sender.sendMessage(Messages.PREFIX + "§f/key info §7<§fSpieler§7>");
            sender.sendMessage(Messages.PREFIX + "§f/key reset §7<§fSpieler§7>");
            sender.sendMessage("");
            return true;
        }

        if (args.length == 2) {
            final UUID uuid = UUIDFetcher.getUUID(args[1]);
            if(uuid == null) {
                sender.sendMessage(Messages.PLAYER_NOT_FOUND.toString());
                return true;
            }

            createIfNotExists(uuid);
            
            final String name = UUIDFetcher.getName(uuid);
            final Player target = Bukkit.getPlayer(uuid);
            
            if (args[0].equalsIgnoreCase("info")) {
                sender.sendMessage("");
                sender.sendMessage(Messages.PREFIX + "§fCrates von §c" + name + "§f:");
                Arrays.stream(CrateUtils.getCrates()).forEach(crate -> sender.sendMessage(Messages.PREFIX + crate.getDisplayname() + " §8» §a" + SQLUtils.getCrateAmount(uuid, crate)));
                sender.sendMessage("");

            } else if (args[0].equalsIgnoreCase("reset")) {
                if (RudiCrates.getPlugin().getMySQL().resetPlayer(uuid)) {
                    sender.sendMessage(Messages.PREFIX + "§7Die Crates von §c" + name + "§7 wurden zurückgesetzt.");
                    if (target != null && target != sender) target.sendMessage(Messages.PREFIX + "§7Deine Crates wurden zurückgesetzt.");

                } else
                    sender.sendMessage(Messages.PREFIX + "§7Dieser Spieler wurde nicht gefunden.");
            } else
                sendHelp(sender);
            return true;
        }

        if (args.length == 4) {
            final int amount;

            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.PREFIX + "§7Du musst einen Zahlenwert angeben.");
                return true;
            }

            if (amount < 1) {
                sender.sendMessage(Messages.PREFIX + "§7Du musst mindestens einen Wert von 1 angeben.");
                return true;
            }

            Crate crate;

            try {
                crate = Crate.getByName(args[2]);
            } catch (NullPointerException e) {
                sender.sendMessage(Messages.UNKNOWN_CRATE.toString());
                return true;
            }

            final String crateName = crate.getDisplayname();
            final UUID uuid = UUIDFetcher.getUUID(args[1]);
            
            if(uuid == null) {
                sender.sendMessage(Messages.PLAYER_NOT_FOUND.toString());
                return true;
            }

            createIfNotExists(uuid);
            
            final String name = UUIDFetcher.getName(uuid);
            final Player target = Bukkit.getPlayer(uuid);

            if (args[0].equalsIgnoreCase("add")) {
                SQLUtils.addCrates(uuid, crate, amount);
                sender.sendMessage(Messages.PREFIX + "§7Du hast §c" + name + "§f " + amount + "x " + crateName + "§7 gegeben.");
                if (target != null && target != sender) target.sendMessage(Messages.PREFIX + "§7Du hast §f" + amount + "x " + crateName + "§7 erhalten.");

            } else if (args[0].equalsIgnoreCase("remove")) {
                SQLUtils.removeCrates(uuid, crate, amount);
                sender.sendMessage(Messages.PREFIX + "§7Du hast §c" + name + "§f " + amount + "x " + crateName + "§7 genommen.");
                if (target != null && target != sender) target.sendMessage(Messages.PREFIX + "§7Dir wurden §f" + amount + "x " + crateName + "§7 genommen.");

            } else if (args[0].equalsIgnoreCase("set")) {
                SQLUtils.setCrateAmount(uuid, crate, amount);
                sender.sendMessage(Messages.PREFIX + "§7Du hast §c" + name + "'s §f" + crateName + "§7 Anzahl auf §f" + amount + "§7 gesetzt.");
                if (target != null && target != sender) target.sendMessage(Messages.PREFIX + "§7Deine §f" + crateName + "§7 Anzahl wurde auf §f" + amount + "§7 gesetzt.");
            }
        
        } else
            sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.key")) return null;
        final List<String> complete = new ArrayList<>();
        final String[] commandArgs = new String[] { "add", "set", "remove", "info", "reset" };
        final List<String> crateArgs = new ArrayList<>();
        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> crateArgs.add(crate.getName()));

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList(commandArgs), complete);
            return complete;

        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set")) {
                StringUtil.copyPartialMatches(args[2], crateArgs, complete);
                return complete;
            }
            
        } else if (args.length == 4) {
            return Collections.emptyList();
        }

        return null;
    }
}