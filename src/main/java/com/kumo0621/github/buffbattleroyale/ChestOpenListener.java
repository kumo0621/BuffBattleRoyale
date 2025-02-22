package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;


public class ChestOpenListener implements Listener {
    private Location chestLocation;

    public ChestOpenListener(Plugin plugin) {
        // config.yml からチェスト座標を読み込む
        FileConfiguration config = plugin.getConfig();
        String worldName = config.getString("chest.world", "world");
        chestLocation = new Location(Bukkit.getWorld(worldName),
                config.getDouble("chest.x", 0),
                config.getDouble("chest.y", 64),
                config.getDouble("chest.z", 0));
    }

    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof Chest)) return;
        Chest chest = (Chest) inv.getHolder();
        Location loc = chest.getLocation();
        // 座標が一致するかチェック（座標がぴったり一致する場合）
        if (loc.equals(chestLocation)) {
            // チェストを開いたプレイヤーに経験値を付与（例：5 XP）
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                player.giveExp(5);
                player.sendMessage("チェストから経験値を獲得しました！");
            }
        }
    }
}
