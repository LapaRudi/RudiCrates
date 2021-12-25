package de.laparudi.rudicrates.crate;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.utils.LocationNameUtils;
import de.laparudi.rudicrates.utils.items.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CrateUtils {
    
    public static List<UUID> currentlyOpening = new ArrayList<>();
    public static List<ItemStack> keyItems = new ArrayList<>();
    public static Map<UUID, Location> inCrateMenu = new HashMap<>();
    private final static Map<String, Double> chancesResult = new HashMap<>();
    
    public final BaseComponent componentPrefix() {
        if(RudiCrates.getPlugin().getServerVersion().contains("1.16")) {
            return arrayToSingleComponent(new ComponentBuilder("[").color(ChatColor.of("#494242"))
                .append("R").color(ChatColor.of("#970460")).append("u").color(ChatColor.of("#96297b")).append("d").color(ChatColor.of("#914195"))
                .append("i").color(ChatColor.of("#8755ac")).append("C").color(ChatColor.of("#7968c0")).append("r").color(ChatColor.of("#677ad0"))
                .append("a").color(ChatColor.of("#538adc")).append("t").color(ChatColor.of("#3c9ae5")).append("e").color(ChatColor.of("#24a9ea"))
                .append("s").color(ChatColor.of("#18b7ec")).append("] ").color(ChatColor.of("#494242")).create());
        }
        return new TextComponent("§8[§4Rudi§5Crates§8] §r");
    }

    public final Crate[] getCrates() {
        final List<Crate> list = new ArrayList<>();
        
        for(String key : RudiCrates.getPlugin().getConfig().getKeys(true)) {
            if(!key.startsWith("crates.")) continue;
            key = key.replaceFirst("crates.", "");
            if(key.contains(".")) continue;
            list.add(Crate.getByName(key));
        }
        
        return list.toArray(Crate[]::new);
    }
    
    private ItemStack getRandomItem(Crate crate) {
        if(crate.getMap().isEmpty()) return null;
        final double random = ThreadLocalRandom.current().nextDouble(chancesResult.get(crate.getName()));
        
        for(Map.Entry<ItemStack, Double[]> entry : crate.getMap().get(crate.getName()).entrySet()) {
            final Double[] doubles = entry.getValue();
            
            if(doubles[0] <= random && doubles[1] >= random) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private int task;
    private int count = 0;
    private int delay = 1;
    private boolean loop = true;
    
    public void animation(Player player, Crate crate, int winningItemID) {
        final Inventory animationInventory = Bukkit.createInventory(null, 27, crate.getDisplayname() + "§8 " + RudiCrates.getPlugin().getLanguage().opening);
        final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
        final ItemStack winningItem = crateConfig.getItemStack(winningItemID + ".item");
        
        for (int i = 0; i < animationInventory.getSize(); i++) {
            animationInventory.setItem(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").toItem());
        }

        final ItemStack winIndicator = new ItemBuilder(Material.getMaterial(Objects.requireNonNull(RudiCrates.getPlugin().getConfig().getString("windisplayitem"))))
                .setName(RudiCrates.getPlugin().getConfig().getString("windisplayname")).invisibleEnchant(RudiCrates.getPlugin().getConfig().getBoolean("windisplayitemenchant")).toItem();
        animationInventory.setItem(4, winIndicator);
        
        for (int i = 9; i < 18; i++) {
            animationInventory.setItem(i, getRandomItem(crate));
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

                    if(delay == 12 && count == 1) {
                        animationInventory.setItem(17, winningItem);
                    } else
                        animationInventory.setItem(17, getRandomItem(crate));
                    
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
    
    private void finishAnimation(Player player, Inventory inventory) {
        left = 12;
        right = 14;
        
        finishTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(RudiCrates.getPlugin(), () -> {
            if(left <= 8 && right >= 18) {
                Bukkit.getScheduler().cancelTask(finishTask);
                
                Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> {
                    currentlyOpening.remove(player.getUniqueId());
                    if (!player.getOpenInventory().getTopInventory().isEmpty()) {
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

    public void openCrate(Player player, Crate crate, boolean animation) {
        if (!RudiCrates.getPlugin().getConfig().getBoolean("enabled")) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().crateOpeningDisabled);
            return;
        }

        final UUID uuid = player.getUniqueId();
        final boolean noVirtualCrates = RudiCrates.getPlugin().getDataUtils().getCrateAmount(uuid, crate) < 1;
        final boolean noItemCrates = this.getKeyItemAmount(player, crate) < 1;

        if (noVirtualCrates && noItemCrates) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().noCratesRemaining);
            return;
        }

        final FileConfiguration crateConfig = YamlConfiguration.loadConfiguration(crate.getFile());
        final Map<ItemStack, Double[]> chances = crate.getMap().get(crate.getName());

        if (crateConfig.getKeys(false).isEmpty()) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().noWinsInCrate);

            if (player.hasPermission("rudicrates.addtocrate")) {
                player.sendMessage(RudiCrates.getPlugin().getLanguage().noWinsInCrateAddon);
            }
            
            return;
        }

        if (chances == null || chances.entrySet().isEmpty()) {
            player.sendMessage(RudiCrates.getPlugin().getLanguage().incorrectWinChances);
            return;
        }

        final double random = ThreadLocalRandom.current().nextDouble(0, chancesResult.get(crate.getName()));
        for (Map.Entry<ItemStack, Double[]> entry : chances.entrySet()) {
            Double[] doubles = entry.getValue();

            if (doubles[0] <= random && doubles[1] >= random) {
                final int id = doubles[2].intValue();
                final double chance = crateConfig.getDouble(id + ".chance");
                final ItemStack item = crateConfig.getItemStack(id + ".item");
                final boolean limited = crateConfig.get(id + ".limited") != null;

                if (item == null) {
                    player.sendMessage(RudiCrates.getPlugin().getLanguage().openingCancelled.replace("%id%", String.valueOf(id)));
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
                    String command = crateConfig.getString(id + ".command");

                    if (command != null) {
                        command = replace(command, "%player%", player.getName());
                        command = replace(command, "%crate%", crate.getDisplayname());
                        command = replace(command, "%chance%", chance + "%");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ChatColor.translateAlternateColorCodes('&', command));
                    }

                    if (!crateConfig.getBoolean(id + ".virtual")) player.getInventory().addItem(item);

                    if (limited) {
                        BaseComponent component = new TextComponent();
                        int limit = crateConfig.getInt(id + ".limited");
                        crateConfig.set(id + ".limited", --limit);

                        if (RudiCrates.getPlugin().getConfig().getDouble("broadcastlimited") >= chance) {
                            component.addExtra(componentPrefix());
                            component.addExtra(RudiCrates.getPlugin().getLanguage().win1.replace("%player%", player.getName()).replace("%amount%", amount));
                            component.addExtra(display);
                            component.addExtra(RudiCrates.getPlugin().getLanguage().winLimited.replace("%crate%", crate.getDisplayname()).replace("%chance%", String.valueOf(chance)).replace("%amount%", String.valueOf(limit)));

                            for (Player players : Bukkit.getOnlinePlayers()) {
                                players.spigot().sendMessage(component);
                                players.playSound(players.getLocation(), RudiCrates.getPlugin().getVersionUtils().pling(), 1.0F, 1.0F);
                            }

                        } else {
                            component.addExtra(componentPrefix());
                            component.addExtra(RudiCrates.getPlugin().getLanguage().win1Self.replace("%amount%", amount));
                            component.addExtra(display);
                            component.addExtra(" " + RudiCrates.getPlugin().getLanguage().winLimited.replace("%crate%", crate.getDisplayname()).replace("%chance%", String.valueOf(chance)).replace("%amount%", String.valueOf(limit)));
                            player.spigot().sendMessage(component);
                        }

                        if (limit < 1) {
                            if (RudiCrates.getPlugin().getConfig().getBoolean("removelimiteditems")) {
                                crateConfig.set(String.valueOf(id), null);
                            }

                            crate.getMap().get(crate.getName()).remove(item);
                        }

                        try {
                            crateConfig.save(crate.getFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        RudiCrates.getPlugin().getInventoryUtils().loadPreviewInventories();
                    }

                    if (chance <= RudiCrates.getPlugin().getConfig().getInt("firework")) spawnFirework(player);

                    if (!limited && chance <= RudiCrates.getPlugin().getConfig().getDouble("broadcast")) {
                        BaseComponent component = new TextComponent();
                        component.addExtra(componentPrefix());
                        component.addExtra(RudiCrates.getPlugin().getLanguage().win1.replace("%player%", player.getName()).replace("%amount%", amount));
                        component.addExtra(display);
                        component.addExtra(" " + RudiCrates.getPlugin().getLanguage().winBroadcast.replace("%player%", player.getName()).replace("%crate%", crate.getDisplayname()).replace("%chance%", String.valueOf(chance)));
                        Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(component));

                    } else if (!limited) {
                        BaseComponent component = new TextComponent();
                        component.addExtra(componentPrefix());
                        component.addExtra(RudiCrates.getPlugin().getLanguage().win1Self.replace("%amount%", amount));
                        component.addExtra(display);
                        component.addExtra(RudiCrates.getPlugin().getLanguage().win.replace("%chance%", String.valueOf(chance)));
                        player.spigot().sendMessage(component);
                    }

                    if (!noItemCrates) {
                        this.removeKeyItem(player, crate);

                    } else
                        RudiCrates.getPlugin().getDataUtils().removeCrates(uuid, crate, 1);
                    
                    if (!animation) {
                        Bukkit.getScheduler().runTaskLater(RudiCrates.getPlugin(), () -> RudiCrates.getPlugin().getInventoryUtils().openCrateMenu(player), 2);
                    }

                }, openDelay);
                break;
            }
        }
    }

    private BaseComponent arrayToSingleComponent(BaseComponent[] array) {
        BaseComponent component = new TextComponent();
        Arrays.stream(array).forEach(component::addExtra);
        return component;
    }
    
    public String replace(String input, String placeholder, String replace) {
        if(input.contains(placeholder)) {
            return input.replace(placeholder, replace);
        }
        return input;
    }
    
    public void loadChancesResult() {
        Arrays.stream(getCrates()).forEach(crate -> {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(crate.getFile());
            double amount = 0;
            
            for (String key : config.getKeys(false)) {
                if (config.get(key + ".limited") != null && config.getInt(key + ".limited") < 1) continue;
                amount = amount + config.getDouble(key + ".chance");
            }
            
            chancesResult.put(crate.getName(), amount);
            Bukkit.broadcastMessage("put " + amount + " in for crate " + crate.getName());
        });
    }
    
    private void spawnFirework(Player player) {
        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation().add(0,2,0), EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().withFlicker().withColor(Color.MAROON).build());
        firework.setFireworkMeta(meta);
    }
    
    public boolean isCrateOpeningInventory(Player player) {
        final Block target = player.getTargetBlock(null, 5);
        if(!(target.getState() instanceof Chest)) return false;

        final Chest chest = (Chest) target.getState();
        final FileConfiguration config = YamlConfiguration.loadConfiguration(RudiCrates.getPlugin().getLocationsFile());
        final List<String> locations = config.getStringList("locations");
        return locations.contains(LocationNameUtils.toLocationString(chest.getLocation()));
    }
    
    public void setupKeyItemList() {
        keyItems.clear();
        Arrays.stream(getCrates()).forEach(crate -> keyItems.add(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1)));
    }
    
    public int getKeyItemAmount(Player player, Crate crate) {
        final ItemStack keyItem = RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1);
        int count = 0;
        
        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null) continue;
            if(item.isSimilar(keyItem)) {
                count = count + item.getAmount();
            }
        }
        
        return count;
    }
    
    public void removeKeyItem(Player player, Crate crate) {
        if(this.getKeyItemAmount(player, crate) < 1) return;
        for(ItemStack item : player.getInventory().getContents()) {
            if(item == null) continue;
            
            if(item.isSimilar(RudiCrates.getPlugin().getItemManager().getCrateKeyItem(crate, 1))) {
                item.setAmount(item.getAmount() -1);
            }
        }
    }
}
