package de.laparudi.rudicrates.language;

import de.laparudi.rudicrates.RudiCrates;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Language {
    
    public String getValue(String path, boolean withPrefix) {
        final String message = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(RudiCrates.getPlugin().getMessagesConfig().getString(path)));
        if(withPrefix) {
            return prefix + message;
        }
        
        return message;
    }

    public List<String> getListValue(String path, boolean withPrefix) {
        final List<String> list = RudiCrates.getPlugin().getMessagesConfig().getStringList(path);
        final List<String> newList = new ArrayList<>();
        
        if(withPrefix) {
            list.forEach(string -> newList.add(prefix + string));
        } else
            return list;
        
        return TranslationUtils.translateChatColor(newList);
    }
    
    public List<String> replaceFromList(List<String> list, String placeholder, String replaceTo) {
        final List<String> newList = new ArrayList<>();
        list.forEach(string -> newList.add(string.replace(placeholder, replaceTo)));
        return TranslationUtils.translateChatColor(newList);
    }
    
    public final String prefix = Bukkit.getBukkitVersion().contains("1.16") ? RudiCrates.getPlugin().getTranslationUtils().hexColorString(
            new String[] { "#4F4F4F", "#970460", "#96297b", "#914195", "#8755ac", "#7968c0", "#677ad0", "#538adc", "#3c9ae5", "#24a9ea", "#18b7ec", "#4F4F4F" },
            new Character[] { '[', 'R', 'u', 'd', 'i', 'C', 'r', 'a', 't', 'e', 's', ']', ' ' }) : "§8[§4Rudi§5Crates§8] §r";
    
    public final String noPermission = getValue("no_permission", true);
    public final String noNumber = getValue("no_number", true);
    public final String noItemInHand = getValue("no_item_in_hand", true);
    public final String incorrectSlotValue = getValue("incorrect_slot_value", true);
    public final String incorrectCloseInventorySlot = getValue("incorrect_closeinventory_slot", true);
    
    public final String crateOpeningDisabled = getValue("crate_opening_disabled", true);
    public final String incorrectWinChances = getValue("incorrect_win_chances", true);
    public final String openingCancelled = getValue("opening_cancelled", true);
    public final String noCratesRemaining = getValue("no_crates_remaining", true);
    public final String noWinsInCrate = getValue("no_wins_in_crate", true);
    public final String noWinsInCrateAddon = getValue("no_wins_in_crate_addon", true);
    public final String noItemsAvailable = getValue("no_items_available", true);
    
    public final String unknownPlayer = getValue("unknown_player", true);
    public final String unknownCrate = getValue("unknown_crate", true);
    public final String unknownID = getValue("unknown_id", true);
    
    public final String itemAlreadyInCrate = getValue("item_already_in_crate", true);
    public final String toLowOrToHigh = getValue("to_low_or_to_high", true);
    public final String notUnderZero = getValue("not_under_zero", true);
    public final String crateOpeningReceived = getValue("crate_opening_received", true);
    
    public final String reloadAll = getValue("reload_all", true);
    public final String reloadConfig = getValue("reload_config", true);
    public final String reloadMessages = getValue("reload_messages", true);
    public final String reloadPreview = getValue("reload_preview", true);
    
    public final String rudiCratesSyntax = getValue("rudicrates_syntax", true);
    public final String rudiCratesEnabled = getValue("rudicrates_enabled", true);
    public final String rudiCratesDisabled = getValue("rudicrates_disabled", true);
    
    public final String addToCrateSyntax = getValue("addtocrate_syntax", true);
    public final String addToCrateDone = getValue("addtocrate_done", true);
    
    public final String removeFromCrateSyntax = getValue("removefromcrate_syntax", true);
    public final String removeFromCrateDone = getValue("removefromcrate_done", true);

    public final String editChanceSyntax = getValue("editchance_syntax", true);
    public final String editChanceDone = getValue("editchance_done", true);
    
    public final List<String> bindCommandHelp = getListValue("bindcommand_help", true);
    public final String bindCommandSyntax = getValue("bindcommand_syntax", true);
    public final String bindCommandRemoved = getValue("bindcommand_removed", true);
    public final String bindCommandAdd = getValue("bindcommand_add", true);
    public final String bindCommandAlreadyAdded = getValue("bindcommand_already_added", true);
    public final String bindCommandCannotRemove = getValue("bindcommand_cannot_remove", true);
    public final String bindCommandInfo = getValue("bindcommand_info", true);
    public final String bindCommandEmpty = getValue("bindcommand_empty", true);

    public final String setVirtualSyntax = getValue("setvirtual_syntax", true);
    public final String setVirtualWarning = getValue("setvirtual_warning", true);
    public final String setVirtualDone = getValue("setvirtual_done", true);

    public final String setLimitedSyntax = getValue("setlimited_syntax", true);
    public final String setLimitedRemoved = getValue("setlimited_removed", true);
    public final String setLimitedDone = getValue("setlimited_done", true);
    
    public final String keyHeader = getValue("key_header", true);
    public final String keyReset = getValue("key_reset", true);
    public final String keyPlayerNotOnline = getValue("key_player_not_online", true);
    public final String keyDoneAddExecutor = getValue("key_done_add_executor", true);
    public final String keyDoneAddTarget = getValue("key_done_add_target", true);
    public final String keyDoneRemoveExecutor = getValue("key_done_remove_executor", true);
    public final String keyDoneRemoveTarget = getValue("key_done_remove_target", true);
    public final String keyDoneSetExecutor = getValue("key_done_set_executor", true);
    public final String keyDoneSetTarget = getValue("key_done_set_target", true);
    public final String keySyntax = getValue("key_syntax", true);

    public final String win1Self = getValue("win_1_self", false);
    public final String win1 = getValue("win_1", false);
    public final String winLimited = getValue("win_limited", false);
    public final String winBroadcast = getValue("win_broadcast", false);
    public final String win = getValue("win", false);
    
    public final String descriptionAddToCrate = getValue("description_addtocrate", false);
    public final String descriptionRemoveFromCrate = getValue("description_removefromcrate", false);
    public final String descriptionEditChance = getValue("description_editchance", false);
    public final String descriptionBindCommand = getValue("description_bindcommand", false);
    public final String descriptionSetLimited = getValue("description_setlimited", false);
    public final String descriptionSetVirtual = getValue("description_setvirtual", false);
    public final String descriptionKey = getValue("description_key", false);
    public final String descriptionGetCrateBlock = getValue("description_getcrateblock", false);
    public final String descriptionRudiCrates = getValue("description_rudicrates", false);
    
    public final String crateOpeningPlaced = getValue("crate_opening_placed", true);
    public final String crateOpeningRemoved = getValue("crate_opening_removed", true);
    public final String opening = getValue("opening", false);
    public final String preview = getValue("preview", false);
  
    public final String mysqlConnected = getValue("mysql_connected", true);
    public final String mysqlAlreadyConnected = getValue("mysql_already_connected", true);
    public final String mysqlCouldNotConnect = getValue("mysql_could_not_connect", true);
    public final String mysqlDisabled = getValue("mysql_disabled", true);
    public final String uuidFetcherExceptionUUID = getValue("uuidfetcher_exeption_uuid", true);
    public final String uuidFetcherExceptionName  = getValue("uuidfetcher_exeption_name", true);
    
    // Items 109-20 89
    public final String backItemName = getValue("item_back_name", false);
    public final String closeMenuItemName = getValue("item_closemenu_name", false);
    public final String previousPageItemName = getValue("item_previouspage_name", false);
    public final String currentPageItemName = getValue("item_currentpage_name", false);
    public final String nextPageItemName = getValue("item_nextpage_name", false);
    
    public final String itemPreviewID = getValue("item_preview_id", false);
    public final String itemPreviewChance = getValue("item_preview_chance", false);
    
    public final List<String> itemCrateLore = getListValue("item_crate_lore", false);
    public final String blockCrateName = getValue("block_crate_name", false);
    public final List<String> blockCrateLore = getListValue("block_crate_lore", false);
}
