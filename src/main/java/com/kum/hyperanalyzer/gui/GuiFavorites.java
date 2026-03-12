package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.data.SessionData;
import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.ZombiesGame;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists all favorited games with open / rename / remove actions.
 */
public class GuiFavorites extends GuiScreen {
    private final GuiScreen parent;

    private final List<FavEntry> entries = new ArrayList<>();
    private int scrollY    = 0;
    private int maxScroll  = 0;

    // Rename state: which entry is being renamed, and its text field
    private int    renameIdx   = -1;
    private GuiTextField renameField = null;

    // Button ID ranges
    // 0..99   → open game details for entry i
    // 100..199 → toggle rename for entry i
    // 200..299 → remove favorite for entry i
    // 9000    → back

    public GuiFavorites(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        loadEntries();
        buildButtons();
    }

    private void loadEntries() {
        entries.clear();
        if (ModConfig.instance.favoriteGames == null) return;
        for (String key : ModConfig.instance.favoriteGames) {
            String[] parts = key.split(":");
            if (parts.length < 2) continue;
            String date = parts[0];
            int gameNum;
            try { gameNum = Integer.parseInt(parts[1]); } catch (NumberFormatException e) { continue; }
            SessionData sd = SessionManager.loadSession(date);
            if (sd == null || sd.games == null) continue;
            if (gameNum < 1 || gameNum > sd.games.size()) continue;
            ZombiesGame game = sd.games.get(gameNum - 1);
            String name = ModConfig.instance.favoriteNames != null
                    ? ModConfig.instance.favoriteNames.get(key) : null;
            entries.add(new FavEntry(key, date, gameNum, game, name));
        }
        java.util.Collections.reverse(entries); // most recently added first
    }

    private void buildButtons() {
        this.buttonList.clear();
        renameField = null;

        int startY = 40;
        for (int i = 0; i < entries.size(); i++) {
            FavEntry e = entries.get(i);
            boolean renaming = renameIdx == i;

            // Open game button (wide)
            this.buttonList.add(new GuiButton(i,
                    this.width / 2 - 155, startY + i * 26, 210, 20, buildLabel(e)));

            // Rename / Done button
            String renameLabel = renaming ? "\u00a7aDone\u00a7r" : "\u00a7eRename\u00a7r";
            this.buttonList.add(new GuiButton(100 + i,
                    this.width / 2 + 60, startY + i * 26, 55, 20, renameLabel));

            // Remove button
            this.buttonList.add(new GuiButton(200 + i,
                    this.width / 2 + 120, startY + i * 26, 40, 20, "\u00a7c\u2715\u00a7r"));

            // If this entry is being renamed, create a text field below it
            if (renaming) {
                renameField = new GuiTextField(0, this.fontRendererObj,
                        this.width / 2 - 155, startY + i * 26 + 22, 315, 18);
                renameField.setMaxStringLength(40);
                renameField.setFocused(true);
                String current = e.customName != null ? e.customName : "";
                renameField.setText(current);
                renameField.setCursorPositionEnd();
            }
        }

        maxScroll = Math.max(0, entries.size() * 26 + (renameIdx >= 0 ? 22 : 0) - (this.height - 80));
        this.buttonList.add(new GuiButton(9000, this.width / 2 - 100, this.height - 30, 200, 20, "Back"));
    }

