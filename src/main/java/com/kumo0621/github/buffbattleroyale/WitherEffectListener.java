package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Random;

public class WitherEffectListener implements Listener {

    // 対象スロットは0～8（メインハンド以外でも発動）
    private static final int SLOT_START = 0;
    private static final int SLOT_END = 8;
    // 1個あたりの付与確率（5%）
    private static final double PER_BUFF_CHANCE = 0.05;
    // 毒効果の持続時間：2秒（2×20 = 40 tick）
    private static final int WITHER_DURATION = 40;

    private final Random random = new Random();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 攻撃者がプレイヤーでなければ処理しない
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player attacker = (Player) event.getDamager();

        // インベントリのスロット0～8から "witherbuff" アイテムの個数をカウント
        int buffCount = 0;
        for (int slot = SLOT_START; slot <= SLOT_END; slot++) {
            ItemStack item = attacker.getInventory().getItem(slot);
            if (item != null) {
                BuffItemData witherBuff = BuffRegistry.getBuffItemById("witherbuff");
                if (witherBuff != null && witherBuff.matches(item)) {
                    buffCount++;
                }
            }
        }

        // 合計付与確率 = buffCount * 5%
        double totalChance = buffCount * PER_BUFF_CHANCE;

        // ランダム判定で発動（重複可能：buffCount個あるほど高確率に）
        if (random.nextDouble() < totalChance) {
            // ダメージ対象が LivingEntity ならウィザー効果を付与（2秒間）
            if (event.getEntity() instanceof LivingEntity) {
                LivingEntity victim = (LivingEntity) event.getEntity();
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, WITHER_DURATION, 0));
                attacker.sendMessage("Wither effect applied to " + victim.getName() + " (" + (totalChance*100) + "% chance)!");
            }
        }
    }
}
