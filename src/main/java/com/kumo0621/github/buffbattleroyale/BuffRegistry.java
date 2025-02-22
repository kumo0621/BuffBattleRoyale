package com.kumo0621.github.buffbattleroyale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

/**
 * バフアイテムの登録・管理を行うクラスです。
 * 新たなバフアイテムを簡単に追加できるよう、ここで一元管理します。
 */
public class BuffRegistry {

    private static final List<BuffItemData> buffItems = new ArrayList<>();

    /**
     * バフアイテムを登録します。
     *
     * @param buffItemData 登録するバフアイテムのデータ
     */
    public static void registerBuffItem(BuffItemData buffItemData) {
        buffItems.add(buffItemData);
    }

    /**
     * 登録されているバフアイテムのリストを返します。
     *
     * @return 登録済みバフアイテムのリスト
     */
    public static List<BuffItemData> getRegisteredBuffItems() {
        return Collections.unmodifiableList(buffItems);
    }

    /**
     * コマンドなどで使用するデフォルトのバフアイテムを返します。
     * 登録されている最初のバフアイテムを返すようにしています。
     *
     * @return デフォルトのバフアイテム、未登録の場合は null
     */
    public static BuffItemData getDefaultBuffItem() {
        if (buffItems.isEmpty()) return null;
        return buffItems.get(0);
    }

    /**
     * 指定されたIDに対応するバフアイテムを返します。
     *
     * @param id バフアイテムのID
     * @return 該当するバフアイテム、見つからない場合は null
     */
    public static BuffItemData getBuffItemById(String id) {
        for (BuffItemData data : buffItems) {
            if (data.getId().equalsIgnoreCase(id)) {
                return data;
            }
        }
        return null;
    }

    // 静的初期化子：ここでバフアイテムを登録
    static {
        // Speed Buff
        registerBuffItem(new BuffItemData("speed", Material.PAPER, 1, PotionEffectType.SPEED, "Speed Buff", 1,0.10));
        registerBuffItem(new BuffItemData("speed2", Material.PAPER, 2, PotionEffectType.SPEED, "Speed Buff (Level 2)", 2,0.10));

        // 攻撃力バフ（Attack：INCREASE_DAMAGE）
        registerBuffItem(new BuffItemData("attack", Material.PAPER, 3, PotionEffectType.INSTANT_DAMAGE, "Attack Buff", 1,0.10));
        registerBuffItem(new BuffItemData("attack2", Material.PAPER, 4, PotionEffectType.INSTANT_DAMAGE, "Attack Buff (Level 2)", 2,0.10));

        // 再生バフ（Resistance：DAMAGE_RESISTANCE）
        registerBuffItem(new BuffItemData("heel", Material.PAPER, 5, PotionEffectType.REGENERATION, "Resistance Buff", 1,0.10));
        registerBuffItem(new BuffItemData("heel2", Material.PAPER, 6, PotionEffectType.REGENERATION, "Resistance Buff (Level 2)", 2,0.10));

        // ジャンプバフ（Jump：JUMP）
        registerBuffItem(new BuffItemData("jump", Material.PAPER, 7, PotionEffectType.JUMP_BOOST, "Jump Buff", 1,0.10));
        registerBuffItem(new BuffItemData("jump2", Material.PAPER, 8, PotionEffectType.JUMP_BOOST, "Jump Buff (Level 2)", 2,0.10));

        // 浮遊バフ（Levitation：LEVITATION）
        registerBuffItem(new BuffItemData("levitation", Material.PAPER, 9, PotionEffectType.LEVITATION, "Levitation Buff", 1,0.10));
        registerBuffItem(new BuffItemData("levitation2", Material.PAPER, 10, PotionEffectType.LEVITATION, "Levitation Buff (Level 2)", 2,0.10));

        // Lethal Hit Buff（特殊効果のみ：殴ったら1%の確率で相手を殺す）
        // ポーション効果は付与しないため、effectType は null、buffLevel は 0 としています。
        registerBuffItem(new BuffItemData("lethal", Material.PAPER, 11, null, "Lethal Hit Buff", 0, new LethalHitEffect(0.01),0.10));
        //テレポート
        registerBuffItem(new BuffItemData("teleport", Material.PAPER, 12, null, "Teleport Buff", 1, null,0.10));

        //耐性
        registerBuffItem(new BuffItemData("resistance", Material.PAPER, 13, PotionEffectType.RESISTANCE, "Resistance Buff", 1,0.10));
        registerBuffItem(new BuffItemData("resistance2", Material.PAPER, 14, PotionEffectType.RESISTANCE, "Resistance Buff (Level 2)", 2,0.10));
        registerBuffItem(new BuffItemData("shiftcreeper", Material.PAPER, 15, null, "Shift Creeper Summon Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftzombie", Material.PAPER, 16, null, "Shift Zombie Summon Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftskeleton", Material.PAPER, 17, null, "Shift Skeleton Summon Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftbabyzombie", Material.PAPER, 18, null, "Shift Baby Zombie Summon Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftfirecharge", Material.PAPER, 19, null, "Shift Fire Charge Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftbow", Material.PAPER, 20, null, "Shift Bow Buff", 1,0.10));
        registerBuffItem(new BuffItemData("shiftinvisible", Material.PAPER, 21, null, "Shift Invisible Buff", 1,0.10));
    }
}
