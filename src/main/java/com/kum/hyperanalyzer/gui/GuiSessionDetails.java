package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.data.SessionData;
import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.ZombiesGame;
import com.kum.hyperanalyzer.tracking.ChestTracker;
import com.kum.hyperanalyzer.tracking.ZombiesDetector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class GuiSessionDetails extends GuiScreen {
    private final GuiScreen parent;
    private final String sessionDate;
    private SessionData sessionData;
    private int scrollY = 0;
    private int maxScroll = 0;
    private String selectedDifficulty = "Normal";

    // Chest/weapon map toggles — loaded from / saved to config
    private int chestMapIndex;
    private int weaponMapIndex;
    private static final String[] CHEST_MAP_NAMES  = {"Bad Blood", "Dead End", "Prison"};
    private static final String[] WEAPON_MAP_NAMES = {"Overall", "Bad Blood", "Dead End", "Prison", "Alien Arcadium"};
    private static final String[] MAP_NAMES = {"Bad Blood", "Dead End", "Prison", "Prison Escape", "Alien Arcadium"};

    private String[] endTimesForButtons = new String[50];
    private int[] endTimeYPositions     = new int[50];
    private int endTimeCount  = 0;
    private int buttonRightEdge = 0;

    // Delete confirm state: -1 = none, ≥0 = gameIdx in sessionData.games pending delete
    private int confirmDeleteGameIdx = -1;

    // Button ID ranges
    // 0..999        = game detail buttons (gameIdx in sessionData.games)
    // 4000..4999    = X delete buttons (4000 + gameIdx)
    // 2000          = live ongoing game
    // 2001          = difficulty toggle
    // 2003          = copy stats
    // 2004          = edit HUD
    // 3000/3001     = chest/weapon map toggle
    // 1000          = back
    // 5000          = confirm delete
    // 5001          = cancel delete
    private static final int BTN_DEL_BASE    = 4000;
    private static final int BTN_CONFIRM_DEL = 5000;
    private static final int BTN_CANCEL_DEL  = 5001;

    public GuiSessionDetails(GuiScreen parent, String sessionDate) {
        this.parent = parent;
        this.sessionDate = sessionDate;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.sessionData = SessionManager.loadSession(sessionDate);
        chestMapIndex  = Math.max(0, Math.min(ModConfig.instance.savedChestMapIndex,  CHEST_MAP_NAMES.length  - 1));
        weaponMapIndex = Math.max(0, Math.min(ModConfig.instance.savedWeaponMapIndex, WEAPON_MAP_NAMES.length - 1));
        selectedDifficulty = ModConfig.instance.savedDifficulty != null ? ModConfig.instance.savedDifficulty : "Normal";
        updateButtons();
    }

    @Override
    public void onGuiClosed() {
        ModConfig.instance.savedDifficulty = selectedDifficulty;
        ModConfig.save();
    }

    private void updateButtons() {
        this.buttonList.clear();
        endTimeCount = 0;

        // ── Confirm-delete mode: show ONLY confirm + cancel, nothing else ──
        if (confirmDeleteGameIdx >= 0) {
            this.buttonList.add(new GuiButton(BTN_CONFIRM_DEL, this.width / 2 - 106, this.height / 2,     100, 20, "\u00a7cConfirm Delete\u00a7r"));
            this.buttonList.add(new GuiButton(BTN_CANCEL_DEL,  this.width / 2 + 6,   this.height / 2,     100, 20, "\u00a7aCancel\u00a7r"));
            return;
        }

        // ── Normal mode ──
        if (sessionData == null || sessionData.games == null) {
            this.buttonList.add(new GuiButton(1000, this.width / 2 - 100, this.height - 30, 200, 20, "Back"));
            return;
        }

        int totalItems  = sessionData.games.size();
        boolean hasOngoing = ZombiesDetector.isInZombies && ZombiesDetector.currentGame != null;

        // Build non-reset played list, newest first
        java.util.List<Integer> playedIndices = new java.util.ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            if (!sessionData.games.get(i).isReset) playedIndices.add(i);
        }
        java.util.Collections.reverse(playedIndices);
        int displayCount = playedIndices.size() + (hasOngoing ? 1 : 0);

        // Layout: X button (20px) + gap (2px) + game button (fills remaining)
        int xBtnW   = 20;
        int gap     = 2;
        int totalW  = 284;
        int gameW   = totalW - xBtnW - gap;
        int listX   = this.width / 2 - totalW / 2;   // left edge of X button
        int gameBtnX = listX + xBtnW + gap;           // left edge of game button
        buttonRightEdge = gameBtnX + gameW;
        int startY  = 110;

        for (int i = 0; i < displayCount; i++) {
            int btnY = startY + (i * 22);
            if (hasOngoing && i == 0) {
                // Live game — no X button
                ZombiesGame ongoing = ZombiesDetector.currentGame;
                long elapsed = ZombiesDetector.ticksToMs(ZombiesDetector.currentTick() - ZombiesDetector.gameStartTick);
                String elapsedStr = ZombiesDetector.formatSplitMs(elapsed);
                String diffColor = GuiGameSessions.getDifficultyColor(ongoing.difficulty);
                String diffChar  = ongoing.difficulty.equals("Normal") ? "N" : ongoing.difficulty.equals("Hard") ? "H" : "R";
                String keyPart   = ongoing.keyPart != null ? ongoing.keyPart : "Unidentified";
                String kpChar    = keyPart.startsWith("Y") ? "Y" : keyPart.startsWith("C") ? "C" : "U";
                String kpColor   = kpChar.equals("Y") ? "\u00a7a" : kpChar.equals("C") ? "\u00a7b" : "\u00a77";
                String keyStr    = ongoing.map.equals("Prison") ? " " + kpColor + "(" + kpChar + ")\u00a7r" : "";
                String chestStr  = (ongoing.startChestLocation != null && !"Prison".equals(ongoing.map) && !"Alien Arcadium".equals(ongoing.map)) ? " \u00a7e[" + ongoing.startChestLocation + "]\u00a7r" : "";
                String label     = String.format("\u00a7e\u25B6\u00a7r \u00a77%s\u00a7r %s %s%s\u00a7r%s%s R%d",
                        elapsedStr, ongoing.map, diffColor, diffChar, keyStr, chestStr, ZombiesDetector.currentRound);
                this.buttonList.add(new GuiButton(2000, gameBtnX, btnY, gameW, 20, label));
            } else {
                int listIdx = hasOngoing ? i - 1 : i;
                if (listIdx >= 0 && listIdx < playedIndices.size()) {
                    int gameIdx    = playedIndices.get(listIdx);
                    int displayNum = playedIndices.size() - listIdx;
                    ZombiesGame game = sessionData.games.get(gameIdx);
                    String durationStr  = GuiGameSessions.formatDuration(game.duration);
                    String diffColor    = GuiGameSessions.getDifficultyColor(game.difficulty);
                    String diffName     = game.difficulty != null ? game.difficulty : "Normal";
                    String diffChar     = diffName.equals("Normal") ? "N" : diffName.equals("Hard") ? "H" : "R";
                    String outcomeColor = game.isWin ? "\u00a7a" : "\u00a7c";
                    String outcome      = game.isWin ? "Win" : "Loss";
                    String escapeTag    = game.isEscape ? " Escape" : "";
                    String keyPart      = game.keyPart != null ? game.keyPart : "Unidentified";
                    String kpChar       = keyPart.startsWith("Y") ? "Y" : keyPart.startsWith("C") ? "C" : "U";
                    String kpColor      = kpChar.equals("Y") ? "\u00a7a" : kpChar.equals("C") ? "\u00a7b" : "\u00a77";
                    String keyStr       = game.map.equals("Prison") ? " " + kpColor + "(" + kpChar + ")\u00a7r" : "";
                    String chestStr     = (game.startChestLocation != null && !"Prison".equals(game.map) && !"Alien Arcadium".equals(game.map)) ? " \u00a7e[" + game.startChestLocation + "]\u00a7r" : "";
                    int roundEnd        = game.lastRound > 0 ? game.lastRound : game.rounds != null ? game.rounds.size() : 0;
                    String label = String.format(
                            "Game %d: \u00a77%s\u00a7r %s %s%s\u00a7r%s%s DG:\u00a76%d\u00a7r R%d (%s%s\u00a7r%s)",
                            displayNum, durationStr, game.map, diffColor, diffChar, keyStr, chestStr,
                            game.doubleGoldCount, roundEnd, outcomeColor, outcome, escapeTag);
                    // X delete button (left)
                    this.buttonList.add(new GuiButton(BTN_DEL_BASE + gameIdx, listX, btnY, xBtnW, 20, "\u00a7c\u2715\u00a7r"));
                    // Game detail button (right)
                    this.buttonList.add(new GuiButton(gameIdx, gameBtnX, btnY, gameW, 20, label));
                }
            }
        }

        maxScroll = Math.max(0, (displayCount * 22) - (this.height - 150));

        String diffColor = GuiGameSessions.getDifficultyColor(selectedDifficulty);
        this.buttonList.add(new GuiButton(2001, this.width - 105, 5, 100, 20,
                "Diff: " + diffColor + selectedDifficulty + "\u00a7r"));
        this.buttonList.add(new GuiButton(3000, this.width - 210, this.height - 72, 100, 18,
                "Chest: " + CHEST_MAP_NAMES[chestMapIndex]));
        this.buttonList.add(new GuiButton(3001, this.width - 210, this.height - 50, 100, 18,
                "Weapon: " + WEAPON_MAP_NAMES[weaponMapIndex]));
        this.buttonList.add(new GuiButton(2004, this.width - 105, 28, 100, 20, "Edit HUD"));
        this.buttonList.add(new GuiButton(2003, 5, this.height - 30, 80, 20, "Copy Stats"));
        this.buttonList.add(new GuiButton(1000, this.width / 2 - 100, this.height - 30, 200, 20, "Back"));
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        if (confirmDeleteGameIdx >= 0) return; // block scrolling during confirm
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollY += (wheel > 0 ? -22 : 22);
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        // ── Confirm-delete mode ──
        if (button.id == BTN_CONFIRM_DEL) {
            if (confirmDeleteGameIdx >= 0 && sessionData != null && sessionData.games != null
                    && confirmDeleteGameIdx < sessionData.games.size()) {
                ZombiesGame g = sessionData.games.get(confirmDeleteGameIdx);
                if (!g.isReset) {
                    g.isReset = true;
                    sessionData.zombiesGamesTotal = Math.max(0, sessionData.zombiesGamesTotal - 1);
                    sessionData.zombiesGamesReset++;
                    if (g.isWin) sessionData.zombiesGamesWon = Math.max(0, sessionData.zombiesGamesWon - 1);
                    else         sessionData.zombiesGamesLost = Math.max(0, sessionData.zombiesGamesLost - 1);
                }
                saveSession();
            }
            confirmDeleteGameIdx = -1;
            // Re-open this screen fresh so no phantom click fires on a game button
            this.mc.displayGuiScreen(new GuiSessionDetails(parent, sessionDate));
            return;
        }
        if (button.id == BTN_CANCEL_DEL) {
            confirmDeleteGameIdx = -1;
            this.mc.displayGuiScreen(new GuiSessionDetails(parent, sessionDate));
            return;
        }

        // ── Normal actions ──
        if (button.id == 1000) {
            this.mc.displayGuiScreen(parent);
        } else if (button.id == 2000) {
            if (ZombiesDetector.currentGame != null)
                this.mc.displayGuiScreen(new GuiGameDetails(this, ZombiesDetector.currentGame, -1));
        } else if (button.id == 2001) {
            if (selectedDifficulty.equals("Normal")) selectedDifficulty = "Hard";
            else if (selectedDifficulty.equals("Hard")) selectedDifficulty = "RIP";
            else selectedDifficulty = "Normal";
            updateButtons();
        } else if (button.id == 2003) {
            copySessionStats();
        } else if (button.id == 2004) {
            this.mc.displayGuiScreen(new GuiEditHud(this, GuiEditHud.Mode.SESSION_DETAILS));
        } else if (button.id == 3000) {
            chestMapIndex = (chestMapIndex + 1) % CHEST_MAP_NAMES.length;
            ModConfig.instance.savedChestMapIndex = chestMapIndex;
            ModConfig.save();
            updateButtons();
        } else if (button.id == 3001) {
            weaponMapIndex = (weaponMapIndex + 1) % WEAPON_MAP_NAMES.length;
            ModConfig.instance.savedWeaponMapIndex = weaponMapIndex;
            ModConfig.save();
            updateButtons();
        } else if (button.id >= BTN_DEL_BASE && button.id < BTN_DEL_BASE + 1000) {
            // X button pressed — enter confirm mode
            confirmDeleteGameIdx = button.id - BTN_DEL_BASE;
            updateButtons();
        } else if (button.id >= 0 && button.id < (sessionData != null ? sessionData.games.size() : 0)) {
            ZombiesGame game = sessionData.games.get(button.id);
            this.mc.displayGuiScreen(new GuiGameDetails(this, game, button.id + 1, sessionDate));
        }
    }

    private void saveSession() {
        if (SessionManager.currentSession != null && SessionManager.currentSession.date.equals(sessionDate)) {
            SessionManager.saveCurrentSession();
        } else {
            try (java.io.FileWriter fw = new java.io.FileWriter(
                    new java.io.File(SessionManager.sessionDir, sessionDate + ".json"))) {
                SessionManager.GSON.toJson(sessionData, fw);
            } catch (Exception ignored) {}
        }
    }

    private void copySessionStats() {
        if (sessionData == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("--- Session Stats ---\n");
        int yardCount = 0, courtsCount = 0;
        for (int i = 0; i < sessionData.games.size(); i++) {
            ZombiesGame g = sessionData.games.get(i);
            if (g.isReset) continue;
            String timeStr = GuiGameSessions.formatDuration(g.duration);
            String keyPart = g.keyPart != null ? g.keyPart : "Unidentified";
            String kpChar  = keyPart.startsWith("Y") ? "Y" : keyPart.startsWith("C") ? "C" : "U";
            if (kpChar.equals("Y")) yardCount++;
            else if (kpChar.equals("C")) courtsCount++;
            String keyStr    = g.map.equals("Prison") ? " (" + kpChar + ")" : "";
            String chestStr  = (g.startChestLocation != null && !"Prison".equals(g.map) && !"Alien Arcadium".equals(g.map)) ? " [" + g.startChestLocation + "]" : "";
            String outcome   = g.isWin ? "Win" : "Loss";
            String escapeTag = g.isEscape ? " Escape" : "";
            int roundEnd     = g.lastRound > 0 ? g.lastRound : g.rounds != null ? g.rounds.size() : 0;
            sb.append(String.format("Game %d: %s %s%s%s %ddg %s R%d%s\n",
                    i + 1, g.map, timeStr, keyStr, chestStr, g.doubleGoldCount, outcome, roundEnd, escapeTag));
        }
        int totalDgs = 0;
        for (ZombiesGame g : sessionData.games) totalDgs += g.doubleGoldCount;
        int playedCount = sessionData.countPlayed();
        double wr = playedCount > 0 ? (sessionData.zombiesGamesWon * 100.0 / playedCount) : 0;
        sb.append(String.format("Yard/Courts: %d/%d\n", yardCount, courtsCount));
        sb.append(String.format("Total Games: %d | Played: %d | Resets: %d | W: %d L: %d | WR: %.0f%% | Total DGs: %d\n",
                sessionData.games.size(), playedCount, sessionData.countResets(),
                sessionData.zombiesGamesWon, sessionData.zombiesGamesLost, wr, totalDgs));
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        } catch (Exception ignored) {}
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // ── Confirm-delete overlay: show ONLY the prompt + two buttons ──
        if (confirmDeleteGameIdx >= 0) {
            if (sessionData != null && confirmDeleteGameIdx < sessionData.games.size()) {
                ZombiesGame g = sessionData.games.get(confirmDeleteGameIdx);
                String mapLabel = g.map != null ? g.map : "this game";
                drawCenteredString(fontRendererObj, "\u00a7lDelete Game?\u00a7r", width / 2, this.height / 2 - 22, 0xFFFFFF);
                drawCenteredString(fontRendererObj,
                        "\u00a77" + mapLabel + " \u00a77\u2014 counted as reset, chest/yard stats kept.",
                        width / 2, this.height / 2 - 10, 0xAAAAAA);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
            return; // skip all normal drawing
        }

        // ── Normal drawing ──

        // Scroll game buttons + X buttons together
        int listStartY = 110;
        endTimeCount   = 0;
        int rankCounter = 0;
        for (GuiButton btn : this.buttonList) {
            boolean isGameBtn = btn.id >= 0 && btn.id < 1000;
            boolean isDelBtn  = btn.id >= BTN_DEL_BASE && btn.id < BTN_DEL_BASE + 1000;
            boolean isLive    = btn.id == 2000;
            if (isGameBtn || isDelBtn || isLive) {
                // Game btn and its paired X btn share the same rank slot
                if (!isDelBtn) {
                    btn.yPosition = listStartY + (rankCounter * 22) - scrollY;
                    btn.visible   = btn.yPosition >= listStartY - 10 && btn.yPosition <= this.height - 40;
                    if (isGameBtn && btn.visible && endTimeCount < endTimesForButtons.length && sessionData != null) {
                        ZombiesGame game = sessionData.games.get(btn.id);
                        if (game.endTime > 0) {
                            endTimesForButtons[endTimeCount] = new SimpleDateFormat("h:mm:ss a").format(new java.util.Date(game.endTime));
                            endTimeYPositions[endTimeCount]  = btn.yPosition + 6;
                            endTimeCount++;
                        }
                    }
                    rankCounter++;
                } else {
                    // X button: match its paired game button position
                    int pairedGameId = btn.id - BTN_DEL_BASE;
                    for (GuiButton other : this.buttonList) {
                        if (other.id == pairedGameId) {
                            btn.yPosition = other.yPosition;
                            btn.visible   = other.visible;
                            break;
                        }
                    }
                }
            }
        }

        String prettyDate = GuiGameSessions.formatDatePretty(sessionDate);
        this.drawCenteredString(fontRendererObj, "\u00a7lSession: " + prettyDate + "\u00a7r", width / 2, 5, 0xFFFFFF);

        if (sessionData != null) {
            int totalAll    = sessionData.games != null ? sessionData.games.size() : 0;
            int resetCount  = sessionData.countResets();
            int playedCount = sessionData.countPlayed();
            double winRate  = playedCount > 0 ? (sessionData.zombiesGamesWon * 100.0 / playedCount) : 0;
            this.drawCenteredString(fontRendererObj, String.format(
                    "Total: \u00a77%d\u00a7r  Played: \u00a7a%d\u00a7r (W: \u00a7a%d\u00a7r / L: \u00a7c%d\u00a7r  WR: \u00a7a%.0f%%\u00a7r)  Resets: \u00a7e%d\u00a7r",
                    totalAll, playedCount, sessionData.zombiesGamesWon, sessionData.zombiesGamesLost, winRate, resetCount),
                    width / 2, 17, 0xFFFFFF);

            String timeZombies = formatTimeColored(sessionData.totalZombiesPlaytime);
            String timeTotal   = formatTimeColored(sessionData.totalHypixelPlaytime);
            this.drawCenteredString(fontRendererObj,
                    "Zombies: " + timeZombies + " | Hypixel: " + timeTotal, width / 2, 28, 0xFFFFFF);

            int totalDgs = 0, yardCount = 0, courtsCount = 0;
            for (ZombiesGame g : sessionData.games) {
                totalDgs += g.doubleGoldCount;
                if (g.keyPart != null) {
                    if (g.keyPart.startsWith("Y")) yardCount++;
                    else if (g.keyPart.startsWith("C")) courtsCount++;
                }
            }
            this.drawCenteredString(fontRendererObj,
                    "Total Double Golds: \u00a76" + totalDgs + "\u00a7r | Yard/Courts: \u00a7a" + yardCount + "\u00a7r/\u00a7b" + courtsCount + "\u00a7r",
                    width / 2, 39, 0xFFFFFF);

            // Fastest Time panel
            {
                int[] pos = HudLayout.sessionFastest(width, height);
                int x = pos[0], y = pos[1];
                drawString(fontRendererObj, "\u00a7lFastest Time (" + selectedDifficulty + ")\u00a7r", x, y, 0xFFFFFF); y += 11;
                for (String mapName : MAP_NAMES) {
                    if (mapName.equals("Alien Arcadium") && !selectedDifficulty.equals("Normal")) continue;
                    long fastest = getFastestTime(mapName, selectedDifficulty);
                    String ts = fastest == Long.MAX_VALUE ? "\u00a77None\u00a7r" : "\u00a7a" + GuiGameSessions.formatDuration(fastest) + "\u00a7r";
                    drawString(fontRendererObj, mapName + ": " + ts, x, y, 0xFFFFFF); y += 10;
                }
            }

            // Avg Time panel
            {
                int[] pos = HudLayout.sessionAvgTime(width, height);
                int x = pos[0], y = pos[1];
                drawString(fontRendererObj, "\u00a7lAvg Time (" + selectedDifficulty + ")\u00a7r", x, y, 0xFFFFFF); y += 11;
                for (String mapName : MAP_NAMES) {
                    if (mapName.equals("Alien Arcadium") && !selectedDifficulty.equals("Normal")) continue;
                    long avg = getAverageWinTime(mapName, selectedDifficulty);
                    String ts = avg == 0 ? "\u00a77None\u00a7r" : "\u00a7a" + GuiGameSessions.formatDuration(avg) + "\u00a7r";
                    drawString(fontRendererObj, mapName + ": " + ts, x, y, 0xFFFFFF); y += 10;
                }
            }

            // R2 Stats panel
            {
                int[] pos = HudLayout.r2Stats(width, height);
                int x = pos[0], y = pos[1];
                int r2MaxCount = 0, r2InsCount = 0, gamesWithMax = 0, gamesWithIns = 0;
                for (ZombiesGame g : sessionData.games) {
                    if (g.maxAmmoPattern != null && !g.maxAmmoPattern.isEmpty()) {
                        gamesWithMax++;
                        if (g.maxAmmoPattern.get(0).equals("r2")) r2MaxCount++;
                    }
                    if (g.instaKillPattern != null && !g.instaKillPattern.isEmpty()) {
                        gamesWithIns++;
                        if (g.instaKillPattern.get(0).equals("r2")) r2InsCount++;
                    }
                }
                double r2MaxPct = gamesWithMax > 0 ? (r2MaxCount * 100.0 / gamesWithMax) : 0;
                double r2InsPct = gamesWithIns > 0 ? (r2InsCount * 100.0 / gamesWithIns) : 0;
                drawString(fontRendererObj, "\u00a79R2 Max Ammo\u00a7r: \u00a77" + String.format("%.0f%%", r2MaxPct) + "\u00a7r", x, y, 0xFFFFFF); y += 11;
                drawString(fontRendererObj, "\u00a7cR2 Insta Kill\u00a7r: \u00a77" + String.format("%.0f%%", r2InsPct) + "\u00a7r", x, y, 0xFFFFFF);
            }

            // Session Chests panel
            {
                int[] pos = HudLayout.sessionChests(width, height);
                int x = pos[0], y = pos[1];
                String chestMap = CHEST_MAP_NAMES[chestMapIndex];
                drawString(fontRendererObj, "\u00a7lSession Chests: " + chestMap + "\u00a7r", x, y, 0xFFFFFF); y += 11;
                Map<String, Integer> counts = getSessionChestCounts(chestMap);
                int total = 0; for (int v : counts.values()) total += v;
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                    drawString(fontRendererObj,
                            String.format("\u00a76%s\u00a7r: \u00a7a%d\u00a7r (\u00a77%.1f%%\u00a7r)", e.getKey(), e.getValue(), pct),
                            x, y, 0xFFFFFF); y += 10;
                }
                if (total == 0) drawString(fontRendererObj, "\u00a77No data\u00a7r", x, y, 0xFFFFFF);
            }

            // Session Weapons panel
            {
                int[] pos = HudLayout.sessionWeapons(width, height);
                int x = pos[0], y = pos[1];
                String weaponMap = WEAPON_MAP_NAMES[weaponMapIndex];
                drawString(fontRendererObj, "\u00a7lSession Weapons: " + weaponMap + "\u00a7r", x, y, 0xFFFFFF); y += 11;
                Map<String, Integer> counts = getSessionWeaponCounts(weaponMap);
                int total = 0; for (int v : counts.values()) total += v;
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                    drawString(fontRendererObj,
                            String.format("\u00a76%s\u00a7r: \u00a7a%d\u00a7r (\u00a77%.1f%%\u00a7r)", e.getKey(), e.getValue(), pct),
                            x, y, 0xFFFFFF); y += 10;
                }
                if (total == 0) drawString(fontRendererObj, "\u00a77No data\u00a7r", x, y, 0xFFFFFF);
            }
        } else {
            drawCenteredString(fontRendererObj, "\u00a7cError loading session data.", width / 2, 40, 0xFFFFFF);
        }

        // Draw end times next to game buttons
        for (int i = 0; i < endTimeCount; i++) {
            drawString(fontRendererObj, "\u00a77" + endTimesForButtons[i] + "\u00a7r",
                    buttonRightEdge + 4, endTimeYPositions[i], 0xFFFFFF);
        }

        if (ZombiesDetector.isInZombies) updateButtons();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ---- Data helpers ----

    private Map<String, Integer> getSessionChestCounts(String map) {
        Map<String, Integer> c = new LinkedHashMap<>();
        if (sessionData == null || sessionData.games == null) return c;
        for (ZombiesGame g : sessionData.games) {
            if (!map.equals(g.map) || g.startChestLocation == null) continue;
            c.put(g.startChestLocation, c.getOrDefault(g.startChestLocation, 0) + 1);
        }
        ZombiesGame live = ZombiesDetector.currentGame;
        if (live != null && live.isOngoing && map.equals(live.map) && live.startChestLocation != null) {
            if (sessionData == SessionManager.currentSession) {
                c.put(live.startChestLocation, c.getOrDefault(live.startChestLocation, 0) + 1);
            }
        }
        return c;
    }

    private Map<String, Integer> getSessionWeaponCounts(String map) {
        Map<String, Integer> c = new LinkedHashMap<>();
        boolean isOverall = "Overall".equals(map);
        if (!isOverall) {
            for (String w : ChestTracker.getWeaponsForMap(map)) c.put(w, 0);
        } else {
            for (String w : ChestTracker.BB_DE_WEAPONS)  c.put(w, 0);
            for (String w : ChestTracker.PRISON_WEAPONS) c.put(w, 0);
            for (String w : ChestTracker.AA_WEAPONS)     c.put(w, 0);
        }
        if (sessionData == null || sessionData.games == null) return c;
        for (ZombiesGame g : sessionData.games) {
            if (!isOverall && !map.equals(g.map)) continue;
            if (g.weaponRolls == null) continue;
            for (Map.Entry<String, Integer> e : g.weaponRolls.entrySet())
                c.put(e.getKey(), c.getOrDefault(e.getKey(), 0) + e.getValue());
        }
        ZombiesGame live = ZombiesDetector.currentGame;
        if (live != null && live.isOngoing && live.weaponRolls != null) {
            if (sessionData == SessionManager.currentSession) {
                if (isOverall || map.equals(live.map)) {
                    for (Map.Entry<String, Integer> e : live.weaponRolls.entrySet())
                        c.put(e.getKey(), c.getOrDefault(e.getKey(), 0) + e.getValue());
                }
            }
        }
        if (isOverall) c.entrySet().removeIf(e -> e.getValue() == 0);
        return c;
    }

    private long getFastestTime(String mapName, String difficulty) {
        if (sessionData == null || sessionData.games == null) return Long.MAX_VALUE;
        long fastest = Long.MAX_VALUE;
        for (ZombiesGame g : sessionData.games)
            if (!g.isReset && g.isWin && GuiGameSessions.matchesMap(g, mapName) && GuiGameSessions.matchesDifficulty(g, difficulty))
                if (g.duration < fastest) fastest = g.duration;
        return fastest;
    }

    private long getAverageWinTime(String mapName, String difficulty) {
        if (sessionData == null || sessionData.games == null) return 0;
        long total = 0; int count = 0;
        for (ZombiesGame g : sessionData.games)
            if (!g.isReset && g.isWin && GuiGameSessions.matchesMap(g, mapName) && GuiGameSessions.matchesDifficulty(g, difficulty)) {
                total += g.duration; count++;
            }
        return count > 0 ? total / count : 0;
    }

    private String formatTimeColored(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        return String.format("\u00a7a%d\u00a7r\u00a7ah\u00a7r \u00a7a%02d\u00a7r\u00a7am\u00a7r \u00a7a%02d\u00a7r\u00a7as\u00a7r", h, m % 60, s % 60);
    }
}
