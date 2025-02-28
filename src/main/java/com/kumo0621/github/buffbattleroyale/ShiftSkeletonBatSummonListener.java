package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftSkeletonBatSummonListener implements Listener {

    // プレイヤーごとのシフト開始時刻と ActionBar タスクを管理
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();

    // 対象バフID（BuffRegistry に "skeletonbat" として登録されていること）
    private static final String SKELETONBAT_BUFF_ID = "skeletonbat";
    // チェック対象のホットバーはスロット 0～8
    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    // 発動条件：シフトを最低15秒以上押し続ける
    private static final int MIN_CHARGE_SECONDS = 15;
    // 召喚上限（総体数の上限）
    private static final int MAX_SUMMON = 15;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始：メインハンドに対象バフがあり、かつホットバー内で他に同じバフがない場合のみ
            if (hasSkeletonBatBuffInMainHandOnly(player)) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsed / 1000);
                    player.sendActionBar(ChatColor.BLUE + "SkeletonBat Charge: " + seconds + " sec");
                }, 0L, 20L);
                chargeTasks.put(playerId, task);
            }
        } else {
            // シフト解除：もし開始時刻が記録されていれば
            if (sneakStartTimes.containsKey(playerId)) {
                BukkitTask task = chargeTasks.remove(playerId);
                if (task != null) task.cancel();
                long duration = System.currentTimeMillis() - sneakStartTimes.remove(playerId);
                int seconds = (int) (duration / 1000);
                player.sendActionBar("");
                if (seconds >= MIN_CHARGE_SECONDS) {
                    // groups: 15秒毎に1グループ
                    int groups = seconds / 5;
                    // 基本召喚数 = 3 × 2^(groups - 1) （例：15秒→3, 30秒→6, 45秒→12, 60秒→24）
                    int base = (groups >= 1) ? 3 * (int) Math.pow(2, groups - 1) : 0;
                    // 召喚総数 = 基本召喚数（上限 MAX_SUMMON）
                    int total = Math.min(base, MAX_SUMMON);
                    if (total > 0) {
                        spawnSkeletonBatsForPlayer(player, total);
                        player.sendMessage(ChatColor.GREEN + "SkeletonBat Summon: " + seconds + " sec charge -> " + total + " skeletons on bats summoned.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Charge too short, no skeleton bats summoned.");
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Charge too short, no skeleton bats summoned.");
                }
            }
        }
    }

    /**
     * プレイヤーのメインハンドに対象バフアイテムがあり、さらにホットバー（0～8）内にメインハンド以外で同じバフが存在しないかをチェックする。
     */
    private boolean hasSkeletonBatBuffInMainHandOnly(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        BuffItemData buffData = BuffRegistry.getBuffItemById(SKELETONBAT_BUFF_ID);
        if (mainHand == null || buffData == null) return false;
        if (!buffData.matches(mainHand)) return false;
        int heldSlot = player.getInventory().getHeldItemSlot();
        // ホットバー内の他のスロットに同じバフが存在する場合は無効
        for (int slot = HOTBAR_SLOT_START; slot <= HOTBAR_SLOT_END; slot++) {
            if (slot == heldSlot) continue;
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && buffData.matches(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * プレイヤーの位置に、Bat に乗った Skeleton を total 体召喚します。
     */
    private void spawnSkeletonBatsForPlayer(Player player, int total) {
        Location loc = player.getLocation();
        for (int i = 0; i < total; i++) {
            // Bat の召喚
            Bat bat = (Bat) player.getWorld().spawnEntity(loc, EntityType.BAT);
            // Skeleton の召喚
            Skeleton skeleton = (Skeleton) player.getWorld().spawnEntity(loc, EntityType.SKELETON);
            // Bat に Skeleton を乗せる
            bat.addPassenger(skeleton);
            // 召喚された Skeleton の名前を、召喚したプレイヤーの名前に設定
            skeleton.setCustomName(player.getName());
            skeleton.setCustomNameVisible(true);
        }
    }
}
