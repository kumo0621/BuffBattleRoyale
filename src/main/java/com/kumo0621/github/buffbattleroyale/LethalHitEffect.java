package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * 特殊効果：殴ったら 1% の確率で相手を殺す効果。
 * このクラスを実装することで、対象プレイヤーが攻撃時に一定確率で即死効果を発動します。
 */
public class LethalHitEffect implements SpecialEffect {
    private final double chance; // 例：0.01 は 1% の確率

    public LethalHitEffect(double chance) {
        this.chance = chance;
    }

    /**
     * 殺傷確率を返します。
     *
     * @return chance（例：0.01 なら 1%）
     */
    public double getChance() {
        return chance;
    }

    @Override
    public void applyEffect(Player attacker, Entity victim, BuffItemData buffData) {
        if (victim instanceof LivingEntity) {
            double random = Math.random();
            if (random < chance) {
                ((LivingEntity) victim).setHealth(0.0);
                attacker.sendMessage(ChatColor.GOLD + "Lethal Hit 発動！相手を即死させました。");
            }
        }
    }
}
