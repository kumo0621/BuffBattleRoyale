package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * プレイヤーがシフトを押すと、ホットバー（スロット0～8）に "witherantidote" バフがある場合、
 * 自身に付与されているウィザー効果（PotionEffectType.WITHER）を解除します。
 */
public class WitherAntidoteShiftListener implements Listener {

    // チェック対象のホットバーのスロット範囲
    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    // 使用するバフID
    private static final String WITHER_ANTIDOTE_BUFF_ID = "witherantidote";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // シフトが押されたときのみ処理（event.isSneaking() が true のとき）
        if (event.isSneaking()) {
            Player player = event.getPlayer();
            if (hasWitherAntidoteBuff(player)) {
                if (player.hasPotionEffect(PotionEffectType.WITHER)) {
                    player.removePotionEffect(PotionEffectType.WITHER);
                    player.sendMessage(ChatColor.GREEN + "Wither Antidote Buff activated: Wither effect cleared!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "No wither effect found.");
                }
            }
        }
    }

    /**
     * プレイヤーのホットバー（スロット0～8）に "witherantidote" バフが存在するかチェックする
     */
    private boolean hasWitherAntidoteBuff(Player player) {
        BuffItemData buff = BuffRegistry.getBuffItemById(WITHER_ANTIDOTE_BUFF_ID);
        if (buff == null) return false;
        for (int slot = HOTBAR_SLOT_START; slot <= HOTBAR_SLOT_END; slot++) {
            if (player.getInventory().getItemInMainHand() != null &&
                BuffRegistry.getBuffItemById(WITHER_ANTIDOTE_BUFF_ID).matches(player.getInventory().getItemInMainHand())) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null && buff.matches(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
