package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetClearListener implements Listener {

    // メインハンドに持っている場合のみ対象（スニーク中にチェック）
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    // シフト中の ActionBar 表示用タスクを管理するマップ
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();

    private static final long THRESHOLD_MS = 1000;
    private static final double RANGE = 3.0;
    private static final String TARGET_CLEAR_BUFF_ID = "targetclear";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始時：メインハンドに対象バフがあれば開始時刻を記録し、ActionBar にチャージ時間を表示
            if (hasTargetClearBuffInMainHand(player)) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int)(elapsed / 1000);
                    player.sendActionBar(ChatColor.AQUA + "Target Clear Charge: " + seconds + " sec");
                }, 0L, 20L);
                chargeTasks.put(playerId, task);
            }
        } else {
            // シフト解除時：もし開始時刻が記録されていれば処理
            if (sneakStartTimes.containsKey(playerId)) {
                // ActionBar 更新タスクをキャンセル
                BukkitTask task = chargeTasks.remove(playerId);
                if (task != null) {
                    task.cancel();
                }
                long duration = System.currentTimeMillis() - sneakStartTimes.remove(playerId);
                player.sendActionBar("");
                if (duration >= THRESHOLD_MS && hasTargetClearBuffInMainHand(player)) {
                    Collection<Entity> nearby = player.getNearbyEntities(RANGE, RANGE, RANGE);
                    int cleared = 0;
                    for (Entity entity : nearby) {
                        if (entity instanceof Creature) {
                            Creature mob = (Creature) entity;
                            if (mob.getTarget() != null && mob.getTarget().equals(player)) {
                                mob.setTarget(null);
                                // メタデータを設定して、今後このプレイヤーをターゲットしないようにする
                                mob.setMetadata("targetCleared:" + player.getUniqueId().toString(),
                                        new FixedMetadataValue(BuffBattleRoyale.getInstance(), true));
                                cleared++;
                            }
                        }
                    }
                    if (cleared > 0) {
                        player.sendMessage(ChatColor.GREEN + "Target Clear Buff activated! Cleared " + cleared +
                                " hostile mob(s) from targeting you permanently.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Target Clear Buff activated, but no hostile mob was found within range.");
                    }
                }
            }
        }
    }

    /**
     * プレイヤーのメインハンドに "targetclear" バフが存在するかチェックする
     */
    private boolean hasTargetClearBuffInMainHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null) {
            BuffItemData data = BuffRegistry.getBuffItemById(TARGET_CLEAR_BUFF_ID);
            return data != null && data.matches(mainHand);
        }
        return false;
    }
}
