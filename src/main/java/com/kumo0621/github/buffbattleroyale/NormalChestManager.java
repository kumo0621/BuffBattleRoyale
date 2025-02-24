package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * チェストに投入する通常アイテムのリストと、重み付きランダム選択を管理するクラスです。
 */
public class NormalChestManager {
    private final List<NormalChestItem> items = new ArrayList<>();
    private final Random random = new Random();

    public NormalChestManager() {
        // 以下は例示です。必要に応じて config.yml から読み込むなどしてください。
        items.add(new NormalChestItem(new ItemStack(Material.EMERALD), 0.4));       // 40%
        items.add(new NormalChestItem(new ItemStack(Material.IRON_SWORD), 0.3));     // 30%
        items.add(new NormalChestItem(new ItemStack(Material.DIAMOND_AXE), 0.2));    // 20%
        items.add(new NormalChestItem(new ItemStack(Material.GOLDEN_APPLE), 0.1));     // 10%
    }

    public List<NormalChestItem> getItems() {
        return items;
    }

    /**
     * 重み付きランダムで通常アイテムを1点選択して返します。
     */
    public NormalChestItem getRandomItem() {
        double total = 0.0;
        for (NormalChestItem item : items) {
            total += item.getChance();
        }
        double r = random.nextDouble() * total;
        double sum = 0.0;
        for (NormalChestItem item : items) {
            sum += item.getChance();
            if (r <= sum) {
                return item;
            }
        }
        return null; // 通常はここには到達しない
    }
}
