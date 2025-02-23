package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftCreeperSummonListener implements Listener {

    // プレイヤーのシフト開始時刻を記録するマップ
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    // プレイヤーごとの ActionBar 表示タスク
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();

    // 対象スロット（9～17番）
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;

    // 召喚バフとして登録している buff ID の一覧
    private static final String[] SUMMON_BUFF_IDS = {
            "shiftcreeper", "shiftzombie", "shiftskeleton", "shiftbabyzombie"
    };

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始：対象バフが1つでもあるなら開始時刻を記録し、ActionBar 表示開始
            if (hasAnySummonBuff(player)) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsedMillis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedMillis / 1000);
                    player.sendActionBar(ChatColor.AQUA + "Charge Time: " + seconds + " sec");
                }, 0L, 20L);
                chargeTasks.put(playerId, task);
            }
        } else {
            // シフト解除：記録があれば処理
            if (sneakStartTimes.containsKey(playerId)) {
                BukkitTask task = chargeTasks.remove(playerId);
                if (task != null) {
                    task.cancel();
                }
                long startTime = sneakStartTimes.remove(playerId);
                long durationMillis = System.currentTimeMillis() - startTime;
                int durationSeconds = (int) (durationMillis / 1000);
                int groups = durationSeconds / 15;
                int baseCount = 0;
                if (groups >= 1) {
                    baseCount = 3 * (int) Math.pow(2, groups - 1);
                }
                // 各召喚バフについて、対象スロット内の個数（上限4個）をカウントし、
                // それぞれに対して、召喚数 = baseCount × (カウント)
                for (String buffId : SUMMON_BUFF_IDS) {
                    int count = countSummonBuffs(player, buffId);
                    if (count > 4) count = 4;
                    int spawnCount = baseCount * count;
                    if (spawnCount > 0) {
                        spawnMobsForPlayer(player, spawnCount, buffId);
                        player.sendMessage(ChatColor.GREEN + "シフト押下時間 " + durationSeconds
                                + "秒 → " + spawnCount + "体の " + getMobName(buffId) + " を召喚しました。");
                    }
                }
                player.sendActionBar("");
            }
        }
    }

    /**
     * 対象スロット内に、指定された召喚バフアイテム（buffId）がいくつあるかカウントする
     */
    private int countSummonBuffs(Player player, String buffId) {
        int count = 0;
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                if (player.getInventory().getItemInMainHand() != null &&
                        java.util.Arrays.stream(SUMMON_BUFF_IDS)
                                .map(BuffRegistry::getBuffItemById)
                                .filter(java.util.Objects::nonNull)
                                .anyMatch(data -> data.matches(player.getInventory().getItemInMainHand()))) {
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
     * 対象スロット内に、いずれかの召喚バフアイテムが装備されているか
     */
    private boolean hasAnySummonBuff(Player player) {
        for (String buffId : SUMMON_BUFF_IDS) {
            if (countSummonBuffs(player, buffId) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * buffId に応じたモブタイプで、指定した数のモブを召喚する。
     * "shiftcreeper" → クリーパー
     * "shiftzombie" → ゾンビ
     * "shiftskeleton" → スケルトン
     * "shiftbabyzombie" → ベイビーゾンビ（ゾンビで setBaby(true)）
     */
    private void spawnMobsForPlayer(Player player, int count, String buffId) {
        EntityType type;
        boolean baby = false;
        switch (buffId.toLowerCase()) {
            case "shiftcreeper":
                type = EntityType.CREEPER;
                break;
            case "shiftzombie":
                type = EntityType.ZOMBIE;
                break;
            case "shiftskeleton":
                type = EntityType.SKELETON;
                break;
            case "shiftbabyzombie":
                type = EntityType.ZOMBIE;
                baby = true;
                break;
            default:
                return;
        }
        for (int i = 0; i < count; i++) {
            // 召喚
            org.bukkit.entity.Entity entity = player.getWorld().spawnEntity(player.getLocation(), type);
            // 共通：召喚者のUUIDをメタデータ "shiftSummonOwner" として設定
            entity.setMetadata("shiftSummonOwner", new FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            // 召喚したプレイヤー名をモブの名前に設定（常に表示）
            if (entity instanceof org.bukkit.entity.Creeper ||
                    entity instanceof org.bukkit.entity.Skeleton ||
                    entity instanceof org.bukkit.entity.Zombie) {
                org.bukkit.entity.LivingEntity mob = (org.bukkit.entity.LivingEntity) entity;
                mob.setCustomName(player.getName());
                mob.setCustomNameVisible(true);
                // ベイビーゾンビの場合は設定
                if (baby && mob instanceof Zombie) {
                    ((Zombie) mob).setBaby(true);
                }
            }
        }
    }

    /**
     * buffId に応じたモブの表示名を返す（メッセージ用）
     */
    private String getMobName(String buffId) {
        switch (buffId.toLowerCase()) {
            case "shiftcreeper":
                return "クリーパー";
            case "shiftzombie":
                return "ゾンビ";
            case "shiftskeleton":
                return "スケルトン";
            case "shiftbabyzombie":
                return "チビゾンビ";
            default:
                return "モブ";
        }
    }
}