    private String buildLabel(FavEntry e) {
        String name = (e.customName != null && !e.customName.isEmpty())
                ? "\u00a7e" + e.customName + "\u00a7r"
                : "Game " + e.gameNum + " \u00a77(" + e.date + ")\u00a7r";
        ZombiesGame g  = e.game;
        String diff    = g.difficulty != null ? g.difficulty : "Normal";
        String dc      = GuiGameSessions.getDifficultyColor(diff);
        String dc1     = diff.equals("Normal") ? "N" : diff.equals("Hard") ? "H" : "R";
        String dur     = GuiGameSessions.formatDuration(g.duration);
        String out     = g.isWin ? "\u00a7aW" : "\u00a7cL";
        int rnd        = g.lastRound > 0 ? g.lastRound : (g.rounds != null ? g.rounds.size() : 0);
        return String.format("\u00a7e\u2605\u00a7r %s %s %s%s\u00a7r R%d %s\u00a7r \u00a77%s\u00a7r",
                name, g.map, dc, dc1, rnd, out, dur);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollY += wheel > 0 ? -26 : 26;
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 9000) {
            // If renaming, commit first
            commitRename();
            this.mc.displayGuiScreen(parent);
        } else if (button.id >= 200 && button.id < 200 + entries.size()) {
            int idx = button.id - 200;
            if (renameIdx == idx) { renameIdx = -1; }
            FavEntry e = entries.get(idx);
            ModConfig.instance.favoriteGames.remove(e.key);
            if (ModConfig.instance.favoriteNames != null) ModConfig.instance.favoriteNames.remove(e.key);
            ModConfig.save();
            renameIdx = -1;
            loadEntries();
            buildButtons();
        } else if (button.id >= 100 && button.id < 100 + entries.size()) {
            int idx = button.id - 100;
            if (renameIdx == idx) {
                // "Done" — commit
                commitRename();
                renameIdx = -1;
            } else {
                commitRename(); // commit any previous rename
                renameIdx = idx;
            }
            buildButtons();
        } else if (button.id >= 0 && button.id < entries.size()) {
            commitRename();
            FavEntry e = entries.get(button.id);
            this.mc.displayGuiScreen(new GuiGameDetails(this, e.game, e.gameNum, e.date));
        }
    }

    private void commitRename() {
        if (renameIdx < 0 || renameField == null || renameIdx >= entries.size()) return;
        FavEntry e = entries.get(renameIdx);
        String name = renameField.getText().trim();
        if (ModConfig.instance.favoriteNames == null)
            ModConfig.instance.favoriteNames = new java.util.LinkedHashMap<>();
        if (name.isEmpty()) {
            ModConfig.instance.favoriteNames.remove(e.key);
            e.customName = null;
        } else {
            ModConfig.instance.favoriteNames.put(e.key, name);
            e.customName = name;
        }
        ModConfig.save();
        renameField = null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (renameField != null && renameField.isFocused()) {
            if (keyCode == 28 || keyCode == 156) { // Enter
                commitRename();
                renameIdx = -1;
                buildButtons();
                return;
            }
            if (keyCode == 1) { // Escape — cancel
                renameIdx = -1;
                renameField = null;
                buildButtons();
                return;
            }
            renameField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (renameField != null) renameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        if (renameField != null) renameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Scroll all entry buttons
        int startY = 40;
        for (GuiButton btn : this.buttonList) {
            if (btn.id < 300 && btn.id != 9000) {
                int row  = btn.id % 100;
                btn.yPosition = startY + row * 26 - scrollY
                        + (btn.id >= 200 ? 0 : (btn.id >= 100 ? 0 : 0));
                btn.visible = btn.yPosition >= startY - 10 && btn.yPosition <= this.height - 50;
            }
        }

        int favCount = ModConfig.instance.favoriteGames != null ? ModConfig.instance.favoriteGames.size() : 0;
        this.drawCenteredString(this.fontRendererObj,
                "\u00a7l\u00a7e\u2605 Favorites (" + favCount + ")\u00a7r", this.width / 2, 12, 0xFFFFFF);

        if (entries.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj,
                    "\u00a77No favorites yet. Open a game and click \u00a7eAdd to Favorites\u00a77.",
                    this.width / 2, this.height / 2, 0xFFFFFF);
        }

        // Draw rename field if active
        if (renameField != null && renameIdx >= 0) {
            int fieldY = startY + renameIdx * 26 + 22 - scrollY;
            renameField.yPosition = fieldY;
            if (fieldY >= startY - 10 && fieldY <= this.height - 50) {
                renameField.drawTextBox();
                // Label
                drawString(this.fontRendererObj, "\u00a77Name:\u00a7r", renameField.xPosition - 35, fieldY + 4, 0xFFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static class FavEntry {
        final String key;
        final String date;
        final int gameNum;
        final ZombiesGame game;
        String customName;

        FavEntry(String key, String date, int gameNum, ZombiesGame game, String customName) {
            this.key = key; this.date = date; this.gameNum = gameNum;
            this.game = game; this.customName = customName;
        }
    }
}
