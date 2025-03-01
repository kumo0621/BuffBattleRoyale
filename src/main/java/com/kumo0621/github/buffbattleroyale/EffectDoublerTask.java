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
 * バフがなくなった場合は、必ず元の効果レベルに戻します。
 */
public class EffectDoublerTask implements Runnable {

    // プレイヤーごとに、各エフェクトの「自然な」amplifier を記録するマップ
    // キー: プレイヤー UUID、値: (PotionEffectType -> 自然な amplifier)
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
            String uuid = player.getUniqueId().toString();
            boolean hasBuff = hasEffectDoubler(player);
            if (hasBuff) {
                // バフが有効な場合：各エフェクトについて、まだ記録されていなければ記録し、倍化を適用
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    Map<PotionEffectType, Integer> map = originalEffects.get(uuid);
                    if (map == null) {
                        map = new HashMap<>();
                        originalEffects.put(uuid, map);
                    }
                    // もし既に記録済みなら、その記録が自然な amplifier として利用される
                    if (!map.containsKey(effect.getType())) {
                        int originalAmp = effect.getAmplifier(); // 自然な効果レベル = originalAmp + 1
                        map.put(effect.getType(), originalAmp);
                    }
                    // 計算する desired amplifier = ((originalAmp + 1) * 2) - 1
                    int desiredAmp = (map.get(effect.getType()) + 1) * 2 - 1;
                    if (effect.getAmplifier() != desiredAmp) {
                        // 効果を置き換える（残り時間は effect.getDuration() をそのまま利用）
                        player.removePotionEffect(effect.getType());
                        player.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), desiredAmp, effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                    }
                }
            } else {
                // バフがなくなった場合：記録済みの効果について必ず自然な値に戻す
                if (originalEffects.containsKey(uuid)) {
                    Map<PotionEffectType, Integer> map = originalEffects.get(uuid);
                    // ※ 現在の全エフェクトではなく、記録されている効果タイプすべてに対して処理
                    for (Map.Entry<PotionEffectType, Integer> entry : map.entrySet()) {
                        PotionEffectType type = entry.getKey();
                        int originalAmp = entry.getValue();
                        if (player.hasPotionEffect(type)) {
                            PotionEffect active = player.getPotionEffect(type);
                            // もし現在の amplifier が自然な値と異なれば、必ず元に戻す
                            if (active.getAmplifier() != originalAmp) {
                                player.removePotionEffect(type);
                                player.addPotionEffect(new PotionEffect(type, active.getDuration(), originalAmp, active.isAmbient(), active.hasParticles(), active.hasIcon()));
                            }
                        }
                    }
                    // 記録を削除
                    originalEffects.remove(uuid);
                }
            }
        }
    }
}
