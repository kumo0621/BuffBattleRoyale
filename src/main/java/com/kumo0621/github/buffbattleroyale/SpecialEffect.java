package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * インターフェース：特殊効果を表す。
 * バフアイテムに特殊効果を持たせたい場合、このインターフェースを実装してください。
 */
public interface SpecialEffect {
    /**
     * 特殊効果を適用します。
     *
     * @param attacker 効果を発動するプレイヤー（攻撃者）
     * @param victim 効果の対象となる Entity
     * @param buffData この効果を持つバフアイテムのデータ
     */
    void applyEffect(Player attacker, Entity victim, BuffItemData buffData);
}
