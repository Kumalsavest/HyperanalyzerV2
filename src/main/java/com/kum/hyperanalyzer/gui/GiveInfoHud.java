package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.data.ZombiesGame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.kum.hyperanalyzer.tracking.ZombiesDetector;

public class GiveInfoHud {
    public static boolean isEnabled() { return ModConfig.instance.giveInfoEnabled; }
    public static void setEnabled(boolean v) { ModConfig.instance.giveInfoEnabled = v; ModConfig.save(); }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!isEnabled()) return;
        if (!ZombiesDetector.isInZombies || ZombiesDetector.currentGame == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        ScaledResolution sr = new ScaledResolution(mc);
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();

        ZombiesGame game = ZombiesDetector.currentGame;
        String map = game.map;

        // Key Part (Prison only)
        if ("Prison".equals(map)) {
            String keyPart = game.keyPart != null ? game.keyPart : "Unidentified";
            String color;
            switch (keyPart) {
                case "Yard":   color = "\u00a7a"; break;
                case "Courts": color = "\u00a7b"; break;
                default:       color = "\u00a77"; break;
            }
            int[] pos = kpPos(sw, sh);
            fr.drawStringWithShadow("Key Part: " + color + keyPart + "\u00a7r", pos[0], pos[1], 0xFFFFFF);
        }

        // Chest Location (not for Alien Arcadium / The Lab)
        if (!"Alien Arcadium".equals(map) && !"The Lab".equals(map)) {
            String chestLoc  = game.currentChestLocation;
            String display   = chestLoc != null ? chestLoc : "Searching...";
            String clrChest  = chestLoc != null ? "\u00a7e" : "\u00a77";
            int[] pos = chestPos(sw, sh);
            fr.drawStringWithShadow("Chest Location: " + clrChest + display + "\u00a7r", pos[0], pos[1], 0xFFFFFF);
        }
    }

    private static int[] kpPos(int w, int h) {
        float[] f = ModConfig.instance.posGiveInfoKeyPart;
        return new int[]{Math.round(f[0]*w), Math.round(f[1]*h)};
    }
    private static int[] chestPos(int w, int h) {
        float[] f = ModConfig.instance.posGiveInfoChest;
        return new int[]{Math.round(f[0]*w), Math.round(f[1]*h)};
    }
}
