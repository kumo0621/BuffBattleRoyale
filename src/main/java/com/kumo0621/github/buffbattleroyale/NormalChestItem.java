package com.kumo0621.github.buffbattleroyale;

import org.bukkit.inventory.ItemStack;

/**
 * チェストに投入する通常アイテムとその出現重みを保持するクラスです。
 */
public class NormalChestItem {
    private final ItemStack item;
    private final double chance; // 例: 0.4 は40%の重み

    public NormalChestItem(ItemStack item, double chance) {
        this.item = item;
        this.chance = chance;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getChance() {
        return chance;
    }
}
