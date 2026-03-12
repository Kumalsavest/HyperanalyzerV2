package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.data.SessionData;
import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.ZombiesGame;
import com.kum.hyperanalyzer.tracking.AutoReset;
import com.kum.hyperanalyzer.tracking.ChestTracker;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.text.SimpleDateFormat;
import java.util.*;

public class GuiGameSessions extends GuiScreen {
    private List<String> sessions;
    private int scrollY = 0;
    private int maxScroll = 0;
    private String selectedDifficulty = "Normal";

    // Delete session confirmation state
    private String pendingDeleteSession = null; // date string of session pending delete confirmation
    private static final int BTN_DELETE_BASE = 5000; // delete buttons: 5000 + session index
    private static final int BTN_DEL_CONFIRM  = 5500;
    private static final int BTN_DEL_CANCEL   = 5501;

    // Chest / Weapon map toggle — loaded from / saved to config
    private int chestMapIndex;
    private int weaponMapIndex;
    private static final String[] CHEST_MAP_NAMES  = {"Bad Blood", "Dead End", "Prison"};
    private static final String[] WEAPON_MAP_NAMES = {"Overall", "Bad Blood", "Dead End", "Prison", "Alien Arcadium"};
    private static final String[] MAP_NAMES = {"Bad Blood","Dead End","Prison","Prison Escape","Alien Arcadium"};

    @Override
    public void initGui() {
        super.initGui();
        // Restore saved map indices
        chestMapIndex  = Math.max(0, Math.min(ModConfig.instance.savedChestMapIndex,  CHEST_MAP_NAMES.length  - 1));
        weaponMapIndex = Math.max(0, Math.min(ModConfig.instance.savedWeaponMapIndex, WEAPON_MAP_NAMES.length - 1));
        // Restore saved difficulty
        selectedDifficulty = ModConfig.instance.savedDifficulty != null ? ModConfig.instance.savedDifficulty : "Normal";
        this.sessions  = SessionManager.getAllSavedSessions();
        updateButtons();
    }

    @Override
    public void onGuiClosed() {
        ModConfig.instance.savedDifficulty = selectedDifficulty;
        ModConfig.save();
    }

