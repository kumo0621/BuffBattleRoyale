package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftBowBuffListener implements Listener {

    // プレイヤーごとにシフト開始時刻を記録
    private final Map<UUID, Long> bowSneakStartTimes = new HashMap<>();
    // プレイヤーごとに ActionBar 表示タスクを保持
    private final Map<UUID, BukkitTask> bowChargeTasks = new HashMap<>();

    // 対象スロット（ホットバー：スロット 0～8）
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;
    // 対象バフアイテムのID（"shiftbow" のみ対応）
    private static final String SHIFT_BOWBUFF_ID = "shiftbow";

    // 発射する全方位の角度ステップ（例：30度刻み＝12方向）
    private static final double ANGLE_STEP_DEG = 30.0;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID pid = player.getUniqueId();
        Plugin plugin = BuffBattleRoyale.getInstance();

        if (event.isSneaking()) {
            // シフト開始：対象バフがある場合、開始時刻を記録し ActionBar にチャージ時間表示
            if (hasBowBuff(player)) {
                long startTime = System.currentTimeMillis();
                bowSneakStartTimes.put(pid, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsed / 1000);
                    player.sendActionBar(ChatColor.AQUA + "Bow Charge Time: " + seconds + " sec");
                }, 0L, 20L);
                bowChargeTasks.put(pid, task);
            }
        } else {
            // シフト解除：対象バフがある場合、チャージ時間に応じた volley 発射を行う
            if (bowSneakStartTimes.containsKey(pid)) {
                BukkitTask task = bowChargeTasks.remove(pid);
                if (task != null) {
                    task.cancel();
                }
                long startTime = bowSneakStartTimes.remove(pid);
                int chargeSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);

                if (chargeSeconds < 1) {
                    player.sendMessage(ChatColor.YELLOW + "Charge time too short. No arrows fired.");
                    player.sendActionBar("");
                    return;
                }

                // 対象スロット内に装備している "shiftbow" バフの個数をカウント（キャップ撤廃）
                int buffCount = countBuffs(player, SHIFT_BOWBUFF_ID);
                if (buffCount <= 0) {
                    player.sendMessage(ChatColor.YELLOW + "No Bow Buff found.");
                    player.sendActionBar("");
                    return;
                }

                // 発射する volley は chargeSeconds 回（1秒ごと）
                for (int i = 0; i < chargeSeconds; i++) {
                    spawnArrowVolleyHemisphere(player, buffCount);
                }
                int totalVolleys = chargeSeconds; // volley回数
                player.sendMessage(ChatColor.GOLD + "Bow Buff: " + totalVolleys + " volley(s) fired in hemisphere shape with " + buffCount + " layer(s)!");
                player.sendActionBar("");
            }
        }
    }

    /**
     * 対象スロット内に "shiftbow" バフアイテムが装備されているかチェック
     */
    private boolean hasBowBuff(Player player) {
        return countBuffs(player, SHIFT_BOWBUFF_ID) > 0;
    }

    /**
     * 対象スロット内に装備している、指定された buffId のバフアイテムの個数をカウントする
     */
    private int countBuffs(Player player, String buffId) {
        int count = 0;
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                if (player.getInventory().getItemInMainHand() != null &&
                    BuffRegistry.getBuffItemById(SHIFT_BOWBUFF_ID).matches(player.getInventory().getItemInMainHand())) {
                    BuffItemData data = BuffRegistry.getBuffItemById(buffId);
                    if (data != null && data.matches(item)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 半球状（上向きも含む）に矢を発射する volley を1回行う。
     * layers = buffCount として、層数を決定。
     * ピッチ角は 0°（水平）～ 90°（真上）まで均等に分割される。
     */
    private void spawnArrowVolleyHemisphere(Player player, int layers) {
        Location loc = player.getEyeLocation();
        int directions = (int) (360 / ANGLE_STEP_DEG);
        // 各層（ピッチ）ごとにループ
        for (int l = 0; l < layers; l++) {
            double pitch;
            if (layers == 1) {
                pitch = 0; // 1層なら水平のみ
            } else {
                // l=0 → pitch=0, l=layers-1 → pitch=90°
                pitch = 90.0 * l / (layers - 1);
            }
            for (int i = 0; i < directions; i++) {
                double yaw = i * ANGLE_STEP_DEG;
                double radPitch = Math.toRadians(pitch);
                double radYaw = Math.toRadians(yaw);
                // xz平面の大きさは cos(radPitch)、y成分は sin(radPitch)
                double x = Math.cos(radPitch) * Math.cos(radYaw);
                double y = Math.sin(radPitch);
                double z = Math.cos(radPitch) * Math.sin(radYaw);
                Vector direction = new Vector(x, y, z);
                direction.normalize();
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(direction.multiply(1.5));
                arrow.setShooter(player);
                arrow.setMetadata("projectileBuffOwner", new FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            }
        }
    }
}
