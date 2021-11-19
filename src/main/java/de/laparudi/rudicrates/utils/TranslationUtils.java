package de.laparudi.rudicrates.utils;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class TranslationUtils {

    private static final Map<Material, TranslatableComponent> translations = new HashMap<>();
    
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
}
