package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class TimeFlipShiftListener implements Listener {

    // 対象ホットバーのスロット範囲（0～8）
    private static final int HOTBAR_SLOT_START = 0;
    private static final int HOTBAR_SLOT_END = 8;
    // 使用するバフID（BuffRegistry に "timeflip" として登録されていること）
    private static final String TIMEFLIP_BUFF_ID = "timeflip";

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        // シフトが押されたとき（isSneaking()がtrue）のみ処理
        if (event.isSneaking()) {
            Player player = event.getPlayer();
            if (hasTimeFlipBuff(player)) {
                World world = player.getWorld();
                long time = world.getTime();
                if (time < 12000) {
                    // 昼→夜: 例えば、昼の時間帯なら 13000 に設定
                    world.setTime(13000);
                    player.sendMessage(ChatColor.GREEN + "TimeFlip Buff activated: Changed day to night.");
                } else {
                    // 夜→昼: 夜なら 1000 に設定
                    world.setTime(1000);
                    player.sendMessage(ChatColor.GREEN + "TimeFlip Buff activated: Changed night to day.");
                }
            }
        }
    }

    /**
     * プレイヤーのホットバー（スロット0～8）に "timeflip" バフが存在するかチェックする。
     */
    private boolean hasTimeFlipBuff(Player player) {
        BuffItemData buff = BuffRegistry.getBuffItemById(TIMEFLIP_BUFF_ID);
        if (buff == null) return false;
        for (int slot = HOTBAR_SLOT_START; slot <= HOTBAR_SLOT_END; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && buff.matches(item)) {
                return true;
            }
        }
        return false;
    }
}
