package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * プレイヤーがシフトを押すと、ホットバー（スロット0～8）に "glowantidote" バフがある場合、
 * 自身に付与されている発光状態を解除します。
 */
public class GlowAntidoteShiftListener implements Listener {

    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    private static final String GLOW_ANTIDOTE_BUFF_ID = "glowantidote";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // シフトが押されたときのみ処理（isSneaking() が true のとき）
        if (event.isSneaking()) {
            Player player = event.getPlayer();
            if (hasGlowAntidoteBuff(player)) {
                // 発光状態が有効なら解除
                if (player.isGlowing()) {
                    player.setGlowing(false);
                    player.sendMessage(ChatColor.GREEN + "Glow Antidote Buff activated: Glow effect cleared!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "No glowing effect found.");
                }
            }
        }
    }

    /**
     * プレイヤーのホットバー（スロット0～8）に "glowantidote" バフが存在するかチェックします。
     */
    private boolean hasGlowAntidoteBuff(Player player) {
        BuffItemData buff = BuffRegistry.getBuffItemById(GLOW_ANTIDOTE_BUFF_ID);
        if (buff == null) return false;
        for (int slot = HOTBAR_SLOT_START; slot <= HOTBAR_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && buff.matches(item)) {
                return true;
            }
        }
        return false;
    }
}
