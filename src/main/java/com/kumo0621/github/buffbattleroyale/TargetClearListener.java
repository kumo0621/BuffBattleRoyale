package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetClearListener implements Listener {

    // メインハンドに持っている場合のみ対象（スニーク中にチェック）
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    private static final long THRESHOLD_MS = 1000;
    private static final double RANGE = 3.0;
    private static final String TARGET_CLEAR_BUFF_ID = "targetclear";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            if (hasTargetClearBuffInMainHand(player)) {
                sneakStartTimes.put(playerId, System.currentTimeMillis());
            }
        } else {
            if (sneakStartTimes.containsKey(playerId)) {
                long duration = System.currentTimeMillis() - sneakStartTimes.remove(playerId);
                if (duration >= THRESHOLD_MS && hasTargetClearBuffInMainHand(player)) {
                    Collection<Entity> nearby = player.getNearbyEntities(RANGE, RANGE, RANGE);
                    int cleared = 0;
                    for (Entity entity : nearby) {
                        if (entity instanceof Creature) {
                            Creature mob = (Creature) entity;
                            if (mob.getTarget() != null && mob.getTarget().equals(player)) {
                                mob.setTarget(null);
                                // 以下のメタデータを設定して、今後このプレイヤーをターゲットしないようにする
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

    private boolean hasTargetClearBuffInMainHand(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null) {
            BuffItemData data = BuffRegistry.getBuffItemById(TARGET_CLEAR_BUFF_ID);
            return data != null && data.matches(mainHand);
        }
        return false;
    }
}
