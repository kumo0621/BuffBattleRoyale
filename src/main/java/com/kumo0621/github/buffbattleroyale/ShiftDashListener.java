package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftDashListener implements Listener {
    // プレイヤーのシフト開始時刻を記録するマップ
    private final Map<UUID, Long> dashStartTimes = new HashMap<>();
    // プレイヤーごとの ActionBar 表示用タスク
    private final Map<UUID, BukkitTask> dashTasks = new HashMap<>();

    // 対象スロット（9～17番）
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;
    // 対象バフアイテムID（Shift Dash Buff）
    private static final String DASH_BUFF_ID = "shiftdash";
    // Dash 発動条件：1秒以上シフトを押し続ける
    private static final long THRESHOLD_MS = 1000;
    // Dash 移動距離：10ブロック（1ブロックずつ）
    private static final int DASH_STEPS = 20;
    // テレポート間隔（tick）：例として4 tick（0.2秒）
    private static final long TELEPORT_INTERVAL = 1L;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始：対象バフがある場合、開始時刻を記録し、ActionBar表示タスク開始
            if (hasDashBuff(player)) {
                long startTime = System.currentTimeMillis();
                dashStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int)(elapsed / 1000);
                    player.sendActionBar(ChatColor.GOLD + "Dash Charge: " + seconds + " sec");
                }, 0L, 20L);
                dashTasks.put(playerId, task);
            }
        } else {
            // シフト解除：対象バフがある場合、開始時刻をチェックしてダッシュ処理実行
            if (dashStartTimes.containsKey(playerId)) {
                BukkitTask task = dashTasks.remove(playerId);
                if (task != null) {
                    task.cancel();
                }
                long startTime = dashStartTimes.remove(playerId);
                long duration = System.currentTimeMillis() - startTime;
                if (duration >= THRESHOLD_MS) {
                    // Dash 発動：プレイヤーの向いている方向へ10ブロックを1ブロックずつテレポート
                    performDash(player);
                }
                player.sendActionBar("");
            }
        }
    }

    /**
     * 対象スロット内に、"shiftdash" バフアイテムが装備されているかチェック
     */
    private boolean hasDashBuff(Player player) {
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                BuffItemData data = BuffRegistry.getBuffItemById(DASH_BUFF_ID);
                if (data != null && data.matches(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * プレイヤーを向いている方向へ、1ブロックずつ10回テレポートさせる（壁があっても進む）。
     */
    private void performDash(final Player player) {
        // Dash を10ステップで行うタスク
        new BukkitRunnable() {
            int steps = 0;
            @Override
            public void run() {
                if (steps >= DASH_STEPS) {
                    cancel();
                    return;
                }
                // プレイヤーの現在位置と向いている方向を取得
                Location loc = player.getLocation();
                // 水平ベクトル（Y成分は 0 にして）
                Vector direction = loc.getDirection().setY(0).normalize();
                // 1ブロック分移動する位置を算出（壁判定なしでそのまま加算）
                Location newLoc = loc.add(direction);
                player.teleport(newLoc);
                steps++;
            }
        }.runTaskTimer(BuffBattleRoyale.getInstance(), 0L, TELEPORT_INTERVAL);
    }
}
