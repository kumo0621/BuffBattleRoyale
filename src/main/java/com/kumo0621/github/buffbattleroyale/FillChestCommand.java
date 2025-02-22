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

import java.util.List;
import java.util.Random;

public class FillChestCommand implements CommandExecutor {

    private final BuffChestManager buffChestManager;
    private final Random random = new Random();

    public FillChestCommand(BuffChestManager buffChestManager) {
        this.buffChestManager = buffChestManager;
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
            // 空いているスロットをランダムに１つ選ぶ
            int[] emptySlots = getEmptySlots(inv);
            if (emptySlots.length == 0) {
                sender.sendMessage(ChatColor.YELLOW + "Chest at " + loc + " is full.");
                continue;
            }
            int slot = emptySlots[random.nextInt(emptySlots.length)];
            // BuffRegistry から全バフアイテムのリストを取得し、ランダムに１点選択
            List<BuffItemData> buffItems = BuffRegistry.getRegisteredBuffItems();
            if (buffItems.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "No buff items registered in BuffRegistry.");
                return true;
            }
            BuffItemData chosenBuff = buffItems.get(random.nextInt(buffItems.size()));
            ItemStack item = chosenBuff.createItemStack();
            inv.setItem(slot, item);
            filledCount++;
        }
        sender.sendMessage(ChatColor.GREEN + "Filled " + filledCount + " chests with one random buff item each.");
        return true;
    }

    // 空きスロットのインデックス一覧を返すユーティリティ
    private int[] getEmptySlots(Inventory inv) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
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
