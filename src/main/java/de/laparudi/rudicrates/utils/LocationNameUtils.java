package de.laparudi.rudicrates.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.util.UUID;

public class LocationNameUtils extends TypeAdapter<UUID> {

    public static String toLocationString(final Location location) {
        if (location.getWorld() == null) throw new NullPointerException("World not found");
        return location.getWorld().getName() + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
    
    public static Location fromLocationString(final String locationString) {
        final String[] location = locationString.split(" ");
        if (Bukkit.getWorld(location[0]) == null) throw new NullPointerException("World '" + location[0] + "' not found");
        return new Location(Bukkit.getWorld(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]));
    }

    public void write(final JsonWriter out, final UUID value) throws IOException {
        out.value(fromUUID(value));
    }

    public UUID read(final JsonReader in) throws IOException {
        return fromString(in.nextString());
    }

    public static String fromUUID(final UUID value) {
        return value.toString().replace("-", "");
    }

    public static UUID fromString(final String input) {
        return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }
}
