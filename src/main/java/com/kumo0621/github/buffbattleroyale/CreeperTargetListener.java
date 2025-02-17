package com.kumo0621.github.buffbattleroyale;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.MetadataValue;

public class CreeperTargetListener implements Listener {

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Creeper && entity.hasMetadata("shiftSummonOwner")) {
            for (MetadataValue value : entity.getMetadata("shiftSummonOwner")) {
                String ownerUUID = value.asString();
                Entity target = event.getTarget();
                if (target instanceof Player) {
                    Player targetPlayer = (Player) target;
                    if (targetPlayer.getUniqueId().toString().equals(ownerUUID)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
