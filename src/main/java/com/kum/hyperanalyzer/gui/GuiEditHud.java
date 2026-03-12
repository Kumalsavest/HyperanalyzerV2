package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic drag-and-drop HUD editor.
 * Caller supplies a list of Panel descriptors; user drags them around and clicks Save.
 */
public class GuiEditHud extends GuiScreen {

    public enum Mode { SESSIONS, SESSION_DETAILS, GIVE_INFO }

    private final GuiScreen parent;
    private final Mode mode;

    // A draggable panel
    private static class Panel {
        String label;      // display name
        String[] preview;  // lines shown as preview
        int x, y;          // current top-left in GUI pixels
        int w, h;          // bounding box
        boolean dragging;
        int dragOffX, dragOffY;

        Panel(String label, String[] preview, int x, int y) {
            this.label   = label;
            this.preview = preview;
            this.x = x; this.y = y;
            this.w = 160; this.h = 10 + preview.length * 10;
        }
    }

    private final List<Panel> panels = new ArrayList<>();

    public GuiEditHud(GuiScreen parent, Mode mode) {
        this.parent = parent;
        this.mode   = mode;
    }

    @Override
    public void initGui() {
        super.initGui();
        panels.clear();
        buildPanels();
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 105, height - 28, 100, 20, "\u00a7aSave"));
        buttonList.add(new GuiButton(1, width / 2 + 5,   height - 28, 100, 20, "\u00a7cCancel"));
    }

    private void buildPanels() {
        switch (mode) {
            case SESSIONS:
                panels.add(make("Overall Fastest Times",
                    new String[]{"Bad Blood: 12m 34s","Dead End: 11m 00s","Prison: 14m 20s"},
                    ModConfig.instance.posOverallFastest));
                panels.add(make("Est. Daily Averages",
                    new String[]{"Double Gold/game: 2.1","\u00a79R2 Max Ammo\u00a7r: 48%","\u00a7cR2 Insta Kill\u00a7r: 55%","Total Sessions: 14"},
                    ModConfig.instance.posDailyAverages));
                panels.add(make("Chest Stats",
                    new String[]{"Mansion: 4 (40%)", "Library: 3 (30%)", "Dungeon: 3 (30%)"},
                    ModConfig.instance.posChestStats));
                panels.add(make("Weapon Stats",
                    new String[]{"Zombie Zapper: 8 (22%)", "The Puncher: 6 (17%)"},
                    ModConfig.instance.posWeaponStats));
                break;
            case SESSION_DETAILS:
                panels.add(make("Fastest Time",
                    new String[]{"Bad Blood: 12m 34s","Dead End: 11m 00s","Prison: 14m 20s"},
                    ModConfig.instance.posSessionFastest));
                panels.add(make("Avg Time",
                    new String[]{"Bad Blood: 13m 00s","Dead End: 12m 10s","Prison: 15m 00s"},
                    ModConfig.instance.posSessionAvgTime));
                panels.add(make("R2 Stats",
                    new String[]{"\u00a79R2 Max Ammo\u00a7r: 48%","\u00a7cR2 Insta Kill\u00a7r: 55%"},
                    ModConfig.instance.posR2Stats));
                panels.add(make("Session Chests",
                    new String[]{"Mansion: 2 (40%)", "Library: 1 (20%)"},
                    ModConfig.instance.posSessionChests));
                panels.add(make("Session Weapons",
                    new String[]{"Zombie Zapper: 4 (33%)", "The Puncher: 3 (25%)"},
                    ModConfig.instance.posSessionWeapons));
                break;
            case GIVE_INFO:
                panels.add(make("Key Part",
                    new String[]{"Key Part: \u00a7aYard\u00a7r"},
                    ModConfig.instance.posGiveInfoKeyPart));
                panels.add(make("Chest Location",
                    new String[]{"Chest Location: \u00a7eLibrary\u00a7r"},
                    ModConfig.instance.posGiveInfoChest));
                break;
        }
    }

    /** Build a panel from fractional config position */
    private Panel make(String label, String[] preview, float[] frac) {
        int px = Math.round(frac[0] * width);
        int py = Math.round(frac[1] * height);
        return new Panel(label, preview, px, py);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // Save
            savePanels();
            ModConfig.save();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    private void savePanels() {
        switch (mode) {
            case SESSIONS:
                setFrac(ModConfig.instance.posOverallFastest, panels.get(0));
                setFrac(ModConfig.instance.posDailyAverages,  panels.get(1));
                setFrac(ModConfig.instance.posChestStats,     panels.get(2));
                setFrac(ModConfig.instance.posWeaponStats,    panels.get(3));
                break;
            case SESSION_DETAILS:
                setFrac(ModConfig.instance.posSessionFastest, panels.get(0));
                setFrac(ModConfig.instance.posSessionAvgTime, panels.get(1));
                setFrac(ModConfig.instance.posR2Stats,        panels.get(2));
                setFrac(ModConfig.instance.posSessionChests,  panels.get(3));
                setFrac(ModConfig.instance.posSessionWeapons, panels.get(4));
                break;
            case GIVE_INFO:
                setFrac(ModConfig.instance.posGiveInfoKeyPart, panels.get(0));
                setFrac(ModConfig.instance.posGiveInfoChest,   panels.get(1));
                break;
        }
    }

    private void setFrac(float[] arr, Panel p) {
        arr[0] = width  > 0 ? (float) p.x / width  : 0f;
        arr[1] = height > 0 ? (float) p.y / height : 0f;
        // Clamp 0..1
        arr[0] = Math.max(0f, Math.min(0.98f, arr[0]));
        arr[1] = Math.max(0f, Math.min(0.98f, arr[1]));
    }

    // ---- Mouse events ----

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            for (Panel p : panels) {
                if (mouseX >= p.x && mouseX <= p.x + p.w && mouseY >= p.y && mouseY <= p.y + p.h) {
                    p.dragging  = true;
                    p.dragOffX  = mouseX - p.x;
                    p.dragOffY  = mouseY - p.y;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel p : panels) p.dragging = false;
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (Panel p : panels) {
            if (p.dragging) {
                p.x = mouseX - p.dragOffX;
                p.y = mouseY - p.dragOffY;
                // Keep on screen
                p.x = Math.max(0, Math.min(width  - p.w, p.x));
                p.y = Math.max(0, Math.min(height - p.h, p.y));
            }
        }
    }

    // ---- Drawing ----

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj,
                EnumChatFormatting.YELLOW + "Edit HUD Layout" + EnumChatFormatting.RESET +
                " \u00a77(drag panels, then Save)\u00a7r",
                width / 2, 8, 0xFFFFFF);

        for (Panel p : panels) {
            // Background box
            int bg = p.dragging ? 0xCC334455 : 0xAA223344;
            drawRect(p.x, p.y, p.x + p.w, p.y + p.h, bg);
            // Border
            drawHorizontalLine(p.x, p.x + p.w, p.y,           0xFF88AACC);
            drawHorizontalLine(p.x, p.x + p.w, p.y + p.h,     0xFF88AACC);
            drawVerticalLine  (p.x,       p.y, p.y + p.h,      0xFF88AACC);
            drawVerticalLine  (p.x + p.w, p.y, p.y + p.h,      0xFF88AACC);
            // Label (bold)
            fontRendererObj.drawStringWithShadow("\u00a7l" + p.label + "\u00a7r", p.x + 3, p.y + 2, 0xFFDD00);
            // Preview lines
            for (int i = 0; i < p.preview.length; i++) {
                fontRendererObj.drawStringWithShadow(p.preview[i], p.x + 3, p.y + 12 + i * 10, 0xCCCCCC);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
