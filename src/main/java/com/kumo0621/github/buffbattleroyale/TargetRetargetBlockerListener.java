package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class TargetRetargetBlockerListener implements Listener {

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        if (event.getEntity() instanceof Creature) {
            Creature mob = (Creature) event.getEntity();
            // メタデータのキーは "targetCleared:" + playerUUID
            String key = "targetCleared:" + player.getUniqueId().toString();
            if (mob.hasMetadata(key)) {
                event.setCancelled(true);
            }
        }
    }
}
