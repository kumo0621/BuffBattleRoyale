package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShiftSkeletonSummonListener implements Listener {

    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    private final Map<UUID, BukkitTask> chargeTasks = new HashMap<>();

    private static final String SHIFT_SKELETON_BUFF_ID = "shiftskeleton";
    private static final int BUFF_SLOT_START = 0;
    private static final int BUFF_SLOT_END = 8;

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (event.isSneaking()) {
            if (hasShiftSkeletonBuff(player)) {
                long startTime = System.currentTimeMillis();
                sneakStartTimes.put(playerId, startTime);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(BuffBattleRoyale.getInstance(), () -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int)(elapsed / 1000);
                    player.sendActionBar(ChatColor.GRAY + "Skeleton Charge: " + seconds + " sec");
                }, 0L, 20L);
                chargeTasks.put(playerId, task);
            }
        } else {
            if (sneakStartTimes.containsKey(playerId)) {
                BukkitTask task = chargeTasks.remove(playerId);
                if (task != null) task.cancel();
                long duration = System.currentTimeMillis() - sneakStartTimes.remove(playerId);
                int seconds = (int)(duration / 1000);
                player.sendActionBar("");
                if (seconds >= 5) {
                    int groups = seconds / 5;
                    int base = (groups >= 1) ? 3 * (int)Math.pow(2, groups - 1) : 0;
                    int buffCount = countShiftSkeletonBuffs(player);
                    if (buffCount > 4) buffCount = 4;
                    int total = base * buffCount;
                    if (total > 0) {
                        spawnSkeletonsForPlayer(player, total);
                        player.sendMessage(ChatColor.GRAY + "Skeleton Summon: " + seconds + " sec charge -> " + total + " Skeletons summoned.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Charge too short, no skeletons summoned.");
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Charge too short, no skeletons summoned.");
                }
            }
        }
    }

    private boolean hasShiftSkeletonBuff(Player player) {
        return countShiftSkeletonBuffs(player) > 0;
    }

    private int countShiftSkeletonBuffs(Player player) {
        int count = 0;
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                if (player.getInventory().getItemInMainHand() != null &&
                    BuffRegistry.getBuffItemById(SHIFT_SKELETON_BUFF_ID).matches(player.getInventory().getItemInMainHand())) {
                    BuffItemData buff = BuffRegistry.getBuffItemById(SHIFT_SKELETON_BUFF_ID);
                    if (buff != null && buff.matches(item)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void spawnSkeletonsForPlayer(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Skeleton skeleton = (Skeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON);
            skeleton.setMetadata("shiftSummonOwner", new FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            skeleton.setCustomName(player.getName());
            skeleton.setCustomNameVisible(true);
        }
    }
}
