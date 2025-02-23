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
import java.util.UUID;

public class GlobalGlowListener implements Listener {
    // プレイヤーのグローバル発光バフ開始時刻を記録するマップ
    private final Map<UUID, Long> startTimes = new HashMap<>();
    // ActionBar 表示用タスク
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    // 対象スロット（9～17）
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;
    // 対象バフID：Shift Global Glow Buff
    private static final String GLOBAL_GLOW_BUFF_ID = "shiftglobalglow";
    // 発光拒否バフID
    private static final String GLOW_REJECT_BUFF_ID = "glowreject";

    // 発光効果の持続時間（秒）
    private static final int GLOW_DURATION_SEC = 10;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player summoner = event.getPlayer();
        UUID summonerId = summoner.getUniqueId();
        if (event.isSneaking()) {
            // シフト開始：対象スローブに「shiftglobalglow」バフを持っているかチェック
            if (hasBuff(summoner, GLOBAL_GLOW_BUFF_ID)) {
                long now = System.currentTimeMillis();
                startTimes.put(summonerId, now);
                // ActionBar 表示（任意）
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - now;
                    int seconds = (int)(elapsed / 1000);
                    summoner.sendActionBar(ChatColor.DARK_PURPLE + "Global Glow Charge: " + seconds + " sec");
                }, 0L, 20L);
                tasks.put(summonerId, task);
            }
        } else {
            // シフト解除：対象バフを持っていれば、効果を発動
            if (startTimes.containsKey(summonerId)) {
                BukkitTask task = tasks.remove(summonerId);
                if (task != null) {
                    task.cancel();
                }
                startTimes.remove(summonerId);
                // 発光効果を、summoner以外の全オンラインプレイヤーに付与
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (target.equals(summoner)) continue; // 自分には付与しない
                    if (!hasBuff(target, GLOW_REJECT_BUFF_ID)) {
                        target.setGlowing(true);
                        // 10秒後に解除するタスク
                        Bukkit.getScheduler().runTaskLater(BuffBattleRoyale.getInstance(), () -> {
                            target.setGlowing(false);
                        }, GLOW_DURATION_SEC * 20L);
                    }
                }
                summoner.sendMessage(ChatColor.GREEN + "Global Glow activated: all eligible players are now glowing for " + GLOW_DURATION_SEC + " seconds.");
                summoner.sendActionBar("");
            }
        }
    }

    /**
     * 対象スローブ内に指定したバフアイテム（buffId）が装備されているかチェック
     */
    private boolean hasBuff(Player player, String buffId) {
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                if (player.getInventory().getItemInMainHand() != null &&
                    BuffRegistry.getBuffItemById("shiftglobalglow").matches(player.getInventory().getItemInMainHand())) {
                    BuffItemData data = BuffRegistry.getBuffItemById(buffId);
                    if (data != null && data.matches(item)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
