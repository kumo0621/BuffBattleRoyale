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
        registerBuffItem(new BuffItemData("speed", Material.PAPER, 1, PotionEffectType.SPEED, "移動速度上昇", "",1,0.10));
        registerBuffItem(new BuffItemData("speed2", Material.PAPER, 2, PotionEffectType.SPEED, "移動速度上昇 (Level 2)","", 2,0.05));

        // 攻撃力バフ（Attack：INCREASE_DAMAGE）
        registerBuffItem(new BuffItemData("attack", Material.PAPER, 3, PotionEffectType.STRENGTH, "攻撃力上昇","", 1,0.10));
        registerBuffItem(new BuffItemData("attack2", Material.PAPER, 4, PotionEffectType.STRENGTH, "攻撃力上昇 (Level 2)","", 2,0.005));
        registerBuffItem(new BuffItemData("attack3", Material.PAPER, 29, PotionEffectType.STRENGTH, "攻撃力上昇 (Level 3)","", 3,0.002));
        // 再生バフ（Resistance：DAMAGE_RESISTANCE）
        registerBuffItem(new BuffItemData("heel", Material.PAPER, 5, PotionEffectType.REGENERATION, "再生","", 1,0.10));
        registerBuffItem(new BuffItemData("heel2", Material.PAPER, 6, PotionEffectType.REGENERATION, "再生 (Level 2)","", 2,0.005));
        registerBuffItem(new BuffItemData("heel3", Material.PAPER, 28, PotionEffectType.REGENERATION, "再生 (Level 3)","", 3,0.002));
        // ジャンプバフ（Jump：JUMP）
        registerBuffItem(new BuffItemData("jump", Material.PAPER, 7, PotionEffectType.JUMP_BOOST, "ジャンプ力上昇","", 1,0.10));
        registerBuffItem(new BuffItemData("jump2", Material.PAPER, 8, PotionEffectType.JUMP_BOOST, "ジャンプ力上昇 (Level 2)","", 2,0.08));

        // 浮遊バフ（Levitation：LEVITATION）
        registerBuffItem(new BuffItemData("levitation", Material.PAPER, 9, PotionEffectType.LEVITATION, "浮遊","", 1,0.10));
        registerBuffItem(new BuffItemData("levitation2", Material.PAPER, 10, PotionEffectType.LEVITATION, "浮遊(Level 2)","", 2,0.005));
        registerBuffItem(new BuffItemData("levitation3", Material.PAPER, 30, PotionEffectType.LEVITATION, "浮遊(Level 3)","", 3,0.003));
        // Lethal Hit Buff（特殊効果のみ：殴ったら1%の確率で相手を殺す）
        // ポーション効果は付与しないため、effectType は null、buffLevel は 0 としています。
        registerBuffItem(new BuffItemData("lethal", Material.PAPER, 11, null, "即死パンチ","1%で殴った相手を即死にする", 0, new LethalHitEffect(0.01),0.05));
        //テレポート
        registerBuffItem(new BuffItemData("teleport", Material.PAPER, 12, null, "テレポート","シフトを15秒押すとランダムなプレイヤーにテレポートする", 1, null,0.05));

        //耐性
        registerBuffItem(new BuffItemData("resistance", Material.PAPER, 13, PotionEffectType.RESISTANCE, "耐性","", 1,0.10));
        registerBuffItem(new BuffItemData("resistance2", Material.PAPER, 14, PotionEffectType.RESISTANCE, "耐性 (Level 2)","", 2,0.005));
        registerBuffItem(new BuffItemData("resistance3", Material.PAPER, 35, PotionEffectType.RESISTANCE, "耐性 (Level 3)","", 3,0.003));

        registerBuffItem(new BuffItemData("shiftcreeper", Material.PAPER, 15, null, "クリーパー召喚","5秒シフトを押すと召喚する", 1,0.03));
        registerBuffItem(new BuffItemData("shiftzombie", Material.PAPER, 16, null, "ゾンビ召喚","5秒シフトを押すとゾンビを召喚する", 1,0.03));
        registerBuffItem(new BuffItemData("shiftskeleton", Material.PAPER, 17, null, "スケルトン召喚","5秒シフトを押すとスケルトンを召喚する", 1,0.03));
        registerBuffItem(new BuffItemData("shiftbabyzombie", Material.PAPER, 18, null, "ベビーゾンビ召喚","5秒シフトを押すとベビーゾンビを召喚する", 1,0.03));
        registerBuffItem(new BuffItemData("firecharge", Material.PAPER, 19, null, "ファイヤーチャージ発射","ダメージを受けるとファイヤーチャージを全方向に放つ", 1,0.04));
        registerBuffItem(new BuffItemData("shiftbow", Material.PAPER, 20, null, "矢召喚","シフトをちょっと長く押すと矢が出る。", 1,0.08));
        registerBuffItem(new BuffItemData("shiftinvisible", Material.PAPER, 21, null, "透明化","シフトを押している間透明化する",1,0.05));
        registerBuffItem(new BuffItemData("shiftfillchest", Material.PAPER, 22, null,"アイテム出現","1分間シフトを押すとアイテムが手に入る", 1,0.02));
        registerBuffItem(new BuffItemData("shiftdash", Material.PAPER, 23, null, "壁抜け","20マス前に移動する。（壁にも埋まる）", 1,0.03));
        registerBuffItem(new BuffItemData("shiftglobalglow", Material.PAPER, 38, null, "全員強制発光","全員を発光させる", 1,0.08));
        // 新規：発行拒否バフ（custom model data 19 例示）

        registerBuffItem(new BuffItemData("toxic", Material.PAPER, 24, null, "毒状態","殴った相手に1%で毒を付与", 1,0.07));
        registerBuffItem(new BuffItemData("witherbuff", Material.PAPER, 25, null, "ウィザー付与","殴った相手に5%でウィザー状態を付与する", 1,0.04));
        registerBuffItem(new BuffItemData("targetclear", Material.PAPER, 26, null, "敵対解除", "持ってシフトを2秒押すと周囲3マスのモブが襲わなくなる",1,0.08));
        registerBuffItem(new BuffItemData("effectdoubler", Material.PAPER, 27, null, "倍効果","自身についてるエフェクトを倍にする", 1,0.05));
        // BuffRegistry.java（静的初期化子内）
        registerBuffItem(new BuffItemData("skeletonbat", Material.PAPER, 31, null, "コウモリ召喚", "5秒シフトを押してるとコウモリに乗ったスケルトンが召喚される", 1, 0.07));
        registerBuffItem(new BuffItemData("antidote", Material.PAPER, 32, null, "毒効果を解除", "シフトで毒エフェクトを解除します", 1, 0.07));
        registerBuffItem(new BuffItemData("witherantidote", Material.PAPER, 33, null, "ウィザー状態を解除", "シフトでウィザー状態を解除します", 1, 0.07));
        registerBuffItem(new BuffItemData("liferegen", Material.PAPER, 34, null, "ゾンビヒール", "ゾンビから攻撃を受けると回復します", 1, 0.06));
        registerBuffItem(new BuffItemData("saturation", Material.PAPER, 39, PotionEffectType.SATURATION, "満腹度上昇","", 1,0.03));
        registerBuffItem(new BuffItemData("saturation2", Material.PAPER, 40, PotionEffectType.SATURATION, "満腹度上昇(Level 2)","", 2,0.01));
        registerBuffItem(new BuffItemData("timeflip", Material.PAPER, 41, null, "昼夜逆転", "昼なら夜に、夜なら昼に変更します", 1, 0.08));
    }
}
