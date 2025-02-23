package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Random;

public class ToxicEffectListener implements Listener {

    // 対象スロットは0～8
    private static final int SLOT_START = 0;
    private static final int SLOT_END = 8;

    // 基本毒付与確率：1%
    private static final double BASE_CHANCE = 0.01;
    // 1個あたり追加される確率：3%
    private static final double PER_BUFF_CHANCE = 0.03;

    // 毒効果の持続時間：5秒（5×20=100 tick）
    private static final int POISON_DURATION = 100;

    private final Random random = new Random();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 攻撃者がプレイヤーであるかチェック
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player attacker = (Player) event.getDamager();

        // インベントリのスロット0～8から toxic バフアイテムの個数をカウントする
        int toxicCount = 0;
        for (int slot = SLOT_START; slot <= SLOT_END; slot++) {
            ItemStack item = attacker.getInventory().getItem(slot);
            if (item != null) {
                BuffItemData toxicBuff = BuffRegistry.getBuffItemById("toxic");
                if (toxicBuff != null && toxicBuff.matches(item)) {
                    toxicCount++;
                }
            }
        }

        // 合計確率 = 基本1% + (1個あたり3% × toxicCount)
        double chance = BASE_CHANCE + toxicCount * PER_BUFF_CHANCE;

        // ランダム判定
        if (random.nextDouble() < chance) {
            // ダメージ対象が LivingEntity なら毒効果を付与
            if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
                org.bukkit.entity.LivingEntity victim = (org.bukkit.entity.LivingEntity) event.getEntity();
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, POISON_DURATION, 0));
                attacker.sendMessage("Toxic effect applied to " + victim.getName() + "!");
            }
        }
    }
}
