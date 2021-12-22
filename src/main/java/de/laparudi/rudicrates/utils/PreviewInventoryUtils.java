package de.laparudi.rudicrates.utils;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PreviewInventoryUtils {
    
    public static Map<String, List<Inventory>> cratePreviewInventoriesMap = new HashMap<>();
    private Inventory menuInventory;
    
    private void fillInventories(List<Inventory> inventories, List<ItemStack> items) {
        int invCount = 0;
        int slot = 0;

        for (ItemStack item : items) {
            if (slot == 45) {
                invCount++;
                slot = 0;
            }

            inventories.get(invCount).setItem(slot, item);
            slot++;
        }

        for(Inventory inventory : inventories) {
            for(int i = 0; i < inventory.getSize(); i++) {
                if(inventory.getItem(i) == null) {
                    inventory.setItem(i, RudiCrates.getPlugin().getItemManager().grayGlass);
                }
            }
        }
    }
    
    public void loadPreviewInventories() {
        for (Crate crate : RudiCrates.getPlugin().getCrateUtils().getCrates()) {

            final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
            final List<Inventory> inventories = new ArrayList<>();
            final List<ItemStack> itemList = new ArrayList<>();
            int inventoryAmount = (int) Math.ceil(config.getKeys(false).size() / 45.0);

            for (int i = 0; i < inventoryAmount; i++) {
                Inventory inventory = Bukkit.createInventory(null, 54, crate.getDisplayname() + " " + RudiCrates.getPlugin().getLanguage().preview);
                inventories.add(inventory);
            }

            config.getKeys(false).forEach(key -> {
                String id = RudiCrates.getPlugin().getLanguage().itemPreviewID.replace("%id%", key);
                String chance = RudiCrates.getPlugin().getLanguage().itemPreviewChance.replace("%chance%", String.valueOf(config.getDouble(key + ".chance")));
                ItemStack item = new ItemBuilder(config.getItemStack(key + ".item")).addLore("", chance).toItem();

                if (config.get(key + ".limited") != null) {
                    item = new ItemBuilder(item).addLimited(config.getInt(key + ".limited")).addLore("").toItem();
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
    }

    public void openCrateMenu(Player player) {
        final FileConfiguration config = RudiCrates.getPlugin().getConfig();
        
        Arrays.stream(RudiCrates.getPlugin().getCrateUtils().getCrates()).forEach(crate -> {
            int slot = config.getInt("crates." + crate.getName() + ".slot");
            if(slot > menuInventory.getSize()) {
                Bukkit.getConsoleSender().sendMessage(RudiCrates.getPlugin().getLanguage().incorrectSlotValue.replace("%crate%", crate.getName()));
                return;
            }
            
            menuInventory.setItem(--slot, RudiCrates.getPlugin().getItemManager().getCrateItem(player, crate));
        });
        player.openInventory(menuInventory);
    }
    
    public void setupCrateMenu() {
        String inventoryTitle = RudiCrates.getPlugin().getConfig().getString("inventorytitle");
        final int inventorySize = RudiCrates.getPlugin().getConfig().getInt("inventoryrows") *9;
        final int closeItemSlot = RudiCrates.getPlugin().getConfig().getInt("closeinventoryslot") -1;
        final boolean useCloseItem = RudiCrates.getPlugin().getConfig().getBoolean("usecloseinventory");
        final boolean useWallItem = RudiCrates.getPlugin().getConfig().getBoolean("usefillitem");
        
        if(inventoryTitle == null) inventoryTitle = "RudiCrates";
        menuInventory = Bukkit.createInventory(null, inventorySize, ChatColor.translateAlternateColorCodes('&', inventoryTitle));

        if(useWallItem) {
            Material material = Material.getMaterial(Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("fillitem.material")));
            String name = RudiCrates.getPlugin().getConfig().getString("fillitem.name");
            boolean enchanted = RudiCrates.getPlugin().getConfig().getBoolean("fillitem.enchanted");
            if(material == null) material = Material.GRAY_STAINED_GLASS_PANE;
            if(name == null) name = " ";
            
            final ItemStack wallItem = new ItemBuilder(material).setName(ChatColor.translateAlternateColorCodes('&', name)).invisibleEnchant(enchanted).toItem();
            for (int i = 0; i < menuInventory.getSize(); i++) {
                if (menuInventory.getItem(i) == null) {
                    menuInventory.setItem(i, wallItem);
                }
            }
        }

        if (!useCloseItem) return;
        if (closeItemSlot >= 0 && closeItemSlot <= (inventorySize -1) ) {
            menuInventory.setItem(closeItemSlot, RudiCrates.getPlugin().getItemManager().closeMenu);
        } else
            Bukkit.getConsoleSender().sendMessage( RudiCrates.getPlugin().getLanguage().incorrectCloseInventorySlot.replace("%highest%", String.valueOf(inventorySize)));
    }
}
