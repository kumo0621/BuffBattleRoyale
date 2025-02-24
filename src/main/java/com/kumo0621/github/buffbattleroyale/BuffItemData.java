package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

/**
 * バフアイテムのデータを保持するクラスです。
 * 各項目として、ID、素材、custom model data、付与するPotionEffectType、
 * 表示名、バフレベル、特殊効果、発生確率、さらに詳細情報 (details) を管理します。
 */
public class BuffItemData {
    private final String id;
    private final Material material;
    private final int customModelData;
    private final PotionEffectType effectType; // 通常バフの場合のポーション効果。特殊効果の場合は null。
    private final String displayName;
    private final int buffLevel;  // 例：1なら実際の効果は amplifier 0、2なら 1 となる。
    private final SpecialEffect specialEffect; // 特殊効果。通常のバフは null。
    private final double chance; // 例：0.10 は10%の確率
    private final String details; // 詳細情報

    /**
     * フルコンストラクタ
     */
    public BuffItemData(String id, Material material, int customModelData, PotionEffectType effectType,
                        String displayName, String details, int buffLevel, SpecialEffect specialEffect, double chance) {
        this.id = id;
        this.material = material;
        this.customModelData = customModelData;
        this.effectType = effectType;
        this.displayName = displayName;
        this.details = details;
        this.buffLevel = buffLevel;
        this.specialEffect = specialEffect;
        this.chance = chance;
    }

    /**
     * 特殊効果不要の場合のコンストラクタ
     */
    public BuffItemData(String id, Material material, int customModelData, PotionEffectType effectType,
                        String displayName, String details, int buffLevel, double chance) {
        this(id, material, customModelData, effectType, displayName, details, buffLevel, null, chance);
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBuffLevel() {
        return buffLevel;
    }

    public SpecialEffect getSpecialEffect() {
        return specialEffect;
    }

    public double getChance() {
        return chance;
    }

    public String getDetails() {
        return details;
    }

    /**
     * 指定された ItemStack がこのバフアイテムと一致するか判定します。
     */
    public boolean matches(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != material) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        if (meta.getCustomModelData() != customModelData) return false;
        if (displayName != null && !displayName.isEmpty()) {
            if (!meta.hasDisplayName()) return false;
            if (!meta.getDisplayName().equals(displayName)) return false;
        }
        return true;
    }

    /**
     * このバフアイテムの ItemStack を生成して返します。
     */
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(customModelData);
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }
        // details があれば、改行区切りで lore に設定
        if (details != null && !details.isEmpty()) {
            // details を "\n" で分割し、リストに変換
            meta.setLore(java.util.Arrays.asList(details.split("\n")));
        }
        item.setItemMeta(meta);
        return item;
    }

}
