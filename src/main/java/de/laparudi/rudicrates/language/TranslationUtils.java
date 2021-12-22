package de.laparudi.rudicrates.language;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationUtils {
    
    private static final Map<Material, TranslatableComponent> translations = new HashMap<>();

    public String hexColorString(String[] hexCodes, @Nonnull Character[] chars) {
        StringBuilder builder = new StringBuilder();
        int count = 0;

        for (Character character : chars) {
            builder.append(ChatColor.of(hexCodes[count])).append(character);
            count++;
            if (count >= hexCodes.length) count = 0;
        }

        return builder.toString();
    }
    
    public void setupTranslations() {
        for(Material material : Material.values()) {
            final String name = material.name().toLowerCase();
            if(name.contains("wall_banner")) continue;

            if(material.isBlock()) {
                translations.put(material, new TranslatableComponent("block.minecraft." + name));
                
            } else if(material.isItem()) {
                if(translations.containsKey(material)) continue;
                translations.put(material, new TranslatableComponent("item.minecraft." + name));
            }
        }
    }
    
    public TranslatableComponent getTranslation(Material material) {
        return translations.get(material);
    }

    public static List<String> translateChatColor(List<String> list) {
        List<String> newList = new ArrayList<>();
        list.forEach(string -> newList.add(ChatColor.translateAlternateColorCodes('&', string)));
        return newList;
    }
}