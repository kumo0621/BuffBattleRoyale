package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ZombieDamageHealListener implements Listener {

    // ホットバーの対象スロット（0～8）
    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    // 使用するバフID
    private static final String LIFEREGEN_BUFF_ID = "liferegen";
    // 回復量（4＝2ハート分、必要に応じて調整してください）
    private static final double HEAL_AMOUNT = 4.0;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 攻撃対象がプレイヤーで、攻撃者がゾンビの場合のみ処理
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Zombie)) return;
        Player player = (Player) event.getEntity();

        // プレイヤーのホットバー（スロット0～8）に "liferegen" バフがあるかチェック
        if (hasLiferegenBuff(player)) {
            double newHealth = Math.min(player.getHealth() + HEAL_AMOUNT, player.getMaxHealth());
            player.setHealth(newHealth);
            player.sendMessage(ChatColor.GREEN + "Liferegen Buff activated: Health restored by " + HEAL_AMOUNT + ".");
        }
    }

    /**
     * プレイヤーのホットバー（スロット0～8）に "liferegen" バフが存在するかチェックする
     */
    private boolean hasLiferegenBuff(Player player) {
        BuffItemData buff = BuffRegistry.getBuffItemById(LIFEREGEN_BUFF_ID);
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
