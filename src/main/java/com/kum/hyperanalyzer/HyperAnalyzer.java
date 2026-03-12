package com.kum.hyperanalyzer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.kum.hyperanalyzer.tracking.ServerTracker;
import com.kum.hyperanalyzer.commands.SessionStatsCommand;
import com.kum.hyperanalyzer.commands.CopyCommand;
import net.minecraftforge.client.ClientCommandHandler;

@Mod(modid = HyperAnalyzer.MODID, version = HyperAnalyzer.VERSION, acceptedMinecraftVersions = "[1.8.9]")
public class HyperAnalyzer {
    public static final String MODID = "hyperanalyzer";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static HyperAnalyzer instance;

    public ServerTracker serverTracker;
    public com.kum.hyperanalyzer.tracking.ScoreboardManager scoreboardManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        com.kum.hyperanalyzer.data.ModConfig.init();
        com.kum.hyperanalyzer.data.SessionManager.init();
        com.kum.hyperanalyzer.gui.KeybindHandler.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        this.serverTracker = new ServerTracker();
        this.scoreboardManager = new com.kum.hyperanalyzer.tracking.ScoreboardManager();
        MinecraftForge.EVENT_BUS.register(this.serverTracker);
        MinecraftForge.EVENT_BUS.register(this.scoreboardManager);
        MinecraftForge.EVENT_BUS.register(new com.kum.hyperanalyzer.tracking.ZombiesDetector());
        MinecraftForge.EVENT_BUS.register(new com.kum.hyperanalyzer.tracking.PowerupTracker());
        MinecraftForge.EVENT_BUS.register(new com.kum.hyperanalyzer.gui.KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new com.kum.hyperanalyzer.gui.GiveInfoHud());
        MinecraftForge.EVENT_BUS.register(this);

        // Register client-side commands
        ClientCommandHandler.instance.registerCommand(new SessionStatsCommand());
        ClientCommandHandler.instance.registerCommand(new CopyCommand());
    }
}
