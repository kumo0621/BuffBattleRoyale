package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ShiftFillChestListener implements Listener {
    // プレイヤーのシフト開始時刻を記録するマップ
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    // プレイヤーごとの ActionBar 表示用タスク
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();
    // 対象スロット（9～17番）
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;
    // 対象バフアイテムのID（Shift FillChest Buff）
    private static final String FILL_CHEST_BUFF_ID = "shiftfillchest";
    // 3分 = 180秒
    private static final int THRESHOLD_SECONDS = 60;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始：対象スロットに「shiftfillchest」バフが装備されていれば開始時刻を記録し、ActionBar 表示
            if (hasFillChestBuff(player)) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsedMillis = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsedMillis / 1000);
                    player.sendActionBar(ChatColor.LIGHT_PURPLE + "FillChest Charge: " + seconds + " sec");
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
                if (durationSeconds >= THRESHOLD_SECONDS) {
                    // 3分以上保持していた場合、ランダムなバフアイテムをプレイヤーに与える
                    giveRandomBuffItem(player);
                    player.sendMessage(ChatColor.GREEN + "FillChest Buff 発動！ランダムなバフアイテムを受け取りました。");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "シフト押下時間が足りませんでした。（" + durationSeconds + "秒）");
                }
                player.sendActionBar("");
            }
        }
    }

    /**
     * 対象スロット内に "shiftfillchest" バフアイテムが装備されているかチェック
     */
    private boolean hasFillChestBuff(Player player) {
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                if (player.getInventory().getItemInMainHand() != null &&
                    BuffRegistry.getBuffItemById(FILL_CHEST_BUFF_ID).matches(player.getInventory().getItemInMainHand())) {
                    BuffItemData data = BuffRegistry.getBuffItemById(FILL_CHEST_BUFF_ID);
                    if (data != null && data.matches(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * BuffRegistry からランダムなバフアイテムを1点取得して、プレイヤーに与える。
     */
    private void giveRandomBuffItem(Player player) {
        BuffItemData randomBuff = getRandomBuffItem();
        if (randomBuff != null) {
            org.bukkit.inventory.ItemStack buffItem = randomBuff.createItemStack();
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(buffItem);
                player.sendMessage(ChatColor.GREEN + "ランダムバフアイテム [" + randomBuff.getId() + "] を受け取りました。");
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), buffItem);
                player.sendMessage(ChatColor.YELLOW + "インベントリが満杯だったため、ランダムバフアイテム [" + randomBuff.getId() + "] をドロップしました。");
            }
        } else {
            player.sendMessage(ChatColor.RED + "ランダムバフアイテムが見つかりませんでした。");
        }
    }

    /**
     * BuffRegistry からランダムなバフアイテムを1点取得する。
     */
    private BuffItemData getRandomBuffItem() {
        java.util.List<BuffItemData> allBuffs = BuffRegistry.getRegisteredBuffItems();
        if (allBuffs.isEmpty()) return null;
        Random rand = new Random();
        return allBuffs.get(rand.nextInt(allBuffs.size()));
    }
}
