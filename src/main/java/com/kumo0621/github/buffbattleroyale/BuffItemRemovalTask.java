package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;

public class BuffItemRemovalTask implements Runnable {

    // 有効なスロット番号の集合（効果発動用: 0～8、保存用: 27～29）
    private final Set<Integer> validSlots = new HashSet<>();

    public BuffItemRemovalTask() {
        // 有効スロット：ホットバー（0～8）
        for (int i = 0; i <= 8; i++) {
            validSlots.add(i);
        }
        // 保存用スロット：インベントリ下段の一部（27～29）
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
            int invSize = player.getInventory().getSize();
            for (int slot = 0; slot < invSize; slot++) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item == null) continue;
                // バフアイテムかどうかチェック
                if (!isBuffItem(item)) continue;
                if (validSlots.contains(slot)) {
                    // 有効スロットの場合、スタック数が2個以上なら余剰分をドロップして、1個だけ残す
                    if (item.getAmount() >= 2) {
                        int extra = item.getAmount() - 1;
                        item.setAmount(1); // 1個だけ残す
                        // 余剰分のアイテムを足元にドロップ
                        ItemStack dropStack = item.clone();
                        dropStack.setAmount(extra);
                        player.getWorld().dropItemNaturally(player.getLocation(), dropStack);
                    }
                } else {
                    // 無効スロットの場合、アイテムを削除するのではなく足元にドロップする
                    player.getInventory().setItem(slot, null);
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
        }
    }

    /**
     * 引数のアイテムが、BuffRegistry に登録されているバフアイテムと一致するか判定する
     */
    private boolean isBuffItem(ItemStack item) {
        return BuffRegistry.getRegisteredBuffItems().stream().anyMatch(data -> data.matches(item));
    }
}
