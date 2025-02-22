package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuffChestManager {
    private final Plugin plugin;
    private final List<Location> chestLocations = new ArrayList<>();
    private BukkitTask task;
    private final Random random = new Random();

    public BuffChestManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // config.yml から複数のチェスト座標を読み込む
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection chestsSection = config.getConfigurationSection("chests");
        if (chestsSection == null) {
            plugin.getLogger().warning("No 'chests' section found in config.yml!");
            return;
        }
        // chests セクション内の各キー（リスト形式の場合は自動的に数値のキーが振られます）
        for (String key : chestsSection.getKeys(false)) {
            String worldName = chestsSection.getString(key + ".world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World " + worldName + " not found for chest " + key);
                continue;
            }
            double x = chestsSection.getDouble(key + ".x", 0);
            double y = chestsSection.getDouble(key + ".y", 64);
            double z = chestsSection.getDouble(key + ".z", 0);
            Location loc = new Location(world, x, y, z);
            chestLocations.add(loc);
        }
        if(chestLocations.isEmpty()){
            plugin.getLogger().warning("No valid chest locations found in config.yml!");
        }
    }

    public void start() {
        // 例：30秒ごとに、各チェストに対して各バフアイテムの chance に基づきアイテムを追加する
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (chestLocations.isEmpty()) return;
            for (Location loc : chestLocations) {
                if (!(loc.getBlock().getState() instanceof Chest)) {
                    plugin.getLogger().warning("No chest found at " + loc);
                    continue;
                }
                Chest chest = (Chest) loc.getBlock().getState();
                Inventory inv = chest.getInventory();
                for (BuffItemData buffData : BuffRegistry.getRegisteredBuffItems()) {
                    double chance = buffData.getChance();
                    if (chance < 1.0 && random.nextDouble() < chance) {
                        ItemStack item = buffData.createItemStack();
                        inv.addItem(item);
                    }
                }
            }
        }, 0L, 600L); // 600 tick = 30秒
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
