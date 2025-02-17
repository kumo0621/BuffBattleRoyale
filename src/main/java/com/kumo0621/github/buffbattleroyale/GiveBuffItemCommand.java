package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

/**
 * コマンド "/buffitem" 実行時に、指定されたバフアイテム（引数がない場合はデフォルト）を与えます。
 * タブ補完機能により、登録済みバフアイテムのIDが候補として表示されます。
 */
public class GiveBuffItemCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // コマンド実行者がプレイヤーであるか確認
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用可能です。");
            return true;
        }
        Player player = (Player) sender;
        BuffItemData buffData = null;
        if (args.length == 0) {
            buffData = BuffRegistry.getDefaultBuffItem();
            if (buffData == null) {
                player.sendMessage(ChatColor.RED + "デフォルトのバフアイテムが登録されていません。");
                return true;
            }
        } else {
            String id = args[0];
            buffData = BuffRegistry.getBuffItemById(id);
            if (buffData == null) {
                player.sendMessage(ChatColor.RED + "指定されたIDのバフアイテムが見つかりません: " + id);
                return true;
            }
        }
        ItemStack buffItem = buffData.createItemStack();
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(buffItem);
            player.sendMessage(ChatColor.GREEN + "バフアイテム [" + buffData.getId() + "] をインベントリに追加しました。");
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), buffItem);
            player.sendMessage(ChatColor.YELLOW + "インベントリが満杯です。地面にバフアイテム [" + buffData.getId() + "] をドロップしました。");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (BuffItemData data : BuffRegistry.getRegisteredBuffItems()) {
                if (data.getId().toLowerCase().startsWith(prefix)) {
                    suggestions.add(data.getId());
                }
            }
        }
        return suggestions;
    }
}
