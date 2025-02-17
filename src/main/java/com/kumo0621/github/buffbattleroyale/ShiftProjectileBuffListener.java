package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftProjectileBuffListener implements Listener {

    // プレイヤーごとにシフト開始時刻を記録
    private final Map<UUID, Long> projectileSneakStartTimes = new HashMap<>();
    // プレイヤーごとに ActionBar 表示タスクを保持
    private final Map<UUID, BukkitTask> projectileChargeTasks = new HashMap<>();

    // 対象スロット（9～17番）
    private static final int BUFF_SLOT_START = 9;
    private static final int BUFF_SLOT_END = 17;
    // 対象バフアイテムのID
    private static final String SHIFT_FIREBUFF_ID = "shiftfirecharge";
    private static final String SHIFT_BOWBUFF_ID = "shiftbow";

    // 発射する全方位の角度ステップ（例：30度間隔＝12方向）
    private static final double ANGLE_STEP_DEG = 30.0;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID pid = player.getUniqueId();
        Plugin plugin = BuffBattleRoyale.getInstance();

        if (event.isSneaking()) {
            // シフト開始：いずれかの対象バフがある場合、開始時刻を記録し、ActionBar にチャージ時間表示
            if (hasAnyProjectileBuff(player)) {
                long startTime = System.currentTimeMillis();
                projectileSneakStartTimes.put(pid, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsed / 1000);
                    player.sendActionBar(ChatColor.AQUA + "Projectile Charge Time: " + seconds + " sec");
                }, 0L, 20L);
                projectileChargeTasks.put(pid, task);
            }
        } else {
            // シフト解除：対象バフがある場合、チャージ時間に応じた発射を行う
            if (projectileSneakStartTimes.containsKey(pid)) {
                BukkitTask task = projectileChargeTasks.remove(pid);
                if (task != null) {
                    task.cancel();
                }
                long startTime = projectileSneakStartTimes.remove(pid);
                int chargeSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
                // 発射処理：各バフごとに装備数をカウント（上限4個）
                int fireCount = countBuffs(player, SHIFT_FIREBUFF_ID);
                if (fireCount > 4) fireCount = 4;
                int bowCount = countBuffs(player, SHIFT_BOWBUFF_ID);
                if (bowCount > 4) bowCount = 4;

                // 発射するボルテージ＝ chargeSeconds（1秒ごとに1 volley）× 装備数
                int fireVolleys = chargeSeconds * fireCount;
                int bowVolleys = chargeSeconds * bowCount;

                if (fireVolleys > 0) {
                    spawnFireChargeVolleys(player, fireVolleys);
                    player.sendMessage(ChatColor.GOLD + "Fire Charge Buff: " + fireVolleys + " volley(s) fired in all directions!");
                }
                if (bowVolleys > 0) {
                    spawnArrowVolleys(player, bowVolleys);
                    player.sendMessage(ChatColor.GOLD + "Bow Buff: " + bowVolleys + " volley(s) fired in all directions!");
                }
                player.sendActionBar("");
            }
        }
    }

    /**
     * 対象スロット内に、いずれかの対象プロジェクタイルバフが装備されているかを返す
     */
    private boolean hasAnyProjectileBuff(Player player) {
        return (countBuffs(player, SHIFT_FIREBUFF_ID) > 0) || (countBuffs(player, SHIFT_BOWBUFF_ID) > 0);
    }

    /**
     * 対象スロット内に装備している、指定された buffId のバフアイテムの個数をカウントする
     */
    private int countBuffs(Player player, String buffId) {
        int count = 0;
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                BuffItemData data = BuffRegistry.getBuffItemById(buffId);
                if (data != null && data.matches(item)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 指定した回数（volley）だけ、全方位にファイヤーチャージ（SmallFireball）を発射する
     */
    private void spawnFireChargeVolleys(Player player, int volleys) {
        Location loc = player.getEyeLocation();
        // 各 volley 毎に、全方向（角度 step ごと）に発射
        for (int i = 0; i < volleys; i++) {
            for (double angle = 0; angle < 360; angle += ANGLE_STEP_DEG) {
                // 水平面上の単位ベクトルを計算
                double rad = Math.toRadians(angle);
                Vector direction = new Vector(Math.cos(rad), 0, Math.sin(rad));
                // 若干上向きにするため、y成分を加算（例：0.1）
                direction.setY(0.1);
                direction.normalize();
                // SmallFireball を発射
                SmallFireball fireball = (SmallFireball) player.getWorld().spawnEntity(loc, EntityType.SMALL_FIREBALL);
                fireball.setDirection(direction);
                // 召喚者のメタデータ設定（後でターゲット制御などに利用可能）
                fireball.setMetadata("projectileBuffOwner", new org.bukkit.metadata.FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            }
        }
    }

    /**
     * 指定した回数（volley）だけ、全方位に矢（Arrow）を発射する
     */
    private void spawnArrowVolleys(Player player, int volleys) {
        // 発射位置：プレイヤーの目の位置
        Location loc = player.getEyeLocation();
        for (int i = 0; i < volleys; i++) {
            for (double angle = 0; angle < 360; angle += ANGLE_STEP_DEG) {
                double rad = Math.toRadians(angle);
                Vector direction = new Vector(Math.cos(rad), 0.1, Math.sin(rad)); // 少し上向きに
                direction.normalize();
                // 矢を発射（launchProjectile は自動で矢の位置を調整）
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(direction.multiply(1.5)); // 速度調整（例：1.5倍）
                arrow.setShooter(player);
                arrow.setMetadata("projectileBuffOwner", new org.bukkit.metadata.FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            }
        }
    }
}
