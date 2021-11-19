package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.utils.Messages;
import de.laparudi.rudicrates.utils.version.VersionUtils;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RudiCratesCommand implements CommandExecutor, TabCompleter {
    
    private final VersionUtils versionUtils = RudiCrates.getPlugin().getVersionUtils();
    private final ComponentBuilder addToCrateComponent = versionUtils.component("/addtocrate §7<§fCrate§7> <§fGewinnchance in %§7>", "/addtocrate", "Fügt das Item in deiner Hand der angegebenen Crate mit der angegebenen Gewinnchance hinzu.");
    private final ComponentBuilder bindCommandComponent = versionUtils.component("/bindcommand §7<§fCrate§7> <§fItem-ID§7> <§fBefehl§7>", "/bindcommand", "Fügt einen Befehl hinzu, der ausgeführt wird, wenn das angegebene Item gewonnen wird.");
    private final ComponentBuilder editChanceComponent = versionUtils.component("/editchance §7<§fCrate§7> <§fItem-ID§7> <§fNeue Chance in %§7>", "/editchance", "Ändert die Gewinnchance des angegebenen Items.");
    private final ComponentBuilder getCaseBlockComponent = versionUtils.component("/getcaseblock", "/getcaseblock", "Legt dir ein Crate-Opening-Block ins Inventar.");
    private final ComponentBuilder keyComponent = versionUtils.component("/key §7<§fadd§7/§fset§7/§fremove§7/§freset§7/§finfo§7> <§fSpieler§7> [§fCrate§7] [§fMenge§7]", "/key", "Ändert die Key (Crate) Anzahl des angegebenen Spielers.");
    private final ComponentBuilder removeFromCrateComponent = versionUtils.component("/removefromcrate §7<§fCrate§7> <§fItem-ID§7>", "/removefromcrate", "Entfernt das angegebene Item aus der angegebenen Crate.");
    private final ComponentBuilder rudiCratesComponent = versionUtils.component("/rudicrates §7[§freload§7/§ftoggle§7]", "/rudicrates", "Zeigt eine Hilfe an, lädt die Gewinne und Config neu oder deaktiviert das öffnen von Crates.");
    private final ComponentBuilder setLimitedComponent = versionUtils.component("/setlimited §7<§fCrate§7> <§fItem-ID§7> <§fMenge§7>", "/setlimited", "Limitiert das angegebene Item auf die angegebene Stückzahl.");
    private final ComponentBuilder setVirtualComponent = versionUtils.component("/setvirtual §7<§fCrate§7> <§fItem-ID§7> <§ftrue§7/§ffalse§7>", "/setvirtual", "Macht das angegebene Item virtuell. [Wenn das Item gewonnen wird bekommt man es nicht ins Inventar und es wird nur der Befehl ausgeführt.]");

    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.admin")) {
            sender.sendMessage(Messages.PREFIX + "§fv" + RudiCrates.getPlugin().getDescription().getVersion() + "§7 von LapaRudi");
            return true;
        }
        
        if(args.length == 0) {
            sender.sendMessage(Messages.PREFIX + "§bRudiCrates v" + RudiCrates.getPlugin().getDescription().getVersion() + "§7 -§f Hilfe");
            sender.sendMessage(" ");
            sender.spigot().sendMessage(addToCrateComponent.create());
            sender.spigot().sendMessage(bindCommandComponent.create());
            sender.spigot().sendMessage(editChanceComponent.create());
            sender.spigot().sendMessage(getCaseBlockComponent.create());
            sender.spigot().sendMessage(keyComponent.create());
            sender.spigot().sendMessage(removeFromCrateComponent.create());
            sender.spigot().sendMessage(rudiCratesComponent.create());
            sender.spigot().sendMessage(setLimitedComponent.create());
            sender.spigot().sendMessage(setVirtualComponent.create());
            sender.sendMessage(" ");
            sender.sendMessage(Messages.PREFIX + "§bRudiCrates v" + RudiCrates.getPlugin().getDescription().getVersion() + "§7 -§f Hilfe");
        
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                Crate.reloadCrateMaps();
                sender.sendMessage(Messages.PREFIX + "§fConfig und Gewinne wurden neu geladen.");
                
            } else if(args[0].equalsIgnoreCase("toggle")) {
                if (RudiCrates.getPlugin().getConfig().getBoolean("enabled")) {
                    RudiCrates.getPlugin().getConfig().set("enabled", false);
                    sender.sendMessage(Messages.PREFIX + "§7Das öffnen von Crates wurde §fdeaktiviert§7.");

                } else {
                    RudiCrates.getPlugin().getConfig().set("enabled", true);
                    sender.sendMessage(Messages.PREFIX + "§7Das öffnen von Crates wurde §faktiviert§7.");
                }

                try {
                    RudiCrates.getPlugin().getConfig().save(RudiCrates.getPlugin().getConfigFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            } else
                sender.sendMessage(Messages.PREFIX + "§7Benutze §f/rudicrates §7[§freload§7/§ftoggle§7]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.admin")) return null;
        final List<String> complete = new ArrayList<>();
        
        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "toggle"), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
