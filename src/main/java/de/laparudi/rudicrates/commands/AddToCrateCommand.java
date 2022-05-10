package de.laparudi.rudicrates.commands;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.crate.Crate;
import de.laparudi.rudicrates.crate.CrateUtils;
import de.laparudi.rudicrates.language.Language;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class AddToCrateCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!(sender.hasPermission("rudicrates.addtocrate")) || !(sender instanceof Player)) {
            Language.send(sender, "player.no_permission");
            return true;
        }
        
        if (args.length != 2) {
            Language.send(sender, "commands.addtocrate.syntax");
            return true;
        }
        
        final Player player = (Player) sender;
        final ItemStack itemInHand = RudiCrates.getPlugin().getVersionUtils().getPlayersItemInHand(player);
        Crate crate;
        double chance;
        
        try {
            crate = Crate.getByName(args[0]);
        } catch (final NullPointerException exception) {
            Language.send(player, "crate.unknown");
            return true;
        }
        
        if (itemInHand.getType() == Material.AIR) {
            Language.send(player, "player.empty_hand");
            return true;
        }
        
        try {
            chance = Double.parseDouble(args[1].replace(',', '.'));
        } catch (final NumberFormatException exception) {
            Language.send(player, "numbers.invalid");
            return true;
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
        for (final String key : config.getKeys(false)) {
            final ItemStack item = config.getItemStack(key + ".item");
            if (item == null || item.getAmount() != itemInHand.getAmount()) continue;
            
            if (item.isSimilar(itemInHand)) {
                Language.send(player, "commands.addtocrate.already_in");
                return true;
            }
        }
        
        final int limit = RudiCrates.getPlugin().getConfig().getInt("max_items_per_crate");
        if (config.getKeys(false).size() >= limit) {
            Language.send(sender, "commands.addtocrate.full", "%limit%", String.valueOf(limit));
            if (sender.isOp() || sender.hasPermission("*")) Language.send(sender, "commands.addtocrate.full_addon");
            return true;
        }

        final int keySize = config.getKeys(false).size() == 0 ? 2 : config.getKeys(false).size() +1;
        for (int id = 0; id < keySize; id++) {
            if (!config.getKeys(false).contains(String.valueOf(id))) {
                config.set(id + ".item", itemInHand);
                config.set(id + ".chance", chance);
                break;
            }
        }
        
        try {
            config.save(crate.getFile());
            Language.send(player, "commands.addtocrate.done", new String[] { "%crate%", "%chance%" }, new String[] { crate.getName(), String.valueOf(chance) });
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        if (!sender.hasPermission("rudicrates.addtocrate")) return Collections.emptyList();
        final List<String> complete = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], CrateUtils.getCrateNames(), complete);
            return complete;
        }

        return Collections.emptyList();
    }
}

/*
        - performance testing - /addtocrate all <crate> <item amount>
        
        if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("all")) return true;
            Crate crate;
            int amount;
            
            try {
                crate = Crate.getByName(args[1]);
                amount = Integer.parseInt(args[2]);
            } catch (final NullPointerException | NumberFormatException exception) {
                Language.send(sender, "crates.unknown");
                return true;
            }
            
            this.fillCrate(crate, amount);
            return true;
        }
       
      private int nextID(final Crate crate) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
        
        for (int i = 0; i < config.getKeys(false).size() +1; i++) {
            final String idString = String.valueOf(i);
            
            if (!config.getKeys(false).contains(idString)) {
                return i;
            }
        }
        
        return 0;
    }
        
    private ItemStack randomItem() {
        final int random = ThreadLocalRandom.current().nextInt(Material.values().length);
        final Material material = Material.values()[random];
        if (material.isAir() || !material.isBlock() || !material.isItem()) return this.randomItem();
        return new ItemBuilder(Material.values()[random]).setLore("§7" + UUID.randomUUID()).toItem();
    }
    
    private void fillCrate(final Crate crate, final int amount) {
        final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
        final int id = this.nextID(crate);
        
        Bukkit.getScheduler().runTaskAsynchronously(RudiCrates.getPlugin(), () -> {
            for (int i = 0; i < amount; i++) {
                final ItemStack item = this.randomItem();

                if (!config.getKeys(false).contains(String.valueOf(id + i))) {
                    config.set((id + i) + ".item", item);
                    config.set((id + i) + ".chance", 7.5);
                    Bukkit.broadcastMessage("added id " + (id + i) + " > " + item.getType() + " [" + i + "]");
                }
            }
            
            try {
                config.save(crate.getFile());
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        });
    }

 */
