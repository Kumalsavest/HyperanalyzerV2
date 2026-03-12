package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.ZombiesGame;
import com.kum.hyperanalyzer.tracking.ZombiesDetector;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class GuiGameDetails extends GuiScreen {
    private final GuiScreen parent;
    private final ZombiesGame game;
    private final int gameNumber;
    private final String sessionDate;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private static final int BTN_BACK = 1000;
    private static final int BTN_FAV  = 1001;

    public GuiGameDetails(GuiScreen parent, ZombiesGame game, int gameNumber) {
        this(parent, game, gameNumber, null);
    }

    public GuiGameDetails(GuiScreen parent, ZombiesGame game, int gameNumber, String sessionDate) {
        this.parent      = parent;
        this.game        = game;
        this.gameNumber  = gameNumber;
        this.sessionDate = sessionDate;
    }

    private String favKey() {
        if (sessionDate == null || gameNumber <= 0) return null;
        return sessionDate + ":" + gameNumber;
    }

    private boolean isFavorited() {
        String key = favKey();
        if (key == null) return false;
        return ModConfig.instance.favoriteGames != null && ModConfig.instance.favoriteGames.contains(key);
    }

    @Override
    public void initGui() {
        super.initGui();
        rebuildButtons();
    }

    private void rebuildButtons() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BTN_BACK, this.width / 2 - 100, this.height - 30, 200, 20, "Back"));
        if (favKey() != null) {
            boolean fav = isFavorited();
            String label = fav ? "\u00a7c\u2605 Remove from Favorites\u00a7r" : "\u00a7e\u2605 Add to Favorites\u00a7r";
            this.buttonList.add(new GuiButton(BTN_FAV, this.width / 2 - 100, this.height - 54, 200, 20, label));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_BACK) {
            this.mc.displayGuiScreen(parent);
        } else if (button.id == BTN_FAV) {
            String key = favKey();
            if (key == null) return;
            if (ModConfig.instance.favoriteGames == null)
                ModConfig.instance.favoriteGames = new java.util.ArrayList<>();
            if (isFavorited()) {
                ModConfig.instance.favoriteGames.remove(key);
                game.isFavorite = false;
            } else {
                ModConfig.instance.favoriteGames.add(key);
                game.isFavorite = true;
            }
            ModConfig.save();
            if (sessionDate != null) {
                com.kum.hyperanalyzer.data.SessionData sd = SessionManager.loadSession(sessionDate);
                if (sd != null && sd.games != null && gameNumber >= 1 && gameNumber <= sd.games.size()) {
                    sd.games.get(gameNumber - 1).isFavorite = game.isFavorite;
                    if (SessionManager.currentSession != null && SessionManager.currentSession.date.equals(sessionDate)) {
                        SessionManager.saveCurrentSession();
                    } else {
                        try (java.io.FileWriter fw = new java.io.FileWriter(
                                new java.io.File(SessionManager.sessionDir, sessionDate + ".json"))) {
                            SessionManager.GSON.toJson(sd, fw);
                        } catch (Exception ignored) {}
                    }
                }
            }
            rebuildButtons();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        boolean isLive    = game.isOngoing && ZombiesDetector.isInZombies;
        boolean showChest = !"Alien Arcadium".equals(game.map) && !"The Lab".equals(game.map);
        boolean showWeaps = game.weaponRolls != null && !game.weaponRolls.isEmpty();
        int weapLines     = showWeaps ? game.weaponRolls.size() + 2 : 0;
        int splitCount    = hasPreciseSplits() ? preciseSplitCount() : (game.rounds != null ? game.rounds.size() : 0);
        int rows          = 18 + splitCount + weapLines + (showChest ? 1 : 0);
        maxScroll = Math.max(0, rows * 12 - (this.height - 90));

        String favStar = isFavorited() ? " \u00a7e\u2605\u00a7r" : "";
        // Show custom name if one has been set
        String customName = (ModConfig.instance.favoriteNames != null && favKey() != null)
                ? ModConfig.instance.favoriteNames.get(favKey()) : null;
        String title;
        if (isLive) {
            title = "\u00a7e\u25B6 LIVE GAME\u00a7r";
        } else if (customName != null && !customName.isEmpty()) {
            title = "\u00a7e\u2605 " + customName + "\u00a7r \u00a77(Game " + gameNumber + ")\u00a7r";
        } else {
            title = "Game " + gameNumber + " Details" + favStar;
        }
        this.drawCenteredString(this.fontRendererObj, title, this.width / 2, 10, 0xFFFFFF);

        int y = 28 - scrollOffset;
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");

        String mapColor = "Prison".equals(game.map) ? "\u00a7a" : "\u00a7e";
        drawL(y, "Map: " + mapColor + game.map + "\u00a7r"); y += 12;

        String diffColor = GuiGameSessions.getDifficultyColor(game.difficulty);
        drawL(y, "Difficulty: " + diffColor + (game.difficulty != null ? game.difficulty : "Normal") + "\u00a7r"); y += 12;

        if ("Prison".equals(game.map) && game.keyPart != null) {
            String kpC = "Yard".equals(game.keyPart) ? "\u00a7a" : "Courts".equals(game.keyPart) ? "\u00a7b" : "\u00a77";
            drawL(y, "Key Part: " + kpC + game.keyPart + "\u00a7r"); y += 12;
        }

        if (showChest) {
            String csl = game.startChestLocation != null ? game.startChestLocation : "Not detected";
            String cslC = game.startChestLocation != null ? "\u00a7e" : "\u00a77";
            drawL(y, "Start Chest: " + cslC + csl + "\u00a7r"); y += 12;
        }

        drawL(y, "Started: \u00a77" + sdf.format(new Date(game.startTime)) + "\u00a7r"); y += 12;

        if (isLive) {
            long totalMs = ZombiesDetector.ticksToMs(ZombiesDetector.currentTick() - ZombiesDetector.gameStartTick);
            drawL(y, "Elapsed: \u00a7a" + fmtUs(totalMs * 1000L) + "\u00a7r (Round " + ZombiesDetector.currentRound + ")"); y += 12;
        } else {
            if (game.endTime > 0) { drawL(y, "Ended: \u00a77" + sdf.format(new Date(game.endTime)) + "\u00a7r"); y += 12; }
            drawL(y, "Duration: \u00a7a" + fmtPreciseDuration() + "\u00a7r"); y += 12;
            String outcome = game.isWin ? "\u00a7aWon" : "\u00a7cLost";
            drawL(y, "Outcome: " + outcome + "\u00a7r" + (game.isEscape ? " (Escape)" : "")); y += 12;
        }

        drawL(y, "Double Gold Spawns: \u00a76" + game.doubleGoldCount + "\u00a7r"); y += 12;
        if ("Alien Arcadium".equals(game.map)) { drawL(y, "Bonus Gold Spawns: \u00a7e" + game.bonusGoldCount + "\u00a7r"); y += 12; }

        String ik = game.instaKillPattern != null && !game.instaKillPattern.isEmpty()
                ? "\u00a7c" + String.join(", ", game.instaKillPattern) + "\u00a7r" : "None";
        drawL(y, "\u00a7cInsta Kill\u00a7r Pattern: " + ik); y += 12;

        String ma = game.maxAmmoPattern != null && !game.maxAmmoPattern.isEmpty()
                ? "\u00a79" + String.join(", ", game.maxAmmoPattern) + "\u00a7r" : "None";
        drawL(y, "\u00a79Max Ammo\u00a7r Pattern: " + ma); y += 12;

        if ("Alien Arcadium".equals(game.map) || "The Lab".equals(game.map)) {
            String ss = game.shoppingSpreePattern != null && !game.shoppingSpreePattern.isEmpty()
                    ? "\u00a75" + String.join(", ", game.shoppingSpreePattern) + "\u00a7r" : "None";
            drawL(y, "\u00a75Shopping Spree\u00a7r Pattern: " + ss); y += 12;
        }

        if (showWeaps) {
            y += 6;
            drawL(y, "\u00a7lWeapon Rolls\u00a7r"); y += 12;
            int tot = 0; for (int v : game.weaponRolls.values()) tot += v;
            for (Map.Entry<String, Integer> e : game.weaponRolls.entrySet()) {
                double pct = tot > 0 ? e.getValue() * 100.0 / tot : 0;
                drawL(y, String.format("\u00a76%s\u00a7r: \u00a7a%d\u00a7r (\u00a77%.1f%%\u00a7r)", e.getKey(), e.getValue(), pct));
                y += 12;
            }
        }

        y += 8;
        drawL(y, "\u00a7lRound Splits\u00a7r"); y += 12;

        if (hasPreciseSplits()) {
            int count = preciseSplitCount();
            for (int i = 0; i < count; i++) {
                long segUs = (game.roundsUs != null && i < game.roundsUs.size()) ? game.roundsUs.get(i) : 0;
                long totUs = (game.roundTotalsUs != null && i < game.roundTotalsUs.size()) ? game.roundTotalsUs.get(i) : 0;
                
                // Sanity check: if segment time is bugged (>1hr or <0), calculate it backwards safely
                if (segUs > 3600_000_000L || segUs < 0) {
                    if (i == 0) {
                        segUs = totUs;
                    } else {
                        long prevTotUs = (game.roundTotalsUs != null && i - 1 < game.roundTotalsUs.size()) ? game.roundTotalsUs.get(i - 1) : 0;
                        if (totUs >= prevTotUs) {
                            segUs = totUs - prevTotUs;
                        } else {
                            segUs = 0; // Completely invalid order
                        }
                    }
                }

                String seg = segUs > 0 ? fmtUs(segUs) : "?";
                String tot2 = totUs > 0 ? fmtUs(totUs) : "?";
                String label = (game.escapeSplitIndex != null && i == game.escapeSplitIndex)
                        ? "Escape" : "Round " + (i + 1);
                drawL(y, String.format("%s: \u00a7a%s\u00a7r (Total: \u00a7a%s\u00a7r)", label, seg, tot2)); y += 12;
            }
        } else if (game.rounds != null && !game.rounds.isEmpty()) {
            long cum = 0;
            for (int i = 0; i < game.rounds.size(); i++) {
                long d = game.rounds.get(i); 
                
                // Sanity check for legacy ms mode
                if (d > 3600_000L || d < 0) {
                    if (i == 0) d = cum;
                    else d = 0; // Unrecoverable without totals tracking in legacy mode
                }
                
                cum += d;
                String label = (game.escapeSplitIndex != null && i == game.escapeSplitIndex)
                        ? "Escape" : "Round " + (i + 1);
                drawL(y, String.format("%s: \u00a7a%s\u00a7r (Total: \u00a7a%s\u00a7r)",
                        label, fmtShort(d), fmtShort(cum))); y += 12;
            }
        } else {
            drawL(y, "\u00a77No split data\u00a7r"); y += 12;
        }

        if (isLive && ZombiesDetector.roundStartTick > 0) {
            long tick    = ZombiesDetector.currentTick();
            long roundMs = ZombiesDetector.ticksToMs(tick - ZombiesDetector.roundStartTick);
            long totalMs = ZombiesDetector.ticksToMs(tick - ZombiesDetector.gameStartTick);
            boolean isEscapeRound = game.escapeSplitIndex != null
                    && (ZombiesDetector.currentRound - 1) == game.escapeSplitIndex;
            String liveLabel = isEscapeRound ? "Escape" : "Round " + ZombiesDetector.currentRound;
            drawL(y, String.format("\u00a7e%s: \u00a7a%s\u00a7r (Total: \u00a7a%s\u00a7r) \u00a7e\u25B6\u00a7r",
                    liveLabel, fmtUs(roundMs * 1000L), fmtUs(totalMs * 1000L))); y += 12;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean hasPreciseSplits() {
        return (game.roundsUs != null && !game.roundsUs.isEmpty())
                || (game.roundTotalsUs != null && !game.roundTotalsUs.isEmpty());
    }

    private int preciseSplitCount() {
        int a = game.roundsUs != null ? game.roundsUs.size() : 0;
        int b = game.roundTotalsUs != null ? game.roundTotalsUs.size() : 0;
        return Math.max(a, b);
    }

    /** Format microseconds exactly like ZombiesAutoSplits: M:SS.TT (tenths+hundredths digits). */
    private String fmtUs(long us) {
        long ms      = us / 1000L;
        // Same math as ZombiesDetector.formatSplitMs
        long totalCs = ms / 10;          // in 10ms units
        long tenths  = (totalCs / 10) % 10;  // the tenths digit
        long hundths = totalCs % 10;          // the hundredths digit
        long totSec  = ms / 1000;
        long sec     = totSec % 60;
        long min     = totSec / 60;
        return String.format("%d:%02d.%d%d", min, sec, tenths, hundths);
    }

    private String fmtPreciseDuration() {
        if (game.roundTotalsUs != null && !game.roundTotalsUs.isEmpty()) {
            long last = 0; for (long t : game.roundTotalsUs) if (t > last) last = t;
            if (last > 0) return fmtUs(last);
        }
        return fmtMs(game.duration);
    }

    private String fmtMs(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        if (h > 0) return String.format("%dh %02dm %02ds", h, m % 60, s % 60);
        return String.format("%dm %02ds", m, s % 60);
    }

    private String fmtShort(long ms) {
        long s = ms / 1000, m = s / 60;
        if (m > 0) return m + "m " + String.format("%02d", s % 60) + "s";
        return (s % 60) + "s";
    }

    private void drawL(int y, String text) {
        if (y > 20 && y < this.height - 60)
            this.drawCenteredString(this.fontRendererObj, text, this.width / 2, y, 0xFFFFFF);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dw = Mouse.getEventDWheel();
        if (dw != 0) {
            scrollOffset += (dw > 0 ? -1 : 1) * 15;
            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        }
    }
}
