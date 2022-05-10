package de.laparudi.rudicrates.listeners.inventory;

import com.cryptomorin.xseries.XSound;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectCrateBlockListener implements Listener {

    @EventHandler
    public void onBlockSelect(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!event.getView().getTitle().contains(Language.withoutPrefix("inventories.available_crate_blocks"))) return;
        
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        event.setCancelled(true);
        
        final ItemStack item = event.getCurrentItem();
        if (item == null) return;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) return;
        
        final Player player = (Player) event.getWhoClicked();
        player.getInventory().addItem(item);
        XSound.play(player, "BLOCK_NOTE_BLOCK_HARP");
        Language.send(player, "commands.getcrateblock.done");
    }
}
