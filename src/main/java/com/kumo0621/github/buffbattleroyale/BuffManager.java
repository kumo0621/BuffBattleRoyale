package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.Map;

/**
 * プレイヤーインベントリの対象スロット（9～17番）を監視し、
 * 登録されたバフアイテムのうち、ポーション効果として付与すべきもの（effectType が null でないもの）の
 * バフレベルを合計して、その合計値に応じた PotionEffect を付与します。
 */
public class BuffManager {

    private static BuffManager instance;
    private BukkitTask task;
    // maxBuffLevel > 0 の場合は効果の上限、-1 の場合は上限なし
    private int maxBuffLevel = -1;

    // 対象スロット（9～17番）
    private final int BUFF_SLOT_START = 9;
    private final int BUFF_SLOT_END = 17;

    private BuffManager() {
    }

    public static BuffManager getInstance() {
        if (instance == null) {
            instance = new BuffManager();
        }
        return instance;
    }

    /**
     * プラグインインスタンスに対して定期タスクを開始します。
     *
     * @param plugin プラグインインスタンス
     */
    public void start(Plugin plugin) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerBuff(player);
            }
        }, 0L, 20L);
    }

    /**
     * 定期タスクを停止します。
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * 対象スロット内のバフアイテムのうち、ポーション効果（effectType が null でないもの）の
     * buffLevel を合計し、その合計値に応じた PotionEffect をプレイヤーに付与します。
     *
     * @param player 対象のプレイヤー
     */
    private void updatePlayerBuff(Player player) {
        Map<PotionEffectType, Integer> effectLevels = new HashMap<>();
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                for (BuffItemData buffData : BuffRegistry.getRegisteredBuffItems()) {
                    if (buffData.matches(item)) {
                        // ポーション効果として付与するもののみ対象
                        if (buffData.getEffectType() != null) {
                            PotionEffectType effectType = buffData.getEffectType();
                            int current = effectLevels.getOrDefault(effectType, 0);
                            effectLevels.put(effectType, current + buffData.getBuffLevel());
                        }
                    }
                }
            }
        }

        // 各効果をプレイヤーに付与
        for (Map.Entry<PotionEffectType, Integer> entry : effectLevels.entrySet()) {
            int totalLevel = entry.getValue();
            if (maxBuffLevel > 0) {
                totalLevel = Math.min(totalLevel, maxBuffLevel);
            }
            // PotionEffect の amplifier は (合計バフレベル - 1)
            PotionEffect effect = new PotionEffect(entry.getKey(), 40, totalLevel - 1, false, false, false);
            player.addPotionEffect(effect, true);
        }

        // 対象スロット内に該当する効果がなくなった場合は解除
        for (BuffItemData buffData : BuffRegistry.getRegisteredBuffItems()) {
            if (buffData.getEffectType() != null) {
                if (!effectLevels.containsKey(buffData.getEffectType())) {
                    player.removePotionEffect(buffData.getEffectType());
                }
            }
        }
    }

    /**
     * バフ効果の上限レベルを設定します。 level <= 0 の場合は上限なし。
     *
     * @param level 上限レベル
     */
    public void setMaxBuffLevel(int level) {
        this.maxBuffLevel = level;
    }

    /**
     * 現在のバフ効果の上限レベルを返します。
     *
     * @return 上限レベル（上限なしの場合は -1）
     */
    public int getMaxBuffLevel() {
        return maxBuffLevel;
    }
}
