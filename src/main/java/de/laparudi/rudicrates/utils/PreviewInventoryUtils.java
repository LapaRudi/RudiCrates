package de.laparudi.rudicrates.utils;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PreviewInventoryUtils {
    
    public static final Map<String, List<Inventory>> cratePreviewInventoriesMap = new HashMap<>();
    private Inventory menuInventory;
    
    private void fillInventories(final List<Inventory> inventories, final List<ItemStack> items) {
        int invCount = 0;
        int slot = 0;

        for (final ItemStack item : items) {
            if (slot == 45) {
                invCount++;
                slot = 0;
            }

            inventories.get(invCount).setItem(slot, item);
            slot++;
        }

        for (final Inventory inventory : inventories) {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, RudiCrates.getPlugin().getItemManager().blackGlass);
                }
            }
        }
    }
    
    public void loadPreviewInventories() {
        Bukkit.getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            for (final Crate crate : CrateUtils.getCrates()) {

                final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
                final List<Inventory> inventories = new ArrayList<>();
                final List<ItemStack> itemList = new ArrayList<>();
                final int inventoryAmount = (int) Math.ceil(crateConfig.getKeys(false).size() / 45.0);

                for (int i = 0; i < inventoryAmount; i++) {
                    final Inventory inventory = Bukkit.createInventory(null, 54, Language.withoutPrefix("inventories.preview", "%crate%", crate.getDisplayname()));
                    inventories.add(inventory);
                }

                crateConfig.getKeys(false).forEach(key -> {
                    final String id = Language.withoutPrefix("items.preview_id", "%id%", key);
                    final String chance = Language.withoutPrefix("items.preview_chance", "%chance%", String.valueOf(crateConfig.getDouble(key + ".chance")));
                    ItemStack item = new ItemBuilder(crateConfig.getItemStack(key + ".item")).addLore("", chance).toItem();

                    if (crateConfig.get(key + ".limited") != null) {
                        item = new ItemBuilder(item).addLimited(crateConfig.getInt(key + ".limited")).addLore("").toItem();
                    }

                    item = new ItemBuilder(item).addLore(id).toItem();
                    itemList.add(item);
                });

                inventories.forEach(inventory -> {
                    for (int i = 46; i < 54; i++) {
                        inventory.setItem(i, RudiCrates.getPlugin().getItemManager().blueGlass);
                    }

                    inventory.setItem(45, RudiCrates.getPlugin().getItemManager().back);
                    inventory.setItem(48, RudiCrates.getPlugin().getItemManager().previousPage);
                    inventory.setItem(49, RudiCrates.getPlugin().getItemManager().currentPage(inventories.indexOf(inventory) + 1));
                    inventory.setItem(50, RudiCrates.getPlugin().getItemManager().nextPage);
                });

                cratePreviewInventoriesMap.put(crate.getName(), inventories);
                fillInventories(cratePreviewInventoriesMap.get(crate.getName()), itemList);
            }
        });
    }

    public void openCrateMenu(final Player player) {
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        
        Arrays.stream(CrateUtils.getCrates()).forEach(crate -> {
            int slot = config.getInt("crates." + crate.getName() + ".slot");
            if (slot > menuInventory.getSize()) {
                Language.send(Bukkit.getConsoleSender(), "crate.incorrect_slot_value", "%crate%", crate.getName());
                return;
            }
            
            menuInventory.setItem(--slot, RudiCrates.getPlugin().getItemManager().getCrateItem(player, crate));
        });
        player.openInventory(menuInventory);
    }
    
    public void setupCrateMenu() {
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        String inventoryTitle = config.getString("inventories.menu_title");
        final int inventorySize = config.getInt("inventories.menu_rows") *9;
        final int closeItemSlot = config.getInt("items.close_slot") -1;
        final boolean useCloseItem = config.getBoolean("items.use_close");
        final boolean useFillItem = config.getBoolean("items.use_fill");
        
        if(inventoryTitle == null) inventoryTitle = "RudiCrates";
        menuInventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&', inventoryTitle));

        if(useFillItem) {
            for (int i = 0; i < menuInventory.getSize(); i++) {
                if (menuInventory.getItem(i) == null) {
                    menuInventory.setItem(i, RudiCrates.getPlugin().getItemManager().fill);
                }
            }
        }

        if (!useCloseItem) return;
        if (closeItemSlot >= 0 && closeItemSlot <= (inventorySize -1) ) {
            menuInventory.setItem(closeItemSlot, RudiCrates.getPlugin().getItemManager().close);
        } else
            Language.send(Bukkit.getConsoleSender(), "crate.incorrect_close_slot", "%highest%", String.valueOf(inventorySize));
    }
}
