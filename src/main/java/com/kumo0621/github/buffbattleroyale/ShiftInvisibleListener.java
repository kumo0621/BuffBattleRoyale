package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftInvisibleListener implements Listener {

    // プレイヤーごとにインビジブル効果更新タスクを保持するマップ
    private final Map<UUID, BukkitTask> invisibleTasks = new HashMap<>();

    // 対象スロット：9～17番
    private static final int BUFF_SLOT_START = 9;
    private static final int BUFF_SLOT_END = 17;
    // 対象バフアイテムのID
    private static final String SHIFT_INVISIBLE_BUFF_ID = "shiftinvisible";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID pid = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始時：対象バフが装備されている場合、インビジブル効果更新タスクを開始
            if (hasShiftInvisibleBuff(player)) {
                // タスク開始：1秒ごとにインビジブル効果を再付与（継続時間40tick）
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
                    // ※ActionBar表示なども必要なら追加可能
                }, 0L, 20L);
                invisibleTasks.put(pid, task);
            }
        } else {
            // シフト解除時：タスクをキャンセルし、インビジブル効果を解除
            if (invisibleTasks.containsKey(pid)) {
                BukkitTask task = invisibleTasks.remove(pid);
                if (task != null) {
                    task.cancel();
                }
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    /**
     * 対象スロット内に「shiftinvisible」バフアイテムが装備されているかチェック
     */
    private boolean hasShiftInvisibleBuff(Player player) {
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                BuffItemData data = BuffRegistry.getBuffItemById(SHIFT_INVISIBLE_BUFF_ID);
                if (data != null && data.matches(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
