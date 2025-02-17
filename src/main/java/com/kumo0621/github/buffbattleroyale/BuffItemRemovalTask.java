package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;

public class BuffItemRemovalTask implements Runnable {

    // 有効なスロット番号の集合（効果発動用: 9～17、保存用: 27～29）
    private final Set<Integer> validSlots = new HashSet<>();

    public BuffItemRemovalTask() {
        for (int i = 9; i <= 17; i++) {
            validSlots.add(i);
        }
        for (int i = 27; i <= 29; i++) {
            validSlots.add(i);
        }
    }

    @Override
    public void run() {
        // オンラインの各プレイヤーについてチェック
        for (Player player : Bukkit.getOnlinePlayers()) {
            // クリエイティブモードのプレイヤーは除外
            if (player.getGameMode() == GameMode.CREATIVE) {
                continue;
            }
            // プレイヤーのインベントリ内の全スロットを走査
            int invSize = player.getInventory().getSize();
            for (int slot = 0; slot < invSize; slot++) {
                // 有効なスロット以外の場合
                if (!validSlots.contains(slot)) {
                    ItemStack item = player.getInventory().getItem(slot);
                    if (item != null && isBuffItem(item)) {
                        // 該当するバフアイテムは削除（消滅）
                        player.getInventory().setItem(slot, null);
                    }
                }
            }
        }
    }

    /**
     * 引数のアイテムが、BuffRegistry に登録されているバフアイテムと一致するか判定
     */
    private boolean isBuffItem(ItemStack item) {
        return BuffRegistry.getRegisteredBuffItems().stream().anyMatch(data -> data.matches(item));
    }
}
