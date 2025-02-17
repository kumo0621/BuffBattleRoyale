package com.kumo0621.github.buffbattleroyale;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

/**
 * バフアイテムのデータを保持するクラスです。
 * 各項目として、ID、素材、custom model data、付与するPotionEffectType、
 * 表示名、バフレベル（buffLevel）、および任意の特殊効果（specialEffect）を管理します。
 */
public class BuffItemData {
    private final String id;
    private final Material material;
    private final int customModelData;
    private final PotionEffectType effectType; // ポーション効果として付与する効果。特殊効果専用の場合は null にします。
    private final String displayName;
    private final int buffLevel;  // アイテムが付与するバフの強さ。たとえば、1なら実際の amplifier は 0、2 なら 1 となる。
    private final SpecialEffect specialEffect; // 任意の特殊効果。通常のバフは null。

    // フルコンストラクタ
    public BuffItemData(String id, Material material, int customModelData, PotionEffectType effectType, String displayName, int buffLevel, SpecialEffect specialEffect) {
        this.id = id;
        this.material = material;
        this.customModelData = customModelData;
        this.effectType = effectType;
        this.displayName = displayName;
        this.buffLevel = buffLevel;
        this.specialEffect = specialEffect;
    }

    // 特殊効果が不要な場合のコンストラクタ
    public BuffItemData(String id, Material material, int customModelData, PotionEffectType effectType, String displayName, int buffLevel) {
        this(id, material, customModelData, effectType, displayName, buffLevel, null);
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

    /**
     * 指定された ItemStack がこのバフアイテムと一致するか判定します。
     *
     * @param item 判定対象の ItemStack
     * @return 一致すれば true
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
     *
     * @return 生成された ItemStack
     */
    public ItemStack createItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(customModelData);
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(displayName);
        }
        item.setItemMeta(meta);
        return item;
    }
}
