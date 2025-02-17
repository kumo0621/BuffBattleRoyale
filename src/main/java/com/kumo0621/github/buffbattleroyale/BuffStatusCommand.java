package com.kumo0621.github.buffbattleroyale;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * コマンド "/buffstatus" を実行すると、
 * 対象スロット（9～17番）に配置されているバフアイテムから
 * 現在のバフ効果（通常効果と特殊効果：ここでは Lethal Hit の合算確率）を集計し、
 * プレイヤーにチャットで表示します。
 */
public class BuffStatusCommand implements CommandExecutor {

    // 対象とするスロット（9～17番）
    private static final int BUFF_SLOT_START = 9;
    private static final int BUFF_SLOT_END = 17;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return true;
        }

        Player player = (Player) sender;
        // ポーション効果用のバフを合算するマップ
        Map<PotionEffectType, Integer> potionBuffs = new HashMap<>();
        // 特殊効果（ここでは lethal hit）の確率を合算する変数
        double aggregatedLethalChance = 0.0;

        // 対象スロット内の各アイテムをチェック
        for (int slot = BUFF_SLOT_START; slot <= BUFF_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null) {
                for (BuffItemData buffData : BuffRegistry.getRegisteredBuffItems()) {
                    if (buffData.matches(item)) {
                        // 通常のポーション効果として付与する場合（effectType が null でない）
                        if (buffData.getEffectType() != null) {
                            int current = potionBuffs.getOrDefault(buffData.getEffectType(), 0);
                            potionBuffs.put(buffData.getEffectType(), current + buffData.getBuffLevel());
                        }
                        // 特殊効果（ここでは LethalHitEffect）の場合
                        if (buffData.getSpecialEffect() != null && buffData.getSpecialEffect() instanceof LethalHitEffect) {
                            LethalHitEffect lethal = (LethalHitEffect) buffData.getSpecialEffect();
                            aggregatedLethalChance += lethal.getChance();
                        }
                    }
                }
            }
        }

        // チャット表示
        player.sendMessage(ChatColor.AQUA + "==== 現在のバフ効果 ====");
        if (potionBuffs.isEmpty() && aggregatedLethalChance <= 0.0) {
            player.sendMessage(ChatColor.YELLOW + "バフ効果は見つかりませんでした。");
        } else {
            // 通常のバフ効果（ポーション効果）の表示
            for (Map.Entry<PotionEffectType, Integer> entry : potionBuffs.entrySet()) {
                PotionEffectType effectType = entry.getKey();
                int totalLevel = entry.getValue();
                // PotionEffect の amplifier は (合計バフレベル - 1) となるので
                String effectName = effectType.getName();
                player.sendMessage(ChatColor.GREEN + effectName + ": 合計レベル " + totalLevel + " (実際の効果レベル " + (totalLevel - 1) + ")");
            }
            // Lethal Hit 効果の表示（合算確率）
            if (aggregatedLethalChance > 0) {
                String chanceStr = String.format("%.2f", aggregatedLethalChance * 100);
                player.sendMessage(ChatColor.RED + "Lethal Hit Buff: " + chanceStr + "% の確率で即死");
            }
        }

        return true;
    }
}
