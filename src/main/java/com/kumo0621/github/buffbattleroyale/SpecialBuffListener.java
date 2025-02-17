package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 攻撃時に、対象スロット内に存在する特殊効果付きバフアイテムを判定し、
 * 対応する特殊効果を適用します。
 */
public class SpecialBuffListener implements Listener {
    // 対象スロット（9～17番）
    private final int BUFF_SLOT_START = 9;
    private final int BUFF_SLOT_END = 17;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 攻撃者がプレイヤーでない場合は無視
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Entity victim = event.getEntity();

        // 対象スロットを確認し、特殊効果付きバフアイテムがあれば適用
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = attacker.getInventory().getItem(slot);
            if (item != null) {
                for (BuffItemData buffData : BuffRegistry.getRegisteredBuffItems()) {
                    if (buffData.matches(item) && buffData.getSpecialEffect() != null) {
                        buffData.getSpecialEffect().applyEffect(attacker, victim, buffData);
                    }
                }
            }
        }
    }
}
