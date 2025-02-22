package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // config.yml の "chests" リスト形式に対応して、複数のチェスト座標を読み込む
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        List<?> chestList = config.getList("chests");
        if (chestList == null) {
            plugin.getLogger().warning("No 'chests' list found in config.yml!");
            return;
        }
        for (Object obj : chestList) {
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                String worldName = (String) map.getOrDefault("world", "world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World " + worldName + " not found!");
                    continue;
                }
                double x = getDouble(map.get("x"), 0);
                double y = getDouble(map.get("y"), 64);
                double z = getDouble(map.get("z"), 0);
                Location loc = new Location(world, x, y, z);
                chestLocations.add(loc);
            }
        }
        if(chestLocations.isEmpty()){
            plugin.getLogger().warning("No valid chest locations found in config.yml!");
        }
    }

    private double getDouble(Object obj, double def) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // ※既存の自動投入タスクは不要の場合、start()/stop() を省略してもよいです。
    public void start() {
        // ここでは自動投入タスクは実行せず、コマンド実行時に投入する仕様とします。
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    // 外部からチェスト座標リストを参照できるように getter を追加
    public List<Location> getChestLocations() {
        return chestLocations;
    }
}
