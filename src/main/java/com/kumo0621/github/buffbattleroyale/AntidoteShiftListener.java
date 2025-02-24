package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * プレイヤーがシフトを押すと、ホットバー（スロット0～8）に "antidote" バフがある場合、
 * 自身に付与されている毒（PotionEffectType.POISON）エフェクトを解除します。
 */
public class AntidoteShiftListener implements Listener {

    // チェック対象のホットバーのスロット範囲
    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    // 使用するバフID
    private static final String ANTIDOTE_BUFF_ID = "antidote";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // シフトが押されたときのみ処理（event.isSneaking() が true のとき）
        if (event.isSneaking()) {
            Player player = event.getPlayer();
            if (hasAntidoteBuff(player)) {
                // 毒エフェクトがあれば解除
                if (player.hasPotionEffect(PotionEffectType.POISON)) {
                    player.removePotionEffect(PotionEffectType.POISON);
                    player.sendMessage(ChatColor.GREEN + "Antidote Buff activated: Poison effect cleared!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "No poison effect found.");
                }
            }
        }
    }

    /**
     * プレイヤーのホットバー（スロット0～8）に "antidote" バフが存在するかチェックする
     */
    private boolean hasAntidoteBuff(Player player) {
        BuffItemData buff = BuffRegistry.getBuffItemById(ANTIDOTE_BUFF_ID);
        if (buff == null) return false;
        for (int slot = HOTBAR_SLOT_START; slot <= HOTBAR_SLOT_END; slot++) {
            if (player.getInventory().getItemInMainHand() != null &&
                BuffRegistry.getBuffItemById(ANTIDOTE_BUFF_ID).matches(player.getInventory().getItemInMainHand())) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item != null && buff.matches(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
