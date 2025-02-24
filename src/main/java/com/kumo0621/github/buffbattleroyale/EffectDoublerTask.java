package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;

/**
 * プレイヤーに付与されている全エフェクト（PotionEffect）の効果レベル（実際は amplifier+1）を、
 * インベントリのスロット0～8に "effectdoubler" バフがある場合、倍にします。
 * バフがなくなった場合は元のレベルに戻します。
 */
public class EffectDoublerTask implements Runnable {

    // プレイヤーごとに、各エフェクトの元の amplifier を記録するマップ
    // キー: プレイヤー UUID、値: (PotionEffectType -> 元の amplifier)
    private static final Map<String, Map<PotionEffectType, Integer>> originalEffects = new HashMap<>();

    /**
     * プレイヤーのインベントリのスロット0～8に "effectdoubler" バフがあるかチェックします。
     */
    private boolean hasEffectDoubler(Player player) {
        BuffItemData doubler = BuffRegistry.getBuffItemById("effectdoubler");
        if (doubler == null) return false;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot) != null &&
                    doubler.matches(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean hasBuff = hasEffectDoubler(player);
            String uuid = player.getUniqueId().toString();
            if (hasBuff) {
                // バフが有効な場合、プレイヤーに付与されている各エフェクトについて処理
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    // プレイヤーごとに元の値を記録するマップを取得（なければ作成）
                    Map<PotionEffectType, Integer> map = originalEffects.get(uuid);
                    if (map == null) {
                        map = new HashMap<>();
                        originalEffects.put(uuid, map);
                    }
                    // 未記録の場合のみ、元の amplifier を記録し、効果レベルを倍にする
                    if (!map.containsKey(effect.getType())) {
                        int originalAmp = effect.getAmplifier(); // 効果レベル = originalAmp + 1
                        map.put(effect.getType(), originalAmp);
                        // 新 amplifier = ((originalAmp + 1) * 2) - 1
                        int newAmp = (originalAmp + 1) * 2 - 1;
                        player.removePotionEffect(effect.getType());
                        player.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), newAmp, effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                    }
                }
            } else {
                // バフがなくなった場合、記録済みの元の値があれば元に戻す
                if (originalEffects.containsKey(uuid)) {
                    Map<PotionEffectType, Integer> map = originalEffects.get(uuid);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (map.containsKey(effect.getType())) {
                            int originalAmp = map.get(effect.getType());
                            if (effect.getAmplifier() != originalAmp) {
                                player.removePotionEffect(effect.getType());
                                player.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), originalAmp, effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                            }
                        }
                    }
                    originalEffects.remove(uuid);
                }
            }
        }
    }
}
