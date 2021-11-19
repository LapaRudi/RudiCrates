package de.laparudi.rudicrates.utils.version;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface VersionUtils {

    // https://stackoverflow.com/questions/25837873
    void updateTaskPeriod(int id, long period);
    
    ItemStack getPlayersItemInHand(Player player);

    ComponentBuilder component(String syntax, String command, String description);
    
    Sound pling();
    Sound winSound();
    Sound blazeShoot();
    
    void openAnimation(Player player, Location location);
    
    void closeAnimation(Player player, Location location);
    
}
