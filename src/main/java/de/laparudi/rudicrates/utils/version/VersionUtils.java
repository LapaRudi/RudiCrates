package de.laparudi.rudicrates.utils.version;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionUtils {

    // https://stackoverflow.com/questions/25837873
    void updateTaskPeriod(int id, long period);
    
    ItemStack getPlayersItemInHand(final Player player);
    ItemStack getConfigItem(final String path);

    BaseComponent[] builder(final String text, final String hoverText, final String displayText);
    BaseComponent[] component(final String syntax, final String command, final String description);
    
    Sound pling();
    Sound winSound();
    Sound blazeShoot();
    
    void openAnimation(final Player player, final Location location);
    
    void closeAnimation(final Player player, final Location location);
    
    void formatConfig();
}
