package com.kumo0621.github.buffbattleroyale;

import org.bukkit.plugin.java.JavaPlugin;

public class BuffBattleRoyale extends JavaPlugin {
    private static BuffBattleRoyale instance;
    private BuffChestManager buffChestManager;
    @Override
    public void onEnable() {
        instance = this;
        // BuffManager の起動（ポーション効果の更新）
        BuffManager.getInstance().start(this);
        this.saveDefaultConfig();
        BuffChestManager buffChestManager = new BuffChestManager(this);
        BuffManager.getInstance().start(this);
        // コマンド "buffitem" の登録（タブ補完付き）
        GiveBuffItemCommand giveCommand = new GiveBuffItemCommand();
        this.getCommand("buffitem").setExecutor(giveCommand);
        this.getCommand("buffitem").setTabCompleter(giveCommand);

        // コマンド "buffstatus" の登録
        this.getCommand("buffstatus").setExecutor(new BuffStatusCommand());

        // 攻撃時の特殊効果判定用リスナーの登録
        getServer().getPluginManager().registerEvents(new SpecialBuffListener(), this);

        getServer().getPluginManager().registerEvents(new ShiftCreeperSummonListener(), this);
        // 新規：召喚されたクリーパーのターゲット制御リスナー
        getServer().getPluginManager().registerEvents(new CreeperTargetListener(), this);
        getServer().getScheduler().runTaskTimer(this, new BuffItemRemovalTask(), 0L, 20L);
        getServer().getPluginManager().registerEvents(new ShiftTeleportListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftZombieSummonListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftSkeletonSummonListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftChibiZombieSummonListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftBowBuffListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftInvisibleListener(), this);
        this.getCommand("fillchest").setExecutor(new FillChestCommand(buffChestManager));
        buffChestManager = new BuffChestManager(this);
        buffChestManager.start();
        getServer().getPluginManager().registerEvents(new ChestOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new FireChargeOnDamageListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftFillChestListener(), this);
        getServer().getPluginManager().registerEvents(new ShiftDashListener(), this);
        getServer().getPluginManager().registerEvents(new GlobalGlowListener(), this);

        getLogger().info("BuffBattleRoyale プラグインが有効になりました。");
    }

    @Override
    public void onDisable() {
        BuffManager.getInstance().stop();
        if (buffChestManager != null) {
            buffChestManager.stop();
        }
        getLogger().info("BuffBattleRoyale プラグインが無効になりました。");
    }
    public static BuffBattleRoyale getInstance() {
        return instance;
    }
}
