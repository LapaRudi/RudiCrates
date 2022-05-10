package de.laparudi.rudicrates.utils.version;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionUtils {
    
    ItemStack getPlayersItemInHand(final Player player);
    ItemStack getConfigItem(final String path);

    BaseComponent[] builder(final String text, final String hoverText, final String displayText);
    BaseComponent[] component(final String syntax, final String command, final String description);
    
    void formatConfig();
}
