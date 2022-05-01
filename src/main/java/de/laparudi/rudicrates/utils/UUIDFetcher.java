// https://gist.github.com/Jofkos/d0c469528b032d820f42

package de.laparudi.rudicrates.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UUIDFetcher {
    
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";

    private static final Map<String, UUID> uuidCache = new HashMap<>();
    private static final Map<UUID, String> nameCache = new HashMap<>();

    private final String name;
    private final UUID id;

    public UUIDFetcher(final String name, final UUID id) {
        this.name = name;
        this.id = id;
    }

    public static UUID getUUID(final String username) {
        if (uuidCache.containsKey(username.toLowerCase())) {
            return uuidCache.get(username.toLowerCase());
        }
        
        try {
            return getAsyncUUID(username).get(5000, TimeUnit.MILLISECONDS);
        } catch (final ExecutionException | InterruptedException | TimeoutException exception) {
            Language.send(Bukkit.getConsoleSender(), "uuidfetcher.exception_uuid", "%player%", username);
            return null;
        }
    }
    
    private static CompletableFuture<UUID> getAsyncUUID(final String username) {
        final String name = username.toLowerCase();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(UUID_URL + name).openConnection();
                connection.setReadTimeout(5000);
                final UUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

                uuidCache.put(name, data.id);
                nameCache.put(data.id, data.name);
                return data.id;

            } catch (final Exception exception) {
                Language.send(Bukkit.getConsoleSender(), "uuidfetcher.exception_uuid", "%player%", name);
                return null;
            }
        });
    }
    
    public static String getName(final UUID uuid) {
        if (nameCache.containsKey(uuid)) return nameCache.get(uuid);

        CompletableFuture.supplyAsync(() -> {
            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
                connection.setReadTimeout(2000);
                final UUIDFetcher[] nameHistory = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
                final UUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];

                uuidCache.put(currentNameData.name.toLowerCase(), uuid);
                nameCache.put(uuid, currentNameData.name);
                return currentNameData.name;

            } catch (final Exception exception) {
                Language.send(Bukkit.getConsoleSender(), "uuidfetcher.exception_name", "%player%", uuid.toString());
            }
            return null;
        });
        return null;
    }
}