package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.version.VersionUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RudiCratesCommand implements CommandExecutor, TabCompleter {
    
    private final String version = RudiCrates.getPlugin().getVersion();
    private final VersionUtils versionUtils = RudiCrates.getPlugin().getVersionUtils();
    
    private final BaseComponent[] addToCrateComponent  = versionUtils.component(Language.withoutPrefix("commands.addtocrate.syntax"), "/addtocrate", Language.withoutPrefix("descriptions.addtocrate"));
    private final BaseComponent[] bindCommandComponent = versionUtils.component(Language.withoutPrefix("commands.bindcommand.syntax"), "/bindcommand", Language.withoutPrefix("descriptions.bindcommand"));
    private final BaseComponent[] editChanceComponent = versionUtils.component(Language.withoutPrefix("commands.editchance.syntax"), "/editchance", Language.withoutPrefix("descriptions.editchance"));
    private final BaseComponent[] getCaseBlockComponent = versionUtils.component(Language.withoutPrefix("commands.getcrateblock.syntax"), "/getcrateblock", Language.withoutPrefix("descriptions.getcrateblock"));
    private final BaseComponent[] keyComponent = versionUtils.component(Language.withoutPrefix("commands.key.syntax"), "/key", Language.withoutPrefix("descriptions.key"));
    private final BaseComponent[] removeFromCrateComponent = versionUtils.component(Language.withoutPrefix("commands.removefromcrate.syntax"), "/removefromcrate", Language.withoutPrefix("descriptions.removefromcrate"));
    private final BaseComponent[] rudiCratesComponent = versionUtils.component(Language.withoutPrefix("commands.rudicrates.syntax"), "/rudicrates", Language.withoutPrefix("descriptions.rudicrates"));
    private final BaseComponent[] setLimitedComponent = versionUtils.component(Language.withoutPrefix("commands.setlimited.syntax"), "/setlimited", Language.withoutPrefix("descriptions.setlimited"));
    private final BaseComponent[] setVirtualComponent = versionUtils.component(Language.withoutPrefix("commands.setvirtual.syntax"), "/setvirtual", Language.withoutPrefix("descriptions.setvirtual"));

    private final BaseComponent[] infoComponentHeader = versionUtils.builder(Language
            .withoutPrefix("commands.rudicrates.header", "%version%", version), "§7§o<?>", Language.withoutPrefix("commands.rudicrates.info"));

    private final BaseComponent[] infoComponentFooter = versionUtils.builder(Language
            .withoutPrefix("commands.rudicrates.footer", "%version%", version), "§7§o<?>", Language.withoutPrefix("commands.rudicrates.info"));

    private void sendIfPermission(final CommandSender sender, final String permission, final BaseComponent[] components) {
        if (!sender.hasPermission(permission)) return;
        if (!(sender instanceof Player)) {
            sender.sendMessage(components[0].toLegacyText());
            return;
        }

        ((Player) sender).spigot().sendMessage(components);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.isOp() && !sender.hasPermission("*") && sender.getEffectivePermissions().stream().noneMatch(permission -> permission.getPermission().contains("rudicrates."))) {
            sender.sendMessage(Language.getPrefix() + "§6v" + RudiCrates.getPlugin().getVersion() + "§c by LapaRudi");
            return true;
        }

        switch (args.length) {
            case 0:
                Language.send(sender, infoComponentHeader, "commands.rudicrates.header");
                sender.sendMessage("");
                this.sendIfPermission(sender, "rudicrates.addtocrate", addToCrateComponent);
                this.sendIfPermission(sender, "rudicrates.bindcommand", bindCommandComponent);
                this.sendIfPermission(sender, "rudicrates.editchance", editChanceComponent);
                this.sendIfPermission(sender, "rudicrates.getcrateblock", getCaseBlockComponent);
                this.sendIfPermission(sender, "rudicrates.key", keyComponent);
                this.sendIfPermission(sender, "rudicrates.removefromcrate", removeFromCrateComponent);
                this.sendIfPermission(sender, "rudicrates.rudicrates", rudiCratesComponent);
                this.sendIfPermission(sender, "rudicrates.setlimited", setLimitedComponent);
                this.sendIfPermission(sender, "rudicrates.setvirtual", setVirtualComponent);
                sender.sendMessage("");
                Language.send(sender, infoComponentFooter, "commands.rudicrates.footer");
                break;

            case 1:
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("rudicrates.reload")) {
                        Language.send(sender, "player.no_permission");
                        return true;
                    }
                    
                    RudiCrates.getPlugin().reloadMessagesConfig();
                    RudiCrates.getPlugin().reloadConfig();
                    Crate.reloadCrateMaps();
                    RudiCrates.getPlugin().getLanguage().loadMessages();
                    RudiCrates.getPlugin().getItemManager().reloadItems();
                    RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                    RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                    CrateUtils.loadCrates();
                    RudiCrates.getPlugin().getCrateUtils().loadChancesResult();
                    Language.send(sender, "commands.rudicrates.reload_all");
                    return true;
                }
                
                if (args[0].equalsIgnoreCase("toggle")) {
                    if (!sender.hasPermission("rudicrates.toggle")) {
                        Language.send(sender, "player.no_permission");
                        return true;
                    }
                    
                    if (RudiCrates.getPlugin().getConfig().getBoolean("enabled")) {
                        RudiCrates.getPlugin().getConfig().set("enabled", false);
                        Language.send(sender, "commands.rudicrates.disabled");
                        
                    } else {
                        RudiCrates.getPlugin().getConfig().set("enabled", true);
                        Language.send(sender, "commands.rudicrates.enabled");
                    }

                    RudiCrates.getPlugin().saveAndReloadConfig();
                    break;
                }

                Language.send(sender, "commands.rudicrates.syntax");
                break;

            case 2:
                switch (args[1].toLowerCase()) {
                    case "config":
                        RudiCrates.getPlugin().reloadConfig();
                        RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                        CrateUtils.loadCrates();
                        RudiCrates.getPlugin().getItemManager().reloadItems();
                        RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                        Language.send(sender, "commands.rudicrates.reload_config");
                        return true;

                    case "messages":
                        RudiCrates.getPlugin().reloadMessagesConfig();
                        RudiCrates.getPlugin().getLanguage().loadMessages();
                        Language.send(sender, "commands.rudicrates.reload_messages");
                        return true;

                    case "items":
                        RudiCrates.getPlugin().getItemManager().reloadItems();
                        RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                        RudiCrates.getPlugin().getCrateUtils().loadChancesResult();
                        Crate.reloadCrateMaps();
                        RudiCrates.getPlugin().getInventoryUtils().setupCrateMenu();
                        CrateUtils.loadCrates();
                        Language.send(sender, "commands.rudicrates.reload_items");
                        return true;
                }
                
            default:
                Language.send(sender, "commands.rudicrates.syntax");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.reload") && !sender.hasPermission("rudicrates.toggle")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload", "toggle"), complete);
            return complete;

        } else if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], Arrays.asList("config", "messages", "items"), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}
