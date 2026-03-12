package com.kum.hyperanalyzer.tracking;

import com.kum.hyperanalyzer.data.SessionManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ServerTracker {
    public static boolean isOnHypixel = false;
    /** True whenever the scoreboard title is "ZOMBIES" — works on any server/proxy. */
    public static boolean isOnZombiesServer = false;
    private long lastTickTime = 0;
    private int tickCounter = 0;
    private boolean checkedFirstLogin = false;

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        checkedFirstLogin = false;
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (isOnHypixel || isOnZombiesServer) {
            updatePlaytime();
            if (ZombiesDetector.isInZombies && ZombiesDetector.currentGame != null) {
                ZombiesDetector.onDisconnect();
            }
        }
        isOnHypixel = false;
        isOnZombiesServer = false;
        checkedFirstLogin = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        SessionManager.checkDayChange();

        // Tick-based Hypixel detection — works even on first login
        if (!checkedFirstLogin) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null && mc.getCurrentServerData() != null) {
                String ip = mc.getCurrentServerData().serverIP.toLowerCase();
                if (ip.contains("hypixel.net")) {
                    if (!isOnHypixel) {
                        isOnHypixel = true;
                        lastTickTime = System.currentTimeMillis();
                        ZombiesDetector.onReconnect();
                    }
                } else {
                    isOnHypixel = false;
                }
                checkedFirstLogin = true;
            }
        }

        // Scoreboard-based zombies detection — works on any proxy/server
        // Check every 10 ticks (~0.5s) to avoid overhead
        tickCounter++;
        if (tickCounter >= 10) {
            tickCounter = 0;
            boolean wasOnZombies = isOnZombiesServer;
            isOnZombiesServer = "ZOMBIES".equals(ScoreboardManager.title);
            if (isOnZombiesServer && !wasOnZombies && !isOnHypixel) {
                // Just connected to a zombies server via non-Hypixel proxy
                lastTickTime = System.currentTimeMillis();
                ZombiesDetector.onReconnect();
            }
        }

        if (isOnHypixel || isOnZombiesServer) {
            if (tickCounter % 20 == 0) {
                updatePlaytime();
            }
        }
    }

    private void updatePlaytime() {
        long now = System.currentTimeMillis();
        long diff = now - lastTickTime;
        if (diff > 0 && diff < 10000) {
            if (SessionManager.currentSession != null) {
                SessionManager.currentSession.totalHypixelPlaytime += diff;
                if (ZombiesDetector.isInZombies) {
                    SessionManager.currentSession.totalZombiesPlaytime += diff;
                }
            }
        }
        lastTickTime = now;

        if (System.currentTimeMillis() % 30000 < 1000) {
            SessionManager.saveCurrentSession();
        }
    }
}
