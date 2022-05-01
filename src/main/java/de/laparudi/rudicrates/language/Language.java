package de.laparudi.rudicrates.language;

import de.laparudi.rudicrates.RudiCrates;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Language {
    
    private static final Map<String, String> messagesMap = new HashMap<>();
    private static final Map<String, List<String>> listMap = new HashMap<>();
    
    public static void send(final CommandSender sender, final BaseComponent[] components, final String configKey) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return;
        
        if (!(sender instanceof Player)) {
            final StringBuilder builder = new StringBuilder();
            Arrays.stream(components).forEach(component -> builder.append(component.toLegacyText()));
            sender.sendMessage(builder.toString());
            return;
        }

        ((Player) sender).spigot().sendMessage(components);
    }
    
    public static void send(final CommandSender sender, final String configKey) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return;
        sender.sendMessage(get(configKey));
    }

    public static void send(final CommandSender sender, final String configKey, final String placeholder, final String replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return;
        sender.sendMessage(get(configKey, placeholder, replaceTo));
    }

    public static void send(final CommandSender sender, final String configKey, final String[] placeholders, final String[] replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return;
        sender.sendMessage(get(configKey, placeholders, replaceTo));
    }
    
    public static String get(final String configKey) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return prefix + ChatColor.translateAlternateColorCodes('&', messagesMap.get(configKey));
    }

    public static String get(final String configKey, final String placeholder, final String replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return replace(messagesMap.get(configKey), placeholder, replaceTo, true);
    }
    
    public static String get(final String configKey, final String[] placeholders, final String[] replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return replace(messagesMap.get(configKey), placeholders, replaceTo, true);
    }
    
    public static List<String> getList(final String configKey) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return addPrefixToList(listMap.get(configKey));
    }
    
    public static List<String> getList(final String configKey, final String placeholder, final String replaceTo) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return replaceList(listMap.get(configKey), placeholder, replaceTo, true);
    }

    public static List<String> getList(final String configKey, final String[] placeholders, final String[] replaceTo) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return replaceList(listMap.get(configKey), placeholders, replaceTo, true);
    }

    public static String withoutPrefix(final String configKey) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return ChatColor.translateAlternateColorCodes('&', messagesMap.get(configKey));
    }

    public static String withoutPrefix(final String configKey, final String placeholder, final String replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return replace(messagesMap.get(configKey), placeholder, replaceTo, false);
    }

    public static String withoutPrefix(final String configKey, final String[] placeholders, final String[] replaceTo) {
        if (!messagesMap.containsKey(configKey) || messagesMap.get(configKey).isBlank()) return "<null>";
        return replace(messagesMap.get(configKey), placeholders, replaceTo, false);
    }

    public static List<String> listWithoutPrefix(final String configKey) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return listMap.get(configKey);
    }

    public static List<String> listWithoutPrefix(final String configKey, final String placeholder, final String replaceTo) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return replaceList(listMap.get(configKey), placeholder, replaceTo, false);
    }

    public static List<String> listWithoutPrefix(final String configKey, final String[] placeholders, final String[] replaceTo) {
        if (!listMap.containsKey(configKey) || listMap.get(configKey).isEmpty()) return Collections.singletonList("<null>");
        return replaceList(listMap.get(configKey), placeholders, replaceTo, false);
    }
    
    public void loadMessages() {
        messagesMap.clear();
        listMap.clear();
        
        RudiCrates.getPlugin().getMessagesConfig().getKeys(true).forEach(key -> {
            if (RudiCrates.getPlugin().getMessagesConfig().isList(key)) {
                listMap.put(key, translateList(RudiCrates.getPlugin().getMessagesConfig().getStringList(key)));
            } else
                messagesMap.put(key, translate(RudiCrates.getPlugin().getMessagesConfig().getString(key)));
        });
    }

    private static String replace(String input, final String placeholder, final String replaceTo, final boolean withPrefix) {
        if (input == null) return "<null>";
        if (input.contains(placeholder)) {
            input = input.replace(placeholder, replaceTo);
        }

        return withPrefix ? prefix + input : input;
    }
    
    private static String replace(String input, final String[] placeholders, final String[] replaceTo, final boolean withPrefix) {
        if (input == null) return "<null>";
        for (int i = 0; i < placeholders.length; i++) {
            if (placeholders[i] == null || replaceTo[i] == null) break;
            
            if (input.contains(placeholders[i])) {
                input = input.replace(placeholders[i], replaceTo[i]);
            }
        }
        
        return withPrefix ? prefix + input : input;
    }

    private static List<String> addPrefixToList(final List<String> input) {
        input.stream().filter(line -> !line.startsWith(prefix)).forEach(line -> input.set(input.indexOf(line), prefix + line));
        return input;
    }
    
    private static List<String> replaceList(final List<String> input, final String placeholder, final String replaceTo, final boolean withPrefix) {
        if (input == null) return Collections.singletonList("<null>");
        final List<String> returnList = new ArrayList<>(input);
        
        returnList.forEach(line -> {
            if (!line.contains(placeholder)) return;
            final String newLine = withPrefix ? prefix + line.replace(placeholder, replaceTo) : line.replace(placeholder, replaceTo);
            returnList.set(returnList.indexOf(line), newLine);
        });
        
        return returnList;
    }
    
    private static List<String> replaceList(final List<String> input, final String[] placeholders, final String[] replaceTo, final boolean withPrefix) {
        if (input == null) return Collections.singletonList("<null>");
        final List<String> returnList = new ArrayList<>(input);

        returnList.forEach(line -> {
            for (int i = 0; i < placeholders.length; i++) {
                if (!line.contains(placeholders[i])) return;
                final String newLine = withPrefix ? prefix + line.replace(placeholders[i], replaceTo[i]) : line.replace(placeholders[i], replaceTo[i]);
                returnList.set(returnList.indexOf(line), newLine);
            }
        });

        return returnList;
    }
    
    private static String translate(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    
    private static List<String> translateList(final List<String> input) {
        input.forEach(line -> input.set(input.indexOf(line), translate(line)));
        return input;
    }
    
    private static final @Getter String prefix = Bukkit.getBukkitVersion().contains("1.16") ? RudiCrates.getPlugin().getTranslationUtils().hexColorString(
            new String[] { "#4F4F4F", "#b50456", "#b82c7c", "#b349a1", "#a563c1", "#907bdc", "#7392f0", "#50a6fc", "#24b9ff", "#00caff", "#14daff", "#4F4F4F" },
            new Character[] { '[', 'R', 'u', 'd', 'i', 'C', 'r', 'a', 't', 'e', 's', ']', ' ' }) : "§8[§cRudiCrates§8] §r";
}
