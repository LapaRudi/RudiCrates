package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.version.VersionUtils;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RudiCratesCommand implements CommandExecutor, TabCompleter {
    
    private final VersionUtils versionUtils = RudiCrates.getPlugin().getVersionUtils();
    private final Language language = RudiCrates.getPlugin().getLanguage();
    
    private final ComponentBuilder addToCrateComponent = versionUtils.component(language.getValue("addtocrate_syntax", false), "/addtocrate", language.descriptionAddToCrate);
    private final ComponentBuilder bindCommandComponent = versionUtils.component(language.getValue("bindcommand_syntax", false), "/bindcommand", language.descriptionBindCommand);
    private final ComponentBuilder editChanceComponent = versionUtils.component(language.getValue("editchance_syntax", false), "/editchance", language.descriptionEditChance);
    private final ComponentBuilder getCaseBlockComponent = versionUtils.component("/getcaseblock", "/getcaseblock", language.descriptionGetCrateBlock);
    private final ComponentBuilder keyComponent = versionUtils.component(language.getValue("key_syntax", false), "/key", language.descriptionKey);
    private final ComponentBuilder removeFromCrateComponent = versionUtils.component(language.getValue("removefromcrate_syntax", false), "/removefromcrate", language.descriptionRemoveFromCrate);
    private final ComponentBuilder rudiCratesComponent = versionUtils.component(language.getValue("rudicrates_syntax", false), "/rudicrates", language.descriptionRudiCrates);
    private final ComponentBuilder setLimitedComponent = versionUtils.component(language.getValue("setlimited_syntax", false), "/setlimited", language.descriptionSetLimited);
    private final ComponentBuilder setVirtualComponent = versionUtils.component(language.getValue("setvirtual_syntax", false), "/setvirtual", language.descriptionSetVirtual);

    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.reload")) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + "§6v" + RudiCrates.getPlugin().getVersion() + "§c by LapaRudi");
            return true;
        }
        
        if(args.length == 0) {
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + "§6v" + RudiCrates.getPlugin().getVersion() + "§c ↓");
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
            sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + "§6v" + RudiCrates.getPlugin().getVersion() + "§c ↑");
        
        } else if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                RudiCrates.getPlugin().getCrateUtils().setupKeyItemList();
                RudiCrates.getPlugin().getCrateUtils().loadChancesResult();
                RudiCrates.getPlugin().reloadLanguage();
                RudiCrates.getPlugin().reloadConfig();
                Crate.reloadCrateMaps();
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().reloadAll);
                
            } else if(args[0].equalsIgnoreCase("toggle")) {
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().prefix + "§cThis feature is currently disabled.");
                return true;
                
                /*
                
                if (RudiCrates.getPlugin().getConfig().getBoolean("enabled")) {
                    RudiCrates.getPlugin().getConfig().set("enabled", false);
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().rudiCratesDisabled);

                } else {
                    RudiCrates.getPlugin().getConfig().set("enabled", true);
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().rudiCratesEnabled);
                }

                try {
                    RudiCrates.getPlugin().getConfig().save(RudiCrates.getPlugin().getConfigFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                 */
                
            } else
                sender.sendMessage(RudiCrates.getPlugin().getLanguage().rudiCratesSyntax);
            
        } else if(args.length == 2) {
            switch (args[1].toLowerCase()) {
                case "config":
                    RudiCrates.getPlugin().reloadConfig();
                    RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                    RudiCrates.getPlugin().getCrateUtils().setupKeyItemList();
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().reloadConfig);
                    break;
                    
                case "messages":
                    RudiCrates.getPlugin().reloadLanguage();
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().reloadMessages);
                    break;
                    
                case "preview":
                    RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                    RudiCrates.getPlugin().getCrateUtils().loadChancesResult();
                    Crate.reloadCrateMaps();
                    sender.sendMessage(RudiCrates.getPlugin().getLanguage().reloadPreview);
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if(!sender.hasPermission("rudicrates.reload")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();
        
        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "toggle"), complete);
            return complete;
            
        } else if(args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("config", "messages", "preview"), complete);
            return complete;
        }
        
        return Collections.emptyList();
    }
}
