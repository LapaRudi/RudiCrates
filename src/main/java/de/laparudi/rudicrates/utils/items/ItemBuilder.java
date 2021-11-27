package de.laparudi.rudicrates.utils.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item;
    }

    public ItemBuilder setName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder setLore(String... lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(String... lines) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.addAll(Arrays.asList(lines));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder invisibleEnchant(boolean configValue) {
        if(!configValue) return this;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder unique() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLimited(int limit) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.addAll(Arrays.asList("§8§m─────────────────", "§c§lLimitiert §7» §6Noch §a" + limit + "§6 verfügbar"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack toItem() {
        return item;
    }

}
