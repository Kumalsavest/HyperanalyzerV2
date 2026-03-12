package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import net.minecraft.client.Minecraft;

/**
 * Converts stored fractional positions [0..1] to actual screen pixels and back.
 * All panels use this so positions work at any resolution.
 */
public class HudLayout {

    public static int toPixelX(float frac) {
        return Math.round(frac * Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().gameSettings.guiScale / getGuiScaleFactor());
    }
    public static int toPixelY(float frac) {
        return Math.round(frac * Minecraft.getMinecraft().displayHeight / Minecraft.getMinecraft().gameSettings.guiScale / getGuiScaleFactor());
    }

    /** Convert pixel x back to [0..1] fraction given current screen width (in GUI units) */
    public static float toFracX(int px, int screenW) {
        return screenW > 0 ? (float) px / screenW : 0f;
    }
    public static float toFracY(int py, int screenH) {
        return screenH > 0 ? (float) py / screenH : 0f;
    }

    private static float getGuiScaleFactor() {
        int scale = Minecraft.getMinecraft().gameSettings.guiScale;
        if (scale == 0) scale = 2; // auto
        return 1f; // We use GuiScreen width/height which is already in GUI coords
    }

    // ---- Convenience accessors that return pixel coords given screen dimensions ----

    public static int[] overallFastest(int w, int h)  { float[] p = ModConfig.instance.posOverallFastest;  return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] dailyAverages(int w, int h)   { float[] p = ModConfig.instance.posDailyAverages;   return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] chestStats(int w, int h)      { float[] p = ModConfig.instance.posChestStats;      return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] weaponStats(int w, int h)     { float[] p = ModConfig.instance.posWeaponStats;     return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }

    public static int[] sessionFastest(int w, int h)  { float[] p = ModConfig.instance.posSessionFastest;  return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] sessionAvgTime(int w, int h)  { float[] p = ModConfig.instance.posSessionAvgTime;  return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] r2Stats(int w, int h)         { float[] p = ModConfig.instance.posR2Stats;         return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] sessionChests(int w, int h)   { float[] p = ModConfig.instance.posSessionChests;   return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] sessionWeapons(int w, int h)  { float[] p = ModConfig.instance.posSessionWeapons;  return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }

    public static int[] giveInfoKeyPart(int w, int h) { float[] p = ModConfig.instance.posGiveInfoKeyPart; return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
    public static int[] giveInfoChest(int w, int h)   { float[] p = ModConfig.instance.posGiveInfoChest;   return new int[]{Math.round(p[0]*w), Math.round(p[1]*h)}; }
}
