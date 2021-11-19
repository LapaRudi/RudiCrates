// https://gist.github.com/Jofkos/d0c469528b032d820f42

package de.laparudi.rudicrates.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UUIDFetcher {

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";

    private static final Map<String, UUID> uuidCache = new HashMap<>();
    private static final Map<UUID, String> nameCache = new HashMap<>();

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private String name;
    private UUID id;

    /**
     * Fetches the uuid asynchronously and passes it to the consumer
     *
     * @param name   The name
     * @param action Do what you want to do with the uuid her
     */
    public static void getUUID(String name, Consumer<UUID> action) {
        pool.execute(() -> action.accept(getUUID(name)));
    }

    public static UUID getUUID(String name) {
        name = name.toLowerCase();
        if (uuidCache.containsKey(name)) return uuidCache.get(name);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(UUID_URL + name).openConnection();
            connection.setReadTimeout(2000);
            UUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

            uuidCache.put(name, data.id);
            nameCache.put(data.id, data.name);
            return data.id;

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§cFehler beim abfragen der UUID von: §4" + name);
        }

        return null;
    }

    /**
     * Fetches the name asynchronously and passes it to the consumer
     *
     * @param uuid   The uuid
     * @param action Do what you want to do with the name her
     */
    public static void getName(UUID uuid, Consumer<String> action) {
        pool.execute(() -> action.accept(getName(uuid)));
    }

    /**
     * Fetches the name synchronously and returns it
     *
     * @param uuid The uuid
     * @return The name
     */
    public static String getName(UUID uuid) {
        if (nameCache.containsKey(uuid)) return nameCache.get(uuid);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            connection.setReadTimeout(2000);
            UUIDFetcher[] nameHistory = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
            UUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];

            uuidCache.put(currentNameData.name.toLowerCase(), uuid);
            nameCache.put(uuid, currentNameData.name);
            return currentNameData.name;

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(Messages.PREFIX + "§cFehler beim abfragen des Namens von: §4" + uuid);
        }

        return null;
    }
}