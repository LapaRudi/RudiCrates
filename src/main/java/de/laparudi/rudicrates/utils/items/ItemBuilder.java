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
    private final ItemMeta meta;

    public ItemBuilder(final Material material) {
        this.item = new ItemStack(material);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder(final ItemStack item) {
        this.item = item;
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder setName(final String name) {
        if (meta == null) return this;
        meta.setDisplayName(name);
        return this;
    }
    
    public ItemBuilder setAmount(final int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(final List<String> lore) {
        if (meta == null || lore == null) return this;
        meta.setLore(lore);
        return this;
    }
    
    public ItemBuilder setLore(final String... lore) {
        if (meta == null) return this;
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder addLore(final String... lines) {
        if (meta == null) return this;
        final List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.addAll(Arrays.asList(lines));
        meta.setLore(lore);
        return this;
    }
    
    @Deprecated
    public ItemBuilder setDurability(final int durability) {
        if (durability == 0) return this;
        item.setDurability((short) durability);
        return this;
    }

    public ItemBuilder enchant(final Enchantment enchantment, int level) {
        if (meta == null) return this;
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder invisibleEnchant(final boolean configValue) {
        if(!configValue || meta == null) return this;
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder unique() {
        if (meta == null) return this;
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemBuilder addLimited(final int limit) {
        if (meta == null) return this;
        final List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.addAll(Arrays.asList("§8§m─────────────────", "§c§lLimitiert §7» §6Noch §a" + limit + "§6 verfügbar"));
        meta.setLore(lore);
        return this;
    }

    public ItemStack toItem() {
        item.setItemMeta(meta);
        return item;
    }
}
