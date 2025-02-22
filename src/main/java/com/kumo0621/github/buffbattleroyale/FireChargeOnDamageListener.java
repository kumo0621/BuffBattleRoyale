package com.kumo0621.github.buffbattleroyale;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class FireChargeOnDamageListener implements Listener {

    // 発射する全方位の角度ステップ（例：30度間隔＝12方向）
    private static final double ANGLE_STEP_DEG = 30.0;
    // ダメージを受けたときに発射する volley 数（固定値、ここでは例として 3 を設定）
    private static final int DEFAULT_VOLLEYS = 3;

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        // ダメージを受けたとき、常に DEFAULT_VOLLEYS 分のファイヤーチャージを全方位に発射する
        spawnFireChargeVolleys(player, DEFAULT_VOLLEYS);
        player.sendMessage(ChatColor.GOLD + "Fire Charge Buff: " + DEFAULT_VOLLEYS + " volley(s) fired in all directions due to damage!");
    }

    private void spawnFireChargeVolleys(Player player, int volleys) {
        Location loc = player.getEyeLocation();
        for (int i = 0; i < volleys; i++) {
            for (double angle = 0; angle < 360; angle += ANGLE_STEP_DEG) {
                double rad = Math.toRadians(angle);
                Vector direction = new Vector(Math.cos(rad), 0, Math.sin(rad));
                // わずかに上向きにする（y成分）
                direction.setY(0.1);
                direction.normalize();
                SmallFireball fireball = (SmallFireball) player.getWorld().spawnEntity(loc, EntityType.SMALL_FIREBALL);
                fireball.setDirection(direction);
                // 召喚者の情報をメタデータとして設定（後でターゲット制御などに利用可能）
                fireball.setMetadata("projectileBuffOwner", new FixedMetadataValue(BuffBattleRoyale.getInstance(), player.getUniqueId().toString()));
            }
        }
    }
}