    private void updateButtons() {
        this.buttonList.clear();

        // ── Confirm-delete mode: show ONLY confirm + cancel ──
        if (pendingDeleteSession != null) {
            this.buttonList.add(new GuiButton(BTN_DEL_CONFIRM, this.width / 2 - 106, this.height / 2,     100, 20, "\u00a7cConfirm Delete\u00a7r"));
            this.buttonList.add(new GuiButton(BTN_DEL_CANCEL,  this.width / 2 + 6,   this.height / 2,     100, 20, "\u00a7aCancel\u00a7r"));
            return;
        }

        // ── Normal mode ──
        int startY = 85;
        for (int i = 0; i < sessions.size(); i++) {
            // Main session button (narrower to leave room for X delete)
            this.buttonList.add(new GuiButton(i, this.width / 2 - 120, startY + (i * 22), 212, 20,
                    formatDatePretty(sessions.get(i))));
            // Small X delete button to the right
            this.buttonList.add(new GuiButton(BTN_DELETE_BASE + i, this.width / 2 + 96, startY + (i * 22), 24, 20, "\u00a7c\u2715\u00a7r"));
        }
        maxScroll = Math.max(0, (sessions.size() * 22) - (this.height - 180));

        // Top-right fixed buttons
        String diffColor = getDifficultyColor(selectedDifficulty);
        this.buttonList.add(new GuiButton(2000, this.width - 105, 5,  100, 20, "Diff: " + diffColor + selectedDifficulty + "\u00a7r"));

        String infoState = GiveInfoHud.isEnabled() ? "\u00a7aOn" : "\u00a7cOff";
        this.buttonList.add(new GuiButton(2001, this.width - 105, 28, 100, 20, "Give Info: " + infoState + "\u00a7r"));

        this.buttonList.add(new GuiButton(2004, this.width - 105, 51, 100, 20, "Edit HUD"));
        this.buttonList.add(new GuiButton(2005, this.width - 105, 74, 100, 20, "Edit HUD (HUD)"));

        String arState = ModConfig.instance.autoResetEnabled ? "\u00a7aON" : "\u00a7cOFF";
        this.buttonList.add(new GuiButton(2006, this.width - 105, 97, 100, 20, "Auto Reset: " + arState + "\u00a7r"));

        int favCount = ModConfig.instance.favoriteGames != null ? ModConfig.instance.favoriteGames.size() : 0;
        this.buttonList.add(new GuiButton(2007, this.width - 105, 120, 100, 20,
                "\u00a7e\u2605 Favorites\u00a7r" + (favCount > 0 ? " \u00a77(" + favCount + ")\u00a7r" : "")));

        this.buttonList.add(new GuiButton(3000, this.width - 210, this.height - 72, 100, 18,
                "Chest: " + CHEST_MAP_NAMES[chestMapIndex]));
        this.buttonList.add(new GuiButton(3001, this.width - 210, this.height - 50, 100, 18,
                "Weapon: " + WEAPON_MAP_NAMES[weaponMapIndex]));

        this.buttonList.add(new GuiButton(1002, this.width / 2 - 100, this.height - 30, 200, 20, "Close"));
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollY += (wheel > 0 ? -22 : 22);
            if (scrollY < 0) scrollY = 0;
            if (scrollY > maxScroll) scrollY = maxScroll;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1002) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 2000) {
            if (selectedDifficulty.equals("Normal")) selectedDifficulty = "Hard";
            else if (selectedDifficulty.equals("Hard")) selectedDifficulty = "RIP";
            else selectedDifficulty = "Normal";
            updateButtons();
        } else if (button.id == 2001) {
            GiveInfoHud.setEnabled(!GiveInfoHud.isEnabled());
            updateButtons();
        } else if (button.id == 2004) {
            // Edit HUD for Sessions screen
            this.mc.displayGuiScreen(new GuiEditHud(this, GuiEditHud.Mode.SESSIONS));
        } else if (button.id == 2005) {
            // Edit HUD for GiveInfo overlay
            this.mc.displayGuiScreen(new GuiEditHud(this, GuiEditHud.Mode.GIVE_INFO));
        } else if (button.id == 2006) {
            // Open Auto Reset settings
            this.mc.displayGuiScreen(new GuiAutoReset(this));
        } else if (button.id == 2007) {
            // Open Favorites screen
            this.mc.displayGuiScreen(new GuiFavorites(this));
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
        } else if (button.id == BTN_DEL_CONFIRM && pendingDeleteSession != null) {
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            if (pendingDeleteSession.equals(today)) {
                // Today's session — clear games but keep session alive
                SessionManager.clearCurrentSession();
            } else {
                // Past session — delete the file entirely
                SessionManager.deleteSession(pendingDeleteSession);
            }
            pendingDeleteSession = null;
            this.sessions = SessionManager.getAllSavedSessions();
            updateButtons();
        } else if (button.id == BTN_DEL_CANCEL) {
            pendingDeleteSession = null;
            updateButtons();
        } else if (button.id >= BTN_DELETE_BASE && button.id < BTN_DELETE_BASE + sessions.size()) {
            // Request delete confirm for this session
            pendingDeleteSession = sessions.get(button.id - BTN_DELETE_BASE);
            updateButtons();
        } else if (button.id >= 0 && button.id < sessions.size()) {
            if (pendingDeleteSession == null) { // don't open session while confirm is showing
                this.mc.displayGuiScreen(new GuiSessionDetails(this, sessions.get(button.id)));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // ── Confirm-delete mode: show ONLY the prompt + two buttons ──
        if (pendingDeleteSession != null) {
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            boolean isToday = pendingDeleteSession.equals(today);
            drawCenteredString(fontRendererObj, "\u00a7lDelete Session?\u00a7r", width / 2, this.height / 2 - 22, 0xFFFFFF);
            drawCenteredString(fontRendererObj, formatDatePretty(pendingDeleteSession), width / 2, this.height / 2 - 10, 0xFFFFFF);
            if (isToday) {
                drawCenteredString(fontRendererObj,
                        "\u00a77Today's session will be \u00a7ecleared\u00a7r\u00a77 — all games removed, fresh start.",
                        width / 2, this.height / 2 + 24, 0xAAAAAA);
            } else {
                drawCenteredString(fontRendererObj,
                        "\u00a7cThis permanently deletes ALL games and stats in this session!",
                        width / 2, this.height / 2 + 24, 0xFF5555);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
            return; // skip all normal drawing
        }

        // ── Normal mode ──

        // Scroll session + X buttons together
        int listStartY = 85;
        for (GuiButton btn : this.buttonList) {
            if (btn.id >= 0 && btn.id < 1000) {
                btn.yPosition = listStartY + (btn.id * 22) - scrollY;
                btn.visible   = btn.yPosition >= listStartY - 10 && btn.yPosition <= this.height - 60;
            } else if (btn.id >= BTN_DELETE_BASE && btn.id < BTN_DELETE_BASE + 1000) {
                int idx = btn.id - BTN_DELETE_BASE;
                btn.yPosition = listStartY + (idx * 22) - scrollY;
                btn.visible   = btn.yPosition >= listStartY - 10 && btn.yPosition <= this.height - 60;
            }
        }

        this.drawCenteredString(this.fontRendererObj,
                "\u00a7lHyperAnalyzer - Game Sessions\u00a7r", this.width / 2, 10, 0xFFFFFF);

        // Total playtime across all sessions
        {
            long totalHypixel = 0;
            long totalZombies = 0;
            int globalWon = 0, globalLost = 0, globalPlayed = 0;
            for (String d : sessions) {
                com.kum.hyperanalyzer.data.SessionData sd = com.kum.hyperanalyzer.data.SessionManager.loadSession(d);
                if (sd == null) continue;
                totalHypixel += sd.totalHypixelPlaytime;
                totalZombies += sd.accurateZombiesPlaytime();
                globalPlayed += sd.zombiesGamesTotal;
                globalWon += sd.zombiesGamesWon;
                globalLost += sd.zombiesGamesLost;
            }
            this.drawCenteredString(this.fontRendererObj,
                    "\u00a77Hypixel: \u00a7a" + formatLongDuration(totalHypixel) + "\u00a7r  \u00a77Zombies: \u00a7a" + formatLongDuration(totalZombies) + "\u00a7r",
                    this.width / 2, 21, 0xFFFFFF);
                    
            double globalWr = globalPlayed > 0 ? (globalWon * 100.0 / globalPlayed) : 0;
            this.drawCenteredString(this.fontRendererObj,
                    "Global W/L: \u00a7a" + String.format("%.0f%%", globalWr) + "\u00a7r (W: \u00a7a" + globalWon + "\u00a7r / L: \u00a7c" + globalLost + "\u00a7r)",
                    this.width / 2, 32, 0xFFFFFF);
        }

        // Overall Fastest Times panel — position from config
        {
            int[] pos = HudLayout.overallFastest(width, height);
            int x = pos[0], y = pos[1];
            this.drawString(fontRendererObj, "\u00a7lOverall Fastest Times (" + selectedDifficulty + ")\u00a7r", x, y, 0xFFFFFF); y += 12;
            for (String mapName : MAP_NAMES) {
                if (mapName.equals("Alien Arcadium") && !selectedDifficulty.equals("Normal")) continue;
                long fastest = getOverallFastestTime(mapName, selectedDifficulty);
                String ts = fastest == Long.MAX_VALUE ? "\u00a77None\u00a7r" : "\u00a7a" + formatDuration(fastest) + "\u00a7r";
                this.drawString(fontRendererObj, mapName + ": " + ts, x, y, 0xFFFFFF); y += 10;
            }
        }

        // Daily Averages + Session Count panel
        {
            int[] pos = HudLayout.dailyAverages(width, height);
            int x = pos[0], y = pos[1];
            this.drawString(fontRendererObj, "\u00a7lEst. Daily Averages\u00a7r", x, y, 0xFFFFFF); y += 12;
            double[] avgs = computeDailyAverages();
            int totalGames  = (int) avgs[3];
            double totalDG  = avgs[4];
            int yardCount   = (int) avgs[5];
            int courtsCount = (int) avgs[6];
            int prisonGames = (int) avgs[7];
            int totalResets = (int) avgs[8];
            double dgPct    = (totalGames + totalResets) > 0 ? (totalDG / (totalGames + totalResets)) * 100.0 : 0;
            double yardPct   = prisonGames > 0 ? (yardCount   * 100.0 / prisonGames) : 0;
            double courtsPct = prisonGames > 0 ? (courtsCount * 100.0 / prisonGames) : 0;
            this.drawString(fontRendererObj,
                    "\u00a76Double Gold/game\u00a7r: \u00a7a" + String.format("%.0f%%", dgPct) + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a79R2 Max Ammo\u00a7r: \u00a7a" + String.format("%.0f%%", avgs[1]) + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7cR2 Insta Kill\u00a7r: \u00a7a" + String.format("%.0f%%", avgs[2]) + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7aYard\u00a7r: \u00a7a" + yardCount + "\u00a7r (\u00a7a" + String.format("%.0f%%", yardPct) + "\u00a7r)"
                    + "  \u00a7bCourts\u00a7r: \u00a7a" + courtsCount + "\u00a7r (\u00a7a" + String.format("%.0f%%", courtsPct) + "\u00a7r)",
                    x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7lTotal Sessions\u00a7r: \u00a7a" + sessions.size() + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7lTotal Games\u00a7r: \u00a7a" + (totalGames + totalResets) + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7lTotal Games Played\u00a7r: \u00a7a" + totalGames + "\u00a7r", x, y, 0xFFFFFF); y += 10;
            this.drawString(fontRendererObj,
                    "\u00a7lTotal Games Reset\u00a7r: \u00a7e" + totalResets + "\u00a7r", x, y, 0xFFFFFF);
        }

        // Chest Stats panel
        {
            int[] pos = HudLayout.chestStats(width, height);
            int x = pos[0], y = pos[1];
            String chestMap = CHEST_MAP_NAMES[chestMapIndex];
            this.drawString(fontRendererObj, "\u00a7lChest Stats: " + chestMap + "\u00a7r", x, y, 0xFFFFFF); y += 11;
            Map<String, Integer> counts = getTotalChestCounts(chestMap);
            int total = 0; for (int v : counts.values()) total += v;
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                this.drawString(fontRendererObj,
                        String.format("\u00a76%s\u00a7r: \u00a7a%d\u00a7r (\u00a77%.1f%%\u00a7r)", e.getKey(), e.getValue(), pct),
                        x, y, 0xFFFFFF); y += 10;
            }
            if (total == 0) { this.drawString(fontRendererObj, "\u00a77No data\u00a7r", x, y, 0xFFFFFF); }
        }

        // Weapon Stats panel
        {
            int[] pos = HudLayout.weaponStats(width, height);
            int x = pos[0], y = pos[1];
            String weaponMap = WEAPON_MAP_NAMES[weaponMapIndex];
            this.drawString(fontRendererObj, "\u00a7lWeapon Rolls: " + weaponMap + "\u00a7r", x, y, 0xFFFFFF); y += 11;
            Map<String, Integer> counts = getTotalWeaponCounts(weaponMap);
            int total = 0; for (int v : counts.values()) total += v;
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                double pct = total > 0 ? (e.getValue() * 100.0 / total) : 0;
                this.drawString(fontRendererObj,
                        String.format("\u00a76%s\u00a7r: \u00a7a%d\u00a7r (\u00a77%.1f%%\u00a7r)", e.getKey(), e.getValue(), pct),
                        x, y, 0xFFFFFF); y += 10;
            }
            if (total == 0) { this.drawString(fontRendererObj, "\u00a77No data\u00a7r", x, y, 0xFFFFFF); }
        }

        // Auto-reset status hint
        if (ModConfig.instance.autoResetEnabled) {
            String arMap = ModConfig.instance.autoResetMap;
            String[] targets = com.kum.hyperanalyzer.tracking.AutoReset.getTargetsForMap(arMap);
            StringBuilder tSb = new StringBuilder();
            for (String t : targets) {
                if (t != null && !t.equals("NA")) {
                    if (tSb.length() > 0) tSb.append(", ");
                    tSb.append(t);
                }
            }
            String tStr = tSb.length() > 0 ? tSb.toString() : "Any";
            String extra = "Prison".equals(arMap) && ModConfig.instance.arPrisonYardOnly ? " +ResetYard" : "";
            String arHint = "\u00a7cAuto Reset ON\u00a7r: \u00a7e" + arMap + "\u00a7r \u00a77\u2192 \u00a76" + tStr + extra + "\u00a7r";
            this.drawString(fontRendererObj, arHint, 5, this.height - 50, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ---- Stats helpers ----

    // Returns: [0]=DG/game, [1]=R2MaxPct, [2]=R2InsPct, [3]=totalGames,
    //          [4]=totalDG, [5]=yardCount, [6]=courtsCount, [7]=prisonGames, [8]=totalResets
    private double[] computeDailyAverages() {
        double totalDG = 0; int totalGames = 0; int totalResets = 0;
        int r2MaxCount = 0, gamesWithMax = 0, r2InsCount = 0, gamesWithIns = 0;
        int yardCount = 0, courtsCount = 0, prisonGames = 0;
        for (String d : sessions) {
            SessionData sd = SessionManager.loadSession(d);
            if (sd == null || sd.games == null) continue;
            for (ZombiesGame g : sd.games) {
                if (g.isReset) {
                    totalResets++;
                    // Count yard/courts and DG/R2 stats from resets too
                    totalDG += g.doubleGoldCount;
                    if (g.maxAmmoPattern != null && !g.maxAmmoPattern.isEmpty()) { gamesWithMax++; if (g.maxAmmoPattern.get(0).equals("r2")) r2MaxCount++; }
                    if (g.instaKillPattern != null && !g.instaKillPattern.isEmpty()) { gamesWithIns++; if (g.instaKillPattern.get(0).equals("r2")) r2InsCount++; }
                    if ("Prison".equals(g.map) && g.keyPart != null) {
                        if (g.keyPart.startsWith("Y")) yardCount++;
                        else if (g.keyPart.startsWith("C")) courtsCount++;
                    }
                    continue;
                }
                totalGames++; totalDG += g.doubleGoldCount;
                if (g.maxAmmoPattern != null && !g.maxAmmoPattern.isEmpty()) { gamesWithMax++; if (g.maxAmmoPattern.get(0).equals("r2")) r2MaxCount++; }
                if (g.instaKillPattern != null && !g.instaKillPattern.isEmpty()) { gamesWithIns++; if (g.instaKillPattern.get(0).equals("r2")) r2InsCount++; }
                if ("Prison".equals(g.map)) {
                    prisonGames++;
                    if (g.keyPart != null) {
                        if (g.keyPart.startsWith("Y")) yardCount++;
                        else if (g.keyPart.startsWith("C")) courtsCount++;
                    }
                }
            }
        }
        return new double[]{
            (totalGames + totalResets) > 0 ? totalDG / (totalGames + totalResets) : 0,
            gamesWithMax > 0 ? (r2MaxCount * 100.0 / gamesWithMax) : 0,
            gamesWithIns > 0 ? (r2InsCount * 100.0 / gamesWithIns) : 0,
            totalGames, totalDG, yardCount, courtsCount, prisonGames, totalResets
        };
    }

    public static Map<String, Integer> getTotalChestCounts(String map) {
        Map<String, Integer> c = new LinkedHashMap<>();
        for (String d : SessionManager.getAllSavedSessions()) {
            SessionData sd = SessionManager.loadSession(d);
            if (sd == null || sd.games == null) continue;
            for (ZombiesGame g : sd.games) {
                if (!map.equals(g.map) || g.startChestLocation == null) continue;
                c.put(g.startChestLocation, c.getOrDefault(g.startChestLocation, 0) + 1);
            }
        }
        // Also include the live ongoing game so stats update immediately
        ZombiesGame live = com.kum.hyperanalyzer.tracking.ZombiesDetector.currentGame;
        if (live != null && live.isOngoing && map.equals(live.map) && live.startChestLocation != null) {
            c.put(live.startChestLocation, c.getOrDefault(live.startChestLocation, 0) + 1);
        }
        return c;
    }

    public static Map<String, Integer> getTotalWeaponCounts(String map) {
        Map<String, Integer> c = new LinkedHashMap<>();
        boolean isOverall = "Overall".equals(map);
        if (!isOverall) {
            for (String w : ChestTracker.getWeaponsForMap(map)) c.put(w, 0);
        } else {
            // Seed with all known weapons
            for (String w : ChestTracker.BB_DE_WEAPONS)  c.put(w, 0);
            for (String w : ChestTracker.PRISON_WEAPONS) c.put(w, 0);
            for (String w : ChestTracker.AA_WEAPONS)     c.put(w, 0);
        }
        for (String d : SessionManager.getAllSavedSessions()) {
            SessionData sd = SessionManager.loadSession(d);
            if (sd == null || sd.games == null) continue;
            for (ZombiesGame g : sd.games) {
                if (!isOverall && !map.equals(g.map)) continue;
                if (g.weaponRolls == null) continue;
                for (Map.Entry<String, Integer> e : g.weaponRolls.entrySet())
                    c.put(e.getKey(), c.getOrDefault(e.getKey(), 0) + e.getValue());
            }
        }
        // Also include the live ongoing game so weapon stats update immediately
        ZombiesGame live = com.kum.hyperanalyzer.tracking.ZombiesDetector.currentGame;
        if (live != null && live.isOngoing && live.weaponRolls != null) {
            if (isOverall || map.equals(live.map)) {
                for (Map.Entry<String, Integer> e : live.weaponRolls.entrySet())
                    c.put(e.getKey(), c.getOrDefault(e.getKey(), 0) + e.getValue());
            }
        }
        // Remove weapons with 0 rolls for cleaner display
        if (isOverall) c.entrySet().removeIf(e -> e.getValue() == 0);
        return c;
    }

    private long getOverallFastestTime(String mapName, String diff) {
        long fastest = Long.MAX_VALUE;
        for (String d : sessions) {
            SessionData sd = SessionManager.loadSession(d);
            if (sd == null || sd.games == null) continue;
            for (ZombiesGame g : sd.games)
                if (g.isWin && matchesMap(g, mapName) && matchesDifficulty(g, diff) && g.duration < fastest) fastest = g.duration;
        }
        return fastest;
    }

    // ---- Static helpers shared by other GUI classes ----
    public static boolean matchesMap(ZombiesGame g, String mapName) {
        if (mapName.equals("Prison Escape")) return g.map.equals("Prison") && g.isEscape;
        if (mapName.equals("Prison"))        return g.map.equals("Prison") && !g.isEscape;
        return g.map.equals(mapName);
    }
    public static boolean matchesDifficulty(ZombiesGame g, String diff) {
        if (g.difficulty == null) return diff.equals("Normal");
        return g.difficulty.equals(diff);
    }
    public static String getDifficultyColor(String diff) {
        if (diff == null) return "\u00a7a";
        switch (diff) { case "Hard": return "\u00a7c"; case "RIP": return "\u00a74"; default: return "\u00a7a"; }
    }
    /** Format milliseconds as h:mm:ss for long durations. */
    public static String formatLongDuration(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        m %= 60; s %= 60;
        if (h > 0) return h + "h " + String.format("%02d", m) + "m " + String.format("%02d", s) + "s";
        if (m > 0) return m + "m " + String.format("%02d", s) + "s";
        return s + "s";
    }

    public static String formatDuration(long ms) {
        if (ms <= 0) return "0s";
        long s = ms / 1000, m = s / 60, h = m / 60;
        m %= 60; s %= 60;
        if (h > 0) return h + "h " + m + "m " + String.format("%02d", s) + "s";
        return m > 0 ? m + "m " + String.format("%02d", s) + "s" : s + "s";
    }
    public static String formatDatePretty(String dateStr) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            return new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(d);
        } catch (Exception e) { return dateStr; }
    }
}
