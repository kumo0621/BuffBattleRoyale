package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FillChestCommandCombined implements CommandExecutor {

    private final BuffChestManager buffChestManager;
    private final NormalChestManager normalChestManager;
    private final Random random = new Random();

    public FillChestCommandCombined(BuffChestManager buffChestManager, NormalChestManager normalChestManager) {
        this.buffChestManager = buffChestManager;
        this.normalChestManager = normalChestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Location> chestLocations = buffChestManager.getChestLocations();
        if (chestLocations.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No chest locations configured in config.yml!");
            return true;
        }
        int filledCount = 0;
        for (Location loc : chestLocations) {
            BlockState state = loc.getBlock().getState();
            if (!(state instanceof Chest)) {
                sender.sendMessage(ChatColor.YELLOW + "No chest found at " + loc);
                continue;
            }
            Chest chest = (Chest) state;
            Inventory inv = chest.getInventory();
            // 空いているスロットを取得
            int[] emptySlots = getEmptySlots(inv);
            if (emptySlots.length < 3) {
                sender.sendMessage(ChatColor.YELLOW + "Chest at " + loc + " doesn't have at least 3 empty slots.");
                continue;
            }
            // ランダムに 3 つの空きスロットを選ぶ
            List<Integer> slotList = new ArrayList<>();
            for (int slot : emptySlots) {
                slotList.add(slot);
            }
            Collections.shuffle(slotList, random);
            int slotBuff = slotList.get(0);    // バフアイテム用
            int slotNormal1 = slotList.get(1);   // ノーマルアイテム用
            int slotNormal2 = slotList.get(2);   // ノーマルアイテム用

            // BuffRegistry からランダムなバフアイテムを１点選ぶ
            List<BuffItemData> buffItems = BuffRegistry.getRegisteredBuffItems();
            if (buffItems.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "No buff items registered in BuffRegistry.");
                return true;
            }
            BuffItemData chosenBuff = buffItems.get(random.nextInt(buffItems.size()));
            ItemStack buffStack = chosenBuff.createItemStack();

            // NormalChestManager からランダムなノーマルアイテムを２点選ぶ
            NormalChestItem normalItem1 = normalChestManager.getRandomItem();
            NormalChestItem normalItem2 = normalChestManager.getRandomItem();
            if (normalItem1 == null || normalItem2 == null) {
                sender.sendMessage(ChatColor.RED + "No normal chest items available.");
                return true;
            }
            ItemStack normalStack1 = normalItem1.getItem();
            ItemStack normalStack2 = normalItem2.getItem();

            inv.setItem(slotBuff, buffStack);
            inv.setItem(slotNormal1, normalStack1);
            inv.setItem(slotNormal2, normalStack2);
            filledCount++;
        }
        sender.sendMessage(ChatColor.GREEN + "Filled " + filledCount + " chests with 1 buff item and 2 normal items each.");
        return true;
    }

    // 空いているスロットのインデックス一覧を返すユーティリティメソッド
    private int[] getEmptySlots(Inventory inv) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                list.add(i);
            }
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
