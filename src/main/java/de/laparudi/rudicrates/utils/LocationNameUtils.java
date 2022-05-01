package de.laparudi.rudicrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationNameUtils {

    public static String toLocationString(Location location) {
        if (location.getWorld() == null) throw new NullPointerException("World not found");
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
    
    public static Location fromLocationString(String locationString) {
        String[] location = locationString.split(" ");
        if (Bukkit.getWorld(location[0]) == null) throw new NullPointerException("World '" + location[0] + "' not found");
        return new Location(Bukkit.getWorld(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]));
    }
}
