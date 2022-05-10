package de.laparudi.rudicrates.language;

import de.laparudi.rudicrates.RudiCrates;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import java.util.*;

public class TranslationUtils {
    
    private static final Map<Material, TranslatableComponent> translations = new HashMap<>();

    public final BaseComponent componentPrefix() {
        if (RudiCrates.getPlugin().getReflection().getVersionInt() >= 16) {
            return this.arrayToSingleComponent(new ComponentBuilder("[").color(ChatColor.of("#4F4F4F"))
                    .append("R").color(ChatColor.of("#b50456")).append("u").color(ChatColor.of("#b82c7c")).append("d").color(ChatColor.of("#b349a1"))
                    .append("i").color(ChatColor.of("#a563c1")).append("C").color(ChatColor.of("#907bdc")).append("r").color(ChatColor.of("#7392f0"))
                    .append("a").color(ChatColor.of("#50a6fc")).append("t").color(ChatColor.of("#24b9ff")).append("e").color(ChatColor.of("#00caff"))
                    .append("s").color(ChatColor.of("#14daff")).append("] ").color(ChatColor.of("#4F4F4F")).create());
        }
        return new TextComponent("§8[§cRudiCrates§8] §r");
    }
    
    public String hexColorString(final String[] hexCodes, @Nonnull final Character[] chars) {
        final StringBuilder builder = new StringBuilder();
        int count = 0;

        for (final Character character : chars) {
            builder.append(ChatColor.of(hexCodes[count])).append(character);
            count++;
            if (count >= hexCodes.length) count = 0;
        }

        return builder.toString();
    }
    
    public void setupTranslations() {
        if (RudiCrates.getPlugin().isLegacy()) return;
        
        for (final Material material : Material.values()) {
            final String name = material.name().toLowerCase();
            if (name.contains("wall_banner")) continue;

            if (material.isBlock()) {
                translations.put(material, new TranslatableComponent("block.minecraft." + name));
                
            } else if (material.isItem()) {
                if (translations.containsKey(material)) continue;
                translations.put(material, new TranslatableComponent("item.minecraft." + name));
            }
        }
    }
    
    public TranslatableComponent getTranslation(final Material material) {
        if (RudiCrates.getPlugin().isLegacy()) {
            return new TranslatableComponent(WordUtils.capitalize(material.name().toLowerCase().replace("_", " ")).trim());
        }
        
        return translations.get(material);
    }

    public static List<String> translateChatColor(final List<String> list) {
        final List<String> newList = new ArrayList<>();
        list.forEach(string -> newList.add(ChatColor.translateAlternateColorCodes('&', string)));
        return newList;
    }

    private BaseComponent arrayToSingleComponent(final BaseComponent[] array) {
        final BaseComponent component = new TextComponent();
        Arrays.stream(array).forEach(component::addExtra);
        return component;
    }
    
    public BaseComponent getWinMessage(final MessageType type, final BaseComponent display, final String amount, final double chance, final String playerName, final String crateDisplay, final int limit) {
        final BaseComponent component = new TextComponent(componentPrefix());
        switch (type) {
            case SELF:
                component.addExtra(Language.withoutPrefix("crate.win.self", "%amount%", amount));
                component.addExtra(display);
                component.addExtra(Language.withoutPrefix("crate.win.chance", "%chance%", String.valueOf(chance)));
                return component;
                
            case BROADCAST:
                component.addExtra(Language.withoutPrefix("crate.win.other", new String[] { "%player%", "%amount%" }, new String[] { playerName, amount }));
                component.addExtra(display);
                component.addExtra(" " + Language.withoutPrefix("crate.win.broadcast", new String[] { "%player%", "%crate%", "%chance%" }, new String[] { playerName, crateDisplay, String.valueOf(chance) }));
                return component;
                
            case SELF_LIMITED:
                component.addExtra(Language.withoutPrefix("crate.win.self", "%amount%", amount));
                component.addExtra(display);
                component.addExtra(" " + Language.withoutPrefix("crate.win.limited", new String[] {"%crate%", "%chance%", "%limit%" }, new String[] { crateDisplay, String.valueOf(chance), String.valueOf(limit) }));
                return component;
                
            case BROADCAST_LIMITED:
                component.addExtra(Language.withoutPrefix("crate.win.other", new String[] { "%player%", "%amount%" }, new String[] { playerName, amount }));
                component.addExtra(display);
                component.addExtra(" " + Language.withoutPrefix("crate.win.limited", new String[] { "%crate%", "%chance%", "%limit%" }, new String[] { crateDisplay, String.valueOf(chance), String.valueOf(limit) }));
                return component;
        }
        return component;
    }
    
    public enum MessageType {
        SELF,
        BROADCAST,
        SELF_LIMITED,
        BROADCAST_LIMITED
    }
}
