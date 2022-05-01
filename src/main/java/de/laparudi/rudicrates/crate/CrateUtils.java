package de.laparudi.rudicrates.crate;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.data.Bundle;
import de.laparudi.rudicrates.language.Language;
import de.laparudi.rudicrates.language.TranslationUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class CrateUtils {

    public static final List<UUID> currentlyOpening = new ArrayList<>();
    public static final List<ItemStack> keyItems = new ArrayList<>();
    public static final Map<UUID, Location> inCrateMenu = new HashMap<>();
    private static final Map<String, Double> chancesResult = new HashMap<>();
    
    private static final @Getter List<String> crateNames = new ArrayList<>();
    private static @Getter Crate[] crates;
    
    public static void loadCrates() {
        final List<Crate> list = new ArrayList<>();
        final ConfigurationSection section = RudiCrates.getPlugin().getConfig().getConfigurationSection("crates");
        if (section == null) return;
        
        section.getKeys(false).forEach(key -> {
            try {
                final Crate crate = Crate.getByName(key);
                crateNames.add(key);
                list.add(crate);
                keyItems.add(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1));

            } catch (final NullPointerException exception) {
                Language.send(Bukkit.getConsoleSender(), "crates.incomplete", "%crate%", key);
            }
        });
        
        crates = list.toArray(Crate[]::new);
    }

    @Nonnull
    private Bundle<ItemStack, Integer> getRandomItem(final Crate crate) {
        final double random = ThreadLocalRandom.current().nextDouble(chancesResult.get(crate.getName()));

        for (final Map.Entry<ItemStack, Double[]> entry : Crate.getMap().get(crate.getName()).entrySet()) {
            final Double[] doubles = entry.getValue();

            if (doubles[0] <= random && doubles[1] >= random) {
                return Bundle.of(entry.getKey(), doubles[2].intValue());
            }
        }

        return Bundle.of(RudiCrates.getPlugin().getItemManager().error, -1);
    }

    private int task;
    private int count = 0;
    private int delay = 1;
    private boolean loop = true;

    public void animation(final Player player, final Crate crate, final int winningItemID) {
        final Inventory animationInventory = Bukkit.createInventory(null, 27, Language.withoutPrefix("inventories.opening", "%crate%", crate.getDisplayname()));
        final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
        final ItemStack winningItem = crateConfig.getItemStack(winningItemID + ".item");

        for (int i = 0; i < animationInventory.getSize(); i++) {
            animationInventory.setItem(i, RudiCrates.getPlugin().getItemManager().blackGlass);
        }

        final ItemStack winIndicator = RudiCrates.getPlugin().getVersionUtils().getConfigItem("items.display");
        animationInventory.setItem(4, winIndicator);

        for (int i = 9; i < 18; i++) {
            animationInventory.setItem(i, this.getRandomItem(crate).getKey());
        }

        player.openInventory(animationInventory);
        currentlyOpening.add(player.getUniqueId());

        while (loop) {
            if (currentlyOpening.contains(player.getUniqueId())) {
                loop = false;
                task = Bukkit.getScheduler().scheduleSyncRepeatingTask(RudiCrates.getPlugin(), () -> {
                    for (int i = 10; i < 18; i++) {
                        final ItemStack item = animationInventory.getItem(i);
                        animationInventory.setItem(i - 1, item);
                    }

                    if (delay == 12 && count == 1) {
                        animationInventory.setItem(17, winningItem);
                    } else
                        animationInventory.setItem(17, this.getRandomItem(crate).getKey());

                    player.playSound(player.getLocation(), RudiCrates.getPlugin().getVersionUtils().pling(), 1.0F, 0.8F);
                    count++;

                    if (count >= 2) {
                        count = delay <= 8 ? 0 : 1;
                        delay++;

                        if (delay >= 17) {
                            Bukkit.getScheduler().cancelTask(task);

                            Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
                                player.playSound(player.getLocation(), RudiCrates.getPlugin().getVersionUtils().winSound(), 1.0F, 0.8F);
                                delay = 2;
                                this.finishAnimation(player, animationInventory);
                            }, 15);
                            
                            return;
                        }

                        loop = true;
                        RudiCrates.getPlugin().getVersionUtils().updateTaskPeriod(task, delay);
                    }
                }, 5, delay);
            }
        }
    }

    private int finishTask;
    private int left = 12;
    private int right = 14;

    private void finishAnimation(final Player player, final Inventory inventory) {
        left = 12;
        right = 14;

        finishTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(RudiCrates.getPlugin(), () -> {
            if (left <= 8 && right >= 18) {
                Bukkit.getScheduler().cancelTask(finishTask);

                Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
                    currentlyOpening.remove(player.getUniqueId());

                    if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                        RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player);
                    }
                }, 40);
                return;
            }

            inventory.setItem(left, RudiCrates.getPlugin().getItemManager().greenGlass);
            inventory.setItem(right, RudiCrates.getPlugin().getItemManager().greenGlass);
            player.playSound(player.getLocation(), RudiCrates.getPlugin().getVersionUtils().blazeShoot(), 0.5F, 1.5F);
            left--;
            right++;

        }, 10, 4);
    }

    public void openCrate(final Player player, final Crate crate, final boolean animation) {
        if (!RudiCrates.getPlugin().getConfig().getBoolean("enabled")) {
            Language.send(player, "crate.opening_disabled");
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            Language.send(player, "player.inventory_full");
            return;
        }

        final UUID uuid = player.getUniqueId();
        final boolean useItemKeys = RudiCrates.getPlugin().getConfig().getBoolean("use_item_keys");
        final boolean hasVirtualKeys = RudiCrates.getPlugin().getDataUtils().getCrateAmount(uuid, crate) > 0;
        final boolean hasItemKeys = this.getKeyItemAmount(player, crate, false) > 0;

        if (!useItemKeys && !hasVirtualKeys && hasItemKeys) {
            player.sendMessage(Language.get("crate.no_remaining") + " " + Language.withoutPrefix("crate.no_remaining_addon"));
            return;
        }
        
        if (!hasVirtualKeys && !hasItemKeys) {
            Language.send(player, "crate.no_remaining");
            return;
        }

        final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
        final Map<ItemStack, Double[]> chances = Crate.getMap().get(crate.getName());

        if (crateConfig.getKeys(false).isEmpty()) {
            Language.send(player, "crate.empty");
            if (player.hasPermission("rudicrates.addtocrate")) Language.send(player, "crate.empty_addon");
            return;
        }

        if (chancesResult.get(crate.getName()) == 0) {
            Language.send(player, "crate.no_items_available");
            return;
        }

        if (chances == null || chances.entrySet().isEmpty()) {
            Language.send(player, "crate.incorrect_win_chances");
            return;
        }

        final Bundle<ItemStack, Integer> randomItemBundle = this.getRandomItem(crate);
        final int id = randomItemBundle.getValue();
        final ItemStack item = randomItemBundle.getKey();

        final double chance = crateConfig.getDouble(id + ".chance");
        final boolean limited = crateConfig.get(id + ".limited") != null;

        if (item.isSimilar(RudiCrates.getPlugin().getItemManager().error)) {
            Language.send(player, "crate.opening_cancelled", "%id%", String.valueOf(id));
            return;
        }

        int openDelay = 0;
        final String amount = item.getAmount() > 1 ? item.getAmount() + "x " : "";

        if (animation) {
            openDelay = 215;
            this.animation(player, crate, id);
        }

        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
            final TranslatableComponent display = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? new TranslatableComponent(item.getItemMeta().getDisplayName()) : RudiCrates.getPlugin().getTranslationUtils().getTranslation(item.getType());
            display.setColor(ChatColor.GOLD);
            List<String> commands = crateConfig.getStringList(id + ".commands");

            if (!commands.isEmpty()) {
                commands = this.replace(commands, "%player%", player.getName());
                commands = this.replace(commands, "%crate%", crate.getDisplayname() + ChatColor.RESET);
                commands = this.replace(commands, "%chance%", chance + "%");
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', command)));
            }

            if (!crateConfig.getBoolean(id + ".virtual")) player.getInventory().addItem(item);

            if (hasItemKeys && useItemKeys) {
                this.removeKeyItem(player, crate);
            } else
                RudiCrates.getPlugin().getDataUtils().removeCrates(uuid, crate, 1);
            
            if (limited) {
                int limit = crateConfig.getInt(id + ".limited");
                crateConfig.set(id + ".limited", --limit);

                if (RudiCrates.getPlugin().getConfig().getDouble("pull_events.limited") >= chance) {
                    for (final Player players : Bukkit.getOnlinePlayers()) {
                        players.spigot().sendMessage(RudiCrates.getPlugin().getTranslationUtils().getWinMessage(TranslationUtils.MessageType.BROADCAST_LIMITED, display, amount, chance, player.getName(), crate.getDisplayname(), limit));
                        players.playSound(players.getLocation(), RudiCrates.getPlugin().getVersionUtils().pling(), 1.0F, 1.0F);
                    }

                } else {
                    player.spigot().sendMessage(RudiCrates.getPlugin().getTranslationUtils().getWinMessage(TranslationUtils.MessageType.SELF_LIMITED, display, amount, chance, player.getName(), crate.getDisplayname(), limit));
                }

                if (limit < 1) {
                    if (RudiCrates.getPlugin().getConfig().getBoolean("remove_limited_items")) {
                        crateConfig.set(String.valueOf(id), null);
                    }

                    Crate.getMap().get(crate.getName()).remove(item);
                }

                try {
                    crateConfig.save(crate.getFile());
                } catch (final IOException exception) {
                    exception.printStackTrace();
                }

                Crate.reloadCrateMaps();
                this.loadChancesResult();
                RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
            }

            if (chance <= RudiCrates.getPlugin().getConfig().getInt("pull_events.firework")) this.spawnFirework(player);

            if (!limited && chance <= RudiCrates.getPlugin().getConfig().getDouble("pull_events.broadcast")) {
                Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(RudiCrates.getPlugin().getTranslationUtils().getWinMessage(TranslationUtils.MessageType.BROADCAST, display, amount, chance, player.getName(), crate.getDisplayname(), 0)));

            } else if (!limited) {
                player.spigot().sendMessage(RudiCrates.getPlugin().getTranslationUtils().getWinMessage(TranslationUtils.MessageType.SELF, display, amount, chance, player.getName(), crate.getDisplayname(), 0));
            }

            if (!animation) {
                Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player), 2);
            }

        }, openDelay);
    }

    public List<String> replace(final List<String> list, final String placeholder, final String replace) {
        final List<String> newList = new ArrayList<>();

        list.forEach(string -> {
            if (string.contains(placeholder)) {
                newList.add(string.replace(placeholder, replace));
            } else
                newList.add(string);
        });

        return newList;
    }

    public void loadChancesResult() {
        Arrays.stream(getCrates()).forEach(crate -> {
            final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
            double amount = 0;

            for (final String key : crateConfig.getKeys(false)) {
                if (crateConfig.get(key + ".limited") != null && crateConfig.getInt(key + ".limited") < 1) continue;
                amount = amount + crateConfig.getDouble(key + ".chance");
            }

            chancesResult.put(crate.getName(), amount);
        });
    }

    private void spawnFirework(final Player player) {
        final Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation().add(0, 2, 0), EntityType.FIREWORK);
        final FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().withFlicker().withColor(Color.MAROON).build());
        firework.setFireworkMeta(meta);
    }

    public int getKeyItemAmount(final Player player, final Crate crate, final boolean enderchest) {
        final ItemStack keyItem = RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1);
        final ItemStack[] inventory = player.getInventory().getContents();
        final ItemStack[] withEnderchest = Stream.concat(Arrays.stream(inventory), Arrays.stream(player.getEnderChest().getContents())).toArray(ItemStack[]::new);
        final ItemStack[] use = enderchest ? withEnderchest : inventory;
        int count = 0;

        for (final ItemStack item : use) {
            if (item == null) continue;
            if (item.isSimilar(keyItem)) {
                count = count + item.getAmount();
            }
        }
        
        return count;
    }

    public void removeKeyItem(final Player player, final Crate crate) {
        if (this.getKeyItemAmount(player, crate, false) < 1) return;
        player.getInventory().removeItem(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1));
    }
}

    /*
        public void removeKeyItem(final Player player, final Crate crate) {
        if (this.getKeyItemAmount(player, crate) < 1) return;
        for (final ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            
            if (item.isSimilar(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1))) {
                item.setAmount(item.getAmount() -1);
                break;
            }
        }
    }
     */
