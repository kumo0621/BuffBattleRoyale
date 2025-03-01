package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class ShiftTeleportListener implements Listener {
    // 有効なバフスロット（0～8）
    private final int BUFF_SLOT_START = 0;
    private final int BUFF_SLOT_END = 8;

    // プレイヤーごとのシフト開始時刻と ActionBar 用タスクを保持
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();
    // シフト解除後の連続テレポート用タスク（プレイヤーごと）
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();

    private final Random random = new Random();

    // 発動条件：シフトを最低15秒以上押し続ける
    private static final int MIN_CHARGE_SECONDS = 15;
    // 追加テレポートの間隔（200 ticks = 10秒）
    private static final long TELEPORT_INTERVAL = 200L;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        // アドベンチャーモード以外は処理しない
        if (!player.getGameMode().equals(GameMode.ADVENTURE)) {
            return;
        }
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            // シフト開始：ホットバー（0～8）に "teleport" バフがあるかチェック
            int buffCount = countTeleportBuffs(player);
            if (buffCount > 0) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                // ActionBar でチャージ時間を表示（1秒毎）
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int sec = (int)(elapsed / 1000);
                    player.sendActionBar(ChatColor.LIGHT_PURPLE + "Teleport Charge: " + sec + " sec");
                }, 0L, 20L);
                chargeTasks.put(playerId, task);
            }
        } else {
            // シフト解除：記録があれば処理
            if (sneakStartTimes.containsKey(playerId)) {
                BukkitTask chargeTask = chargeTasks.remove(playerId);
                if (chargeTask != null) {
                    chargeTask.cancel();
                }
                long heldMillis = System.currentTimeMillis() - sneakStartTimes.remove(playerId);
                int heldSec = (int)(heldMillis / 1000);
                player.sendActionBar("");

                if (heldSec >= MIN_CHARGE_SECONDS) {
                    int buffCount = countTeleportBuffs(player);
                    if (buffCount > 0) {
                        // 初回テレポート：ランダムな他のオンラインプレイヤーへ移動
                        teleportToRandomPlayer(player, null);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Initial teleport executed!");

                        // 装備しているテレポートバフの個数（最大4個）に応じて、10秒おきに連続テレポートを実行
                        final int totalTeleports = buffCount; // バフ数がテレポート回数
                        final int[] countDone = {0};
                        BukkitTask tpTask = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                            teleportToRandomPlayer(player, null);
                            countDone[0]++;
                            if (countDone[0] >= totalTeleports) {
                                Bukkit.getScheduler().cancelTask(teleportTasks.get(playerId).getTaskId());
                                teleportTasks.remove(playerId);
                            }
                        }, TELEPORT_INTERVAL, TELEPORT_INTERVAL);
                        teleportTasks.put(playerId, tpTask);
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Teleport buff activated: You will be teleported " + totalTeleports + " additional times at 10 sec intervals.");
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Teleport charge insufficient (hold at least 15 sec).");
                }
            }
        }
    }

    /**
     * ホットバー（0～8）にある "teleport" バフの個数をカウントする。
     * ※ このバフは BuffRegistry に "teleport" として登録されているものとします。
     */
    private int countTeleportBuffs(Player player) {
        int count = 0;
        BuffItemData buffData = BuffRegistry.getBuffItemById("teleport");
        if (buffData == null) return 0;
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && buffData.matches(item)) {
                count++;
            }
        }
        return Math.min(count, 4);
    }

    /**
     * ランダムな他のオンラインプレイヤーへテレポートする（前回のターゲットと同じなら再度選ぶ）。
     * lastTarget は前回のターゲットを除外するために利用できますが、null であれば無視します。
     */
    private void teleportToRandomPlayer(Player player, Player lastTarget) {
        List<Player> others = new ArrayList<>(Bukkit.getOnlinePlayers());
        others.remove(player);
        if (others.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No other players available for teleportation.");
            return;
        }
        Player target;
        if (lastTarget != null && others.size() > 1) {
            do {
                target = others.get(random.nextInt(others.size()));
            } while (target.equals(lastTarget));
        } else {
            target = others.get(random.nextInt(others.size()));
        }
        player.teleport(target.getLocation());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Teleported to " + target.getName() + "!");
    }
}
