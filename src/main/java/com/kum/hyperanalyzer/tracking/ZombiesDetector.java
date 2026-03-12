package com.kum.hyperanalyzer.tracking;

import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.ZombiesGame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.List;

public class ZombiesDetector {
    public static boolean isInZombies = false;
    public static ZombiesGame currentGame = null;
    public static long lastRoundStartTime = 0;
    public static int currentRound = 0;
    private static int difficultyCheckTick = 0;
    private static int keyPartCheckTick = 0;
    // Tracks the Server ID of the last game that was properly ended (Game Over / Win).
    // scanForMidroundRejoin() will skip this ID so it doesn't resurrect a finished game.
    private static String lastEndedServerId = null;

    // A game counts as a "reset" (not a real game) if:
    //   - duration ≤ 10s, OR
    //   - duration ≥ 5m22s (322s) — lobby inflation / AFK
    // Also, if a round-1 game exceeds MAX_ROUND1_PLAY_MS we assume it includes lobby wait
    // time and cap it to RESET_CAP_MS so it doesn't show as a long fake game.
    public static final long RESET_DURATION_MS      = 10_000L;   // ≤10s = reset
    public static final long RESET_LONG_DURATION_MS  = 322_000L;  // ≥5m22s = reset (lobby inflation)
    public static final long MAX_ROUND1_PLAY_MS     = 322_000L;  // 5m22s = max believable round-1 real play
    public static final long RESET_CAP_MS           = 10_000L;   // cap bogus round-1 durations to 10s

    // Tick-based timing — matches ZombiesAutoSplits exactly (world ticks × 50ms)
    // gameStartTick: world tick when game started (or was rejoined at round 1)
    // roundStartTick: world tick when the current round started
    public static long gameStartTick  = 0;
    public static long roundStartTick = 0;

    /** Returns current world tick, or 0 if world unavailable. */
    public static long currentTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) return 0;
        return mc.theWorld.getTotalWorldTime();
    }

    /** Ticks-to-milliseconds: ticks * 50. */
    public static long ticksToMs(long ticks) { return ticks * 50L; }

    /** Format milliseconds as M:SS.TT matching ZombiesAutoSplits formattedTime exactly. */
    public static String formatSplitMs(long ms) {
        // ZombiesAutoSplits: %d:%02d.%d%d  where last two are digit1=tenths, digit2=hundredths
        long totalCs = ms / 10;           // centiseconds
        long tenths  = (totalCs / 10) % 10; // 10ths digit
        long hundths = totalCs % 10;        // 100ths digit (actually 10ms units)
        long totSec  = ms / 1000;
        long sec     = totSec % 60;
        long min     = totSec / 60;
        return String.format("%d:%02d.%d%d", min, sec, tenths, hundths);
    }

    // Track whether we've already ended the game to prevent double-ending
    private static boolean gameEndPending = false;

    public static void onTitle(String title) {
        if (!ServerTracker.isOnHypixel && !ServerTracker.isOnZombiesServer) return;

        String lower = title.toLowerCase();

        if (lower.contains("round") || lower.contains("you made it to")) {
            int round = extractNumber(lower);
            if (round > 0) {
                if (!isInZombies || currentGame == null) {
                    // Not currently in a game — start fresh or rejoin
                    if (round > 1) {
                        rejoinGame(round);
                    } else {
                        startGame();
                    }
                } else if (round == 1 && currentRound > 1) {
                    // Were past round 1 already — this is a brand new game
                    startGame();
                } else if (round > currentRound) {
                    // Normal round progression
                    splitRound(round);
                }
                // round == 1 && currentRound <= 1: already on round 1, ignore duplicate title
            }
        }

        // Win/loss detection — title is primary when chat hasn't fired yet
        boolean isWin  = lower.contains("survived") || lower.contains("you won")
                || lower.contains("victory") || lower.contains("extraction successful")
                || lower.contains("escaped");
        boolean isLoss = lower.contains("game over");
        if ((isWin || isLoss) && isInZombies && currentGame != null && !gameEndPending) {
            if (isWin && (lower.contains("escaped") || lower.contains("escape")))
                currentGame.isEscape = true;
            gameEndPending = true;
            endGame(isWin);
        }
    }

    /**
     * Called with the clean click-event value from ZombiesAutoSplits messages.
     * Format: "Round X took M:SS.TT!" or "Round X finished at M:SS.TT!" (optionally with delta).
     * This is the PRIMARY and most reliable source for split times.
     */
    public static void onSplitChat(String val) {
        if (!isInZombies || currentGame == null) return;
        String clean = val.trim();
        String lower = clean.toLowerCase();

        // "Round X took M:SS.TT!" — segment time for this round
        if (lower.startsWith("round ") && lower.contains(" took ")) {
            try {
                int tookIdx  = lower.indexOf(" took ");
                int roundNum = extractNumber(lower.substring(0, tookIdx));
                String after = clean.substring(tookIdx + 6).trim();
                // time token ends at space or '(' — strip delta suffix
                String timeToken = after.split("[\\s(]")[0].replace("!", "");
                long ms = parseSplitTimeMs(timeToken);
                if (ms > 0 && roundNum > 0) {
                    ensureSize(currentGame.roundsUs, roundNum);
                    // Only write if tick-based data hasn't already filled this slot
                    if (currentGame.roundsUs.get(roundNum - 1) == 0L) {
                        currentGame.roundsUs.set(roundNum - 1, ms * 1000L);
                        SessionManager.saveCurrentSession();
                    }
                }
            } catch (Exception ignored) {}
        }

        // "Round X finished at M:SS.TT!" — cumulative total
        if (lower.startsWith("round ") && lower.contains(" finished at ")) {
            try {
                int finIdx   = lower.indexOf(" finished at ");
                int roundNum = extractNumber(lower.substring(0, finIdx));
                String after = clean.substring(finIdx + 13).trim();
                String timeToken = after.split("[\\s(]")[0].replace("!", "");
                long ms = parseSplitTimeMs(timeToken);
                if (ms > 0 && roundNum > 0) {
                    ensureSize(currentGame.roundTotalsUs, roundNum);
                    // Only write if tick-based data hasn't already filled this slot
                    if (currentGame.roundTotalsUs.get(roundNum - 1) == 0L) {
                        currentGame.roundTotalsUs.set(roundNum - 1, ms * 1000L);
                        SessionManager.saveCurrentSession();
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static void onChat(String message) {
        if (!isInZombies || currentGame == null) return;

        // Scan all lines (getFormattedText preserves \n between siblings).
        // Fallback split detection in case click event wasn't available.
        String[] lines = message.split("\n");
        for (String rawLine : lines) {
            String line  = net.minecraft.util.StringUtils.stripControlCodes(rawLine).trim();
            String lline = line.toLowerCase();
            if (lline.startsWith("round ") && (lline.contains(" took ") || lline.contains(" finished at "))) {
                onSplitChat(line);
            }
        }

        // Win/loss detection on stripped text
        String lower = net.minecraft.util.StringUtils.stripControlCodes(message).toLowerCase();

        if (lower.contains("survived!") && !gameEndPending) {
            parseChatTime(message);
            gameEndPending = true;
            endGame(true);
            return;
        }
        if ((lower.contains("escaped!") || lower.contains("you escaped")) && !gameEndPending) {
            currentGame.isEscape = true;
            parseChatTime(message);
            gameEndPending = true;
            endGame(true);
            return;
        }
        if ((lower.contains("you won!") || lower.contains("victory!")
                || lower.contains("extraction successful!")) && !gameEndPending) {
            parseChatTime(message);
            gameEndPending = true;
            endGame(true);
            return;
        }
        if (lower.contains("game over!") && !gameEndPending) {
            parseChatTime(message);
            gameEndPending = true;
            endGame(false);
            return;
        }
        if (currentGame.map.equals("Prison")
                && (lower.contains("prison escape") || lower.contains("you escaped"))) {
            currentGame.isEscape = true;
        }

        // Helicopter call — start the Escape split (Prison only, 120s phase)
        if (currentGame.map.equals("Prison") && !gameEndPending
                && lower.contains("helicopter is on its way")
                && currentGame.escapeSplitIndex == null) {
            splitEscape();
        }
    }

    /** Called when the helicopter is summoned in Prison Escape.
     *  Closes the current round split and opens a new "Escape" split
     *  that is always exactly 120 seconds long. */
    private static void splitEscape() {
        if (currentGame == null) return;

        long tick = currentTick();
        boolean ticksValid = gameStartTick > 0 && tick >= gameStartTick && tick >= roundStartTick;

        // Close the current round (the last zombie round before escape)
        long roundMs = ticksValid
                ? ticksToMs(tick - roundStartTick)
                : Math.max(0, System.currentTimeMillis() - lastRoundStartTime);
        long totalAtHeli = ticksValid
                ? ticksToMs(tick - gameStartTick)
                : Math.max(0, System.currentTimeMillis() - currentGame.startTime);

        int idx = currentRound - 1;
        ensureSize(currentGame.roundsUs,      idx + 1);
        ensureSize(currentGame.roundTotalsUs, idx + 1);
        ensureSize(currentGame.rounds,        idx + 1);
        currentGame.roundsUs.set(idx,      roundMs * 1000L);
        currentGame.roundTotalsUs.set(idx, totalAtHeli * 1000L);
        currentGame.rounds.set(idx,        roundMs);

        // Advance to the Escape split (next index)
        currentRound++;
        int escIdx = currentRound - 1;
        currentGame.escapeSplitIndex = escIdx;
        currentGame.isEscape = true;

        // Pre-fill the Escape split with 120s segment; total = totalAtHeli + 120000
        long escapeMs    = 120_000L;
        long escapeTotMs = totalAtHeli + escapeMs;
        ensureSize(currentGame.roundsUs,      escIdx + 1);
        ensureSize(currentGame.roundTotalsUs, escIdx + 1);
        ensureSize(currentGame.rounds,        escIdx + 1);
        currentGame.roundsUs.set(escIdx,      escapeMs * 1000L);
        currentGame.roundTotalsUs.set(escIdx, escapeTotMs * 1000L);
        currentGame.rounds.set(escIdx,        escapeMs);

        roundStartTick     = tick;
        lastRoundStartTime = System.currentTimeMillis();
        SessionManager.saveCurrentSession();
    }

    /**
     * Parse ZombiesAutoSplits time format "M:SS.TT" → milliseconds.
     * formattedTime() in ZombiesAutoSplits uses: time *= 50 (ticks→ms), then
     *   %d:%02d.%d%d  where the last TWO chars are individual digits:
     *   digit1 = tenths of a second  (0-9)
     *   digit2 = hundredths of a second (0-9)
     * So "0:36.55" = 36 seconds + 5 tenths + 5 hundredths = 36 + 0.55 s = 36550 ms.
     * Returns milliseconds as a long.
     */
    private static long parseSplitTimeMs(String s) {
        try {
            s = s.trim();
            if (!s.contains(":") || !s.contains(".")) return 0;
            int colonIdx = s.indexOf(':');
            int dotIdx   = s.indexOf('.');
            long minutes = Long.parseLong(s.substring(0, colonIdx).trim());
            long seconds = Long.parseLong(s.substring(colonIdx + 1, dotIdx).trim());
            String fracStr = s.substring(dotIdx + 1).trim();
            // Take exactly 2 characters: digit1=tenths, digit2=hundredths
            if (fracStr.length() > 2) fracStr = fracStr.substring(0, 2);
            while (fracStr.length() < 2) fracStr += "0";
            long tenths     = fracStr.charAt(0) - '0';  // 0-9
            long hundredths = fracStr.charAt(1) - '0';  // 0-9
            long fracMs = tenths * 100L + hundredths * 10L;
            return (minutes * 60L + seconds) * 1000L + fracMs;
        } catch (Exception ignored) {}
        return 0;
    }

    /** Ensure list has at least `size` entries (pad with 0L). */
    private static void ensureSize(java.util.List<Long> list, int size) {
        while (list.size() < size) list.add(0L);
    }

    /**
     * Parse game time from chat. Only overwrites duration if not already set by title.
     * Hypixel sends "Zombies - mm:ss" or "Zombies - hh:mm:ss" in chat at game end.
     */
    private static void parseChatTime(String message) {
        if (currentGame == null) return;

        // "Zombies - mm:ss" or "Zombies - hh:mm:ss"
        if (message.contains("Zombies -") || message.contains("Zombies-")) {
            int idx = message.indexOf('-');
            if (idx != -1) {
                String after = message.substring(idx + 1).trim();
                long ms = parseColonTime(after);
                if (ms <= 0) ms = parseTimeToMs(after);
                if (ms > 0) { currentGame.duration = ms; return; }
            }
        }

        // "Time: mm:ss" or "Total Time: mm:ss"
        if (message.contains("Time:")) {
            String after = message.substring(message.indexOf("Time:") + 5).trim();
            long ms = parseColonTime(after);
            if (ms <= 0) ms = parseTimeToMs(after);
            if (ms > 0) { currentGame.duration = ms; return; }
        }

        // Last resort — try bare colon or text format
        long ms = parseColonTime(message.trim());
        if (ms <= 0) ms = parseTimeToMs(message.trim());
        if (ms > 0) currentGame.duration = ms;
    }

    /** Parse "hh:mm:ss" or "mm:ss" colon-separated time string. */
    private static long parseColonTime(String s) {
        try {
            String t = s.trim().split("\\s")[0]; // take first token only
            String[] parts = t.split(":");
            if (parts.length == 3) {
                long h = Long.parseLong(parts[0].replaceAll("\\D", ""));
                long m = Long.parseLong(parts[1].replaceAll("\\D", ""));
                long sec = Long.parseLong(parts[2].replaceAll("\\D", ""));
                return (h * 3600L + m * 60L + sec) * 1000L;
            } else if (parts.length == 2) {
                long m = Long.parseLong(parts[0].replaceAll("\\D", ""));
                long sec = Long.parseLong(parts[1].replaceAll("\\D", ""));
                return (m * 60L + sec) * 1000L;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public static void tickDifficultyCheck() {
        if (!isInZombies || currentGame == null) return;

        // If world wasn't ready when startGame() ran, gameStartTick==0.
        // Capture it now on the first available tick so timing is correct.
        if (gameStartTick == 0) {
            long t = currentTick();
            if (t > 0) { gameStartTick = t; roundStartTick = t; }
        }

        difficultyCheckTick++;
        if (difficultyCheckTick >= 40) {
            difficultyCheckTick = 0;
            updateDifficulty();
        }
        if (currentGame.map.equals("Prison")
                && (currentGame.keyPart == null || currentGame.keyPart.equals("Unidentified"))) {
            keyPartCheckTick++;
            if (keyPartCheckTick >= 20) {
                keyPartCheckTick = 0;
                checkKeyPart();
            }
        }
        
        if (currentGame.serverId == null) {
            checkServerId();
        }
        
        updateGameTime();
        checkLeave();
    }
    
    private static void checkServerId() {
        for (String line : ScoreboardManager.content) {
            String[] parts = line.trim().split("\\s+");
            
            if (parts.length >= 2) {
                String possibleDate = parts[0];
                String potentialId = parts[parts.length - 1];
                
                // Hypixel attaches invisible unicode padding (\u200e, etc.) to the ends of lines.
                // We MUST physically strip all non-alphanumeric characters before regex matching.
                potentialId = potentialId.replaceAll("[^a-zA-Z0-9]", "");
                
                if ((possibleDate.contains("/") || possibleDate.contains("-")) && potentialId.matches("^[mM][a-zA-Z0-9]{2,7}$")) {
                    currentGame.serverId = potentialId;
                    SessionManager.saveCurrentSession();
                    return;
                }
            }
        }
    }

    private static long lastScoreboardTimeMs = 0;

    private static void updateGameTime() {
        if (!isInZombies || currentGame == null) return;
        List<String> raw = ScoreboardManager.rawContent;
        for (String line : raw) {
            String stripped = net.minecraft.util.StringUtils.stripControlCodes(line).trim();
            if (stripped.startsWith("Time:") || stripped.contains("时间:") || stripped.contains("時間:")) {
                int colonIdx = stripped.indexOf(":");
                if (colonIdx == -1) colonIdx = stripped.indexOf("：");
                if (colonIdx != -1) {
                    String timeStr = stripped.substring(colonIdx + 1).trim();
                    long ms = parseShortTimeToMs(timeStr);
                    if (ms > 0) lastScoreboardTimeMs = ms;
                }
                break;
            }
        }
    }

    private static long parseShortTimeToMs(String timeStr) {
        // "12:34"
        try {
            String[] parts = timeStr.split(":");
            if (parts.length == 2) {
                int m = Integer.parseInt(parts[0].replaceAll("\\D", ""));
                int s = Integer.parseInt(parts[1].replaceAll("\\D", ""));
                return (m * 60L + s) * 1000L;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private static long parseTimeToMs(String timeStr) {
        // "12m 34s" or "12m34s"
        try {
            long totalMs = 0;
            String t = timeStr.trim();
            if (t.contains("m")) {
                String mStr = t.substring(0, t.indexOf("m")).replaceAll("\\D", "").trim();
                if (!mStr.isEmpty()) totalMs += Long.parseLong(mStr) * 60000L;
                t = t.substring(t.indexOf("m") + 1).trim();
            }
            if (t.contains("s")) {
                String sStr = t.substring(0, t.indexOf("s")).replaceAll("\\D", "").trim();
                if (!sStr.isEmpty()) totalMs += Long.parseLong(sStr) * 1000L;
            }
            return totalMs;
        } catch (Exception ignored) {}
        return 0;
    }

    // ---- Rejoin / Leave detection ----
    // checkLeave handles the /leave-while-connected case (scoreboard disappears while on Hypixel).
    // Hard disconnects are handled immediately in ServerTracker.onClientDisconnect via onDisconnect().
    private static int leaveTicks = 0;
    private static int leaveGraceTicks = 0;
    private static final int LEAVE_THRESHOLD_TICKS = 100; // ~5 sec of missing scoreboard = left
    private static final int LEAVE_GRACE_TICKS = 120;     // ~6 sec grace after game start

    private static void checkLeave() {
        if (!isInZombies || currentGame == null) { leaveTicks = 0; return; }
        // Only fires while still connected — hard disconnect is handled by onDisconnect()
        if (!ServerTracker.isOnHypixel && !ServerTracker.isOnZombiesServer) { leaveTicks = 0; return; }
        // Grace period after game start so scoreboard has time to populate
        if (leaveGraceTicks > 0) { leaveGraceTicks--; leaveTicks = 0; return; }

        boolean foundMarker = false;
        List<String> lines = ScoreboardManager.rawContent;
        String boardTitle = ScoreboardManager.title.toLowerCase();
        
        if (currentGame.serverId != null && !ScoreboardManager.content.isEmpty()) {
            String firstLine = ScoreboardManager.content.get(0);
            String[] parts = firstLine.split("\\s+");
            if (parts.length >= 2) {
                String liveId = parts[parts.length - 1];
                if (currentGame.serverId.equals(liveId)) {
                    foundMarker = true;
                }
            }
        }

        if (!foundMarker) {
            if (boardTitle.contains("zombies")) {
                foundMarker = true;
            } else {
                for (String line : lines) {
                    String s = net.minecraft.util.StringUtils.stripControlCodes(line).toLowerCase();
                    if (s.contains("zombies left") || s.contains("round") || s.contains("kills")
                            || s.contains("gold") || s.contains("time") || s.contains("zombie")) {
                        foundMarker = true;
                        break;
                    }
                }
            }
        }

        if (!foundMarker) {
            leaveTicks++;
            if (leaveTicks >= LEAVE_THRESHOLD_TICKS) {
                onDisconnect();
                leaveTicks = 0;
            }
        } else {
            leaveTicks = 0;
        }
    }

    public static void scanForMidroundRejoin() {
        if (isInZombies) return; // already tracking formally
        if (SessionManager.currentSession == null || SessionManager.currentSession.games == null) return;
        
        String currentServerId = null;
        int detectedRound = 0;
        
        for (String line : ScoreboardManager.content) {
            String clean = net.minecraft.util.StringUtils.stripControlCodes(line).trim();
            String lower = clean.toLowerCase();
            
            if (lower.startsWith("round ")) {
                detectedRound = extractNumber(lower);
            }
            
            String[] parts = clean.split("\\s+");
            if (parts.length >= 2) {
                String possibleDate = parts[0];
                String potentialId = parts[parts.length - 1].replaceAll("[^a-zA-Z0-9]", "");
                if ((possibleDate.contains("/") || possibleDate.contains("-")) && potentialId.matches("^[mM][a-zA-Z0-9]{2,7}$")) {
                    currentServerId = potentialId;
                }
            }
        }
        
        if (currentServerId != null && detectedRound > 0) {
            // Don't resurrect a game that was properly ended (Game Over / Win)
            if (currentServerId.equals(lastEndedServerId)) return;
            
            for (ZombiesGame g : SessionManager.currentSession.games) {
                // ONLY resurrect games still marked as ongoing (from a disconnect in THIS session).
                // Games ended by startup cleanup or Game Over/Win are final.
                if (g.isOngoing && currentServerId.equals(g.serverId)) {
                    rejoinGame(detectedRound);
                    return;
                }
            }
        }
    }

    /** Called on hard disconnect or /leave — save the game as ongoing so it can
     *  be resurrected within the same Minecraft session via scanForMidroundRejoin().
     *  If the player never rejoins (or restarts MC), loadCurrentSession() cleanup
     *  will properly end it on next startup. */
    public static void onDisconnect() {
        if (isInZombies && currentGame != null) {
            currentGame.isOngoing = true;
            if (SessionManager.currentSession != null) {
                if (SessionManager.currentSession.games == null)
                    SessionManager.currentSession.games = new java.util.ArrayList<>();
                SessionManager.currentSession.games.add(currentGame);
            }
            SessionManager.saveCurrentSession();
            PowerupTracker.onGameReset();
            isInZombies = false;
            currentGame = null;
            currentRound = 0;
        }
        leaveTicks = 0;
        gameEndPending = false;
    }

    /** Called when the player reconnects to Hypixel. Resets leave counter so a
     *  brief scoreboard flicker on reconnect doesn't trigger a false /leave detection. */
    public static void onReconnect() {
        leaveTicks = 0;
        leaveGraceTicks = LEAVE_GRACE_TICKS;
        gameEndPending = false;
        // currentGame stays alive — rejoinGame() will restore it when round > 1 title fires
    }

    private static void checkKeyPart() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;
        if (hasItemAt(mc, 52, 72, 49)) {
            if (!"Yard".equals(currentGame.keyPart)) {
                currentGame.keyPart = "Yard";
                AutoReset.onKeyPartDetected("Yard");
            }
            return;
        }
        if (hasItemAt(mc, 126, 74, 71)) {
            if (!"Courts".equals(currentGame.keyPart)) {
                currentGame.keyPart = "Courts";
                AutoReset.onKeyPartDetected("Courts");
            }
            return;
        }
        if (hasItemAt(mc, 110, 74, 71)) {
            if (!"Courts".equals(currentGame.keyPart)) {
                currentGame.keyPart = "Courts";
                AutoReset.onKeyPartDetected("Courts");
            }
            return;
        }
        if (currentGame.keyPart == null) currentGame.keyPart = "Unidentified";
    }

    private static boolean hasItemAt(Minecraft mc, double x, double y, double z) {
        try {
            // Use a generous box — the key part item floats and bobs slightly
            net.minecraft.util.AxisAlignedBB box =
                    new net.minecraft.util.AxisAlignedBB(x - 2, y - 2, z - 2, x + 2, y + 3, z + 2);
            List<net.minecraft.entity.item.EntityItem> items = mc.theWorld.getEntitiesWithinAABB(
                    net.minecraft.entity.item.EntityItem.class, box);
            return !items.isEmpty();
        } catch (Exception e) { return false; }
    }

    private static void updateDifficulty() {
        List<String> raw = ScoreboardManager.rawContent;
        for (String line : raw) {
            String stripped = net.minecraft.util.StringUtils.stripControlCodes(line).trim();
            if (stripped.startsWith("Difficulty") || stripped.startsWith("难易度")
                    || stripped.startsWith("難易度")) {
                int colonIdx = line.indexOf(":");
                if (colonIdx == -1) colonIdx = line.indexOf("：");
                if (colonIdx != -1) {
                    String afterColon = line.substring(colonIdx + 1);
                    if (afterColon.contains("\u00a74")) currentGame.difficulty = "RIP";
                    else if (afterColon.contains("\u00a7c")) currentGame.difficulty = "Hard";
                    else currentGame.difficulty = "Normal";
                }
                break;
            }
        }
    }

    private static void startGame() {
        // Intercept Round 1 Duplication: Before starting a new game, check if this Server ID already maps to a session.
        String currentServerId = null;
        for (String line : ScoreboardManager.content) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                String possibleDate = parts[0];
                String potentialId = parts[parts.length - 1];
                // Clean invisible formatting 
                potentialId = potentialId.replaceAll("[^a-zA-Z0-9]", "");
                
                if ((possibleDate.contains("/") || possibleDate.contains("-")) && potentialId.matches("^[mM][a-zA-Z0-9]{2,7}$")) {
                    currentServerId = potentialId;
                    break;
                }
            }
        }

        if (currentServerId != null && SessionManager.currentSession != null && SessionManager.currentSession.games != null) {
            for (ZombiesGame g : SessionManager.currentSession.games) {
                if (currentServerId.equals(g.serverId)) {
                    // Match found! Even if we think we are 'starting' a Round 1 game, this ID exists.
                    // Funnel this back into the Rejoin override framework.
                    rejoinGame(1);
                    return;
                }
            }
        }

        // Don't double-count a game that was already saved while mid-flight
        if (currentGame != null && isInZombies) {
            currentGame.isOngoing = false;
            currentGame.endTime   = System.currentTimeMillis();
            long nowTick = currentTick();
            long tickMs  = (gameStartTick > 0 && nowTick >= gameStartTick)
                    ? ticksToMs(nowTick - gameStartTick) : 0;
            long wallMs  = currentGame.endTime - currentGame.startTime;

            boolean wasRound1 = (currentRound <= 1);
            // For round-1 closeouts cap tightly; for multi-round use 10 min
            final long MAX_RESET_MS  = 10 * 60 * 1000L;
            final long MAX_ROUND1_MS = 90 * 1000L;
            long cap = wasRound1 ? MAX_ROUND1_MS : MAX_RESET_MS;

            long chosenMs;
            if (tickMs > 0 && tickMs <= cap) {
                chosenMs = tickMs;
            } else if (wallMs > 0 && wallMs <= cap) {
                chosenMs = wallMs;
            } else {
                chosenMs = 0;
            }

            // If still on round 1 and duration > 5m30s, it's lobby wait inflation — cap to 10s
            if (wasRound1 && chosenMs > MAX_ROUND1_PLAY_MS) {
                chosenMs = RESET_CAP_MS;
            }

            currentGame.duration  = chosenMs;
            currentGame.lastRound = currentRound;

            // Mark as reset if ≤10s or ≥5m22s
            boolean isReset = (chosenMs <= RESET_DURATION_MS) || (chosenMs >= RESET_LONG_DURATION_MS);
            currentGame.isReset = isReset;

            if (SessionManager.currentSession != null) {
                if (SessionManager.currentSession.games == null)
                    SessionManager.currentSession.games = new java.util.ArrayList<>();
                SessionManager.currentSession.games.add(currentGame);
                if (isReset) {
                    SessionManager.currentSession.zombiesGamesReset++;
                } else {
                    SessionManager.currentSession.zombiesGamesTotal++;
                }
            }
        }

        // Close out any orphaned ongoing games (disconnect without rejoin) as losses
        if (SessionManager.currentSession != null && SessionManager.currentSession.games != null) {
            java.util.Iterator<ZombiesGame> it = SessionManager.currentSession.games.iterator();
            while (it.hasNext()) {
                ZombiesGame g = it.next();
                if (g.isOngoing) {
                    g.isOngoing = false;
                    g.isWin     = false;
                    if (g.endTime <= 0) g.endTime = System.currentTimeMillis();
                    if (g.duration <= 0) {
                        long d = g.endTime - g.startTime;
                        // Cap at 10 minutes — orphaned games are always losses/disconnects,
                        // never legitimately long tracked games.
                        final long MAX_ORPHAN_MS = 10 * 60 * 1000L;
                        g.duration = (d > 0 && d <= MAX_ORPHAN_MS) ? d : 0;
                    }
                    if (g.lastRound <= 0) g.lastRound = g.rounds != null ? g.rounds.size() : 1;
                    SessionManager.currentSession.zombiesGamesTotal++;
                    SessionManager.currentSession.zombiesGamesLost++;
                }
            }
        }

        isInZombies    = true;
        gameEndPending = false;
        lastEndedServerId = null; // Clear blacklist for the new game
        currentGame    = new ZombiesGame();
        currentGame.startTime  = System.currentTimeMillis();
        currentGame.map        = detectMap();
        currentGame.isOngoing  = true;
        currentGame.difficulty = "Normal";
        currentGame.keyPart    = currentGame.map.equals("Prison") ? "Unidentified" : null;

        lastScoreboardTimeMs = 0;  // discard stale scoreboard reading from prev game
        lastRoundStartTime  = System.currentTimeMillis();
        AutoReset.onGameStart();
        PowerupTracker.onGameReset();
        currentRound         = 1;
        difficultyCheckTick  = 0;
        keyPartCheckTick     = 0;
        leaveTicks           = 0;
        leaveGraceTicks      = LEAVE_GRACE_TICKS;
        long startTick = currentTick();
        // Only use tick-based timing if world tick is available and non-zero.
        // If it's 0 the world isn't ready and we'd end up measuring from world epoch.
        gameStartTick  = startTick > 0 ? startTick : 0;
        roundStartTick = gameStartTick;
        SessionManager.saveCurrentSession();
    }

    /**
     * Called when we detect a round title > 1 but have no current game.
     * This means the player rejoined a game already in progress.
     * We resume tracking without ending/counting the previous game as a loss.
     */
    private static void rejoinGame(int round) {
        ZombiesGame rejoined = null;
        
        // Try to identify the live server ID from the scoreboard
        String currentServerId = null;
        for (String line : ScoreboardManager.content) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 2) {
                String possibleDate = parts[0];
                String potentialId = parts[parts.length - 1];
                // Clean invisible formatting
                potentialId = potentialId.replaceAll("[^a-zA-Z0-9]", "");
                
                if ((possibleDate.contains("/") || possibleDate.contains("-")) && potentialId.matches("^[mM][a-zA-Z0-9]{2,7}$")) {
                    currentServerId = potentialId;
                    break;
                }
            }
        }

        if (SessionManager.currentSession != null && SessionManager.currentSession.games != null) {
            List<ZombiesGame> games = SessionManager.currentSession.games;
            
            // 1. Search for exact serverId match (even if it was already marked as lost/finished!)
            if (currentServerId != null) {
                for (int i = games.size() - 1; i >= 0; i--) {
                    ZombiesGame g = games.get(i);
                    if (currentServerId.equals(g.serverId)) {
                        rejoined = g;
                        // Undo the "loss" or "reset" marking if it was orphaned previously
                        if (!rejoined.isOngoing && !rejoined.isWin) {
                            if (rejoined.isReset) {
                                SessionManager.currentSession.zombiesGamesReset = Math.max(0, SessionManager.currentSession.zombiesGamesReset - 1);
                            } else {
                                SessionManager.currentSession.zombiesGamesLost = Math.max(0, SessionManager.currentSession.zombiesGamesLost - 1);
                                SessionManager.currentSession.zombiesGamesTotal = Math.max(0, SessionManager.currentSession.zombiesGamesTotal - 1);
                            }
                        }
                        games.remove(i);
                        break;
                    }
                }
            }
            
            // 2. Fallback: Search for the last general ongoing game
            if (rejoined == null) {
                for (int i = games.size() - 1; i >= 0; i--) {
                    if (games.get(i).isOngoing) {
                        rejoined = games.get(i);
                        games.remove(i); // pull back out so endGame re-adds it
                        break;
                    }
                }
            }
        }

        isInZombies    = true;
        gameEndPending = false;
        leaveTicks     = 0;
        leaveGraceTicks = LEAVE_GRACE_TICKS;

        if (rejoined != null) {
            // Continue the existing game
            currentGame = rejoined;
            currentGame.isOngoing = true;
            currentGame.isWin = false; // Just to be safe
            currentGame.isReset = false; // We are rejoining it, so it's not a reset anymore
        } else {
            // No saved ongoing game — start fresh but at the current round
            currentGame            = new ZombiesGame();
            currentGame.startTime  = System.currentTimeMillis();
            currentGame.map        = detectMap();
            currentGame.isOngoing  = true;
            currentGame.difficulty = "Normal";
            currentGame.keyPart    = currentGame.map.equals("Prison") ? "Unidentified" : null;
        }

        lastScoreboardTimeMs = 0;  // discard stale value
        currentRound         = round;
        lastRoundStartTime   = System.currentTimeMillis();
        difficultyCheckTick  = 0;
        keyPartCheckTick     = 0;
        roundStartTick       = currentTick();
        // We don't know the exact game start tick after a rejoin, approximate from scoreboard
        gameStartTick        = currentTick();
        SessionManager.saveCurrentSession();
    }

    private static void splitRound(int nextRound) {
        if (currentGame == null) return;

        long tick = currentTick();
        boolean ticksValid = gameStartTick > 0 && tick >= gameStartTick && tick >= roundStartTick;
        long roundMs = ticksValid
                ? ticksToMs(tick - roundStartTick)
                : Math.max(0, System.currentTimeMillis() - lastRoundStartTime);
        long totalMs = ticksValid
                ? ticksToMs(tick - gameStartTick)
                : Math.max(0, System.currentTimeMillis() - currentGame.startTime);

        // Store precise tick-based times in roundsUs (as µs = ms*1000 for 2-decimal display)
        int idx = currentRound - 1; // 0-based index for the round that just ended
        ensureSize(currentGame.roundsUs, idx + 1);
        ensureSize(currentGame.roundTotalsUs, idx + 1);
        currentGame.roundsUs.set(idx, roundMs * 1000L);
        currentGame.roundTotalsUs.set(idx, totalMs * 1000L);

        // Also keep legacy ms-based rounds list for backwards compat
        ensureSize(currentGame.rounds, idx + 1);
        currentGame.rounds.set(idx, roundMs);

        roundStartTick     = tick;
        lastRoundStartTime = System.currentTimeMillis();
        currentRound       = nextRound;

        // Notify PowerupTracker when round 2 begins so it starts tracking powerup mobs
        if (nextRound == 2) {
            PowerupTracker.onRound2Start();
        } else if (nextRound == 3) {
            PowerupTracker.onRound3Start();
        }
    }

    private static void endGame(boolean isWin) {
        if (currentGame == null) return;

        long tick    = currentTick();

        // Validate tick-based timing. gameStartTick==0 means world wasn't ready when game
        // started — in that case tick math would measure from world epoch (huge wrong number).
        // roundStartTick==0 has the same issue for segment times.
        boolean ticksValid = gameStartTick > 0 && tick >= gameStartTick
                && tick >= roundStartTick;

        long roundMs = (ticksValid && roundStartTick > 0)
                ? ticksToMs(tick - roundStartTick)
                : Math.max(0, System.currentTimeMillis() - lastRoundStartTime);
        long totalMs;
        if (ticksValid) {
            totalMs = ticksToMs(tick - gameStartTick);
        } else {
            // Fall back to wall-clock: endTime - startTime
            long wallMs = System.currentTimeMillis() - currentGame.startTime;
            totalMs = Math.max(0, wallMs);
        }
        // Final sanity cap: if totalMs is still unreasonable (>3h), use wall-clock
        if (totalMs > 3 * 3600 * 1000L || totalMs < 0) {
            totalMs = Math.max(0, System.currentTimeMillis() - currentGame.startTime);
        }

        // Final round split
        int idx = currentRound - 1;
        ensureSize(currentGame.roundsUs, idx + 1);
        ensureSize(currentGame.roundTotalsUs, idx + 1);
        currentGame.roundsUs.set(idx, roundMs * 1000L);
        currentGame.roundTotalsUs.set(idx, totalMs * 1000L);
        ensureSize(currentGame.rounds, idx + 1);
        currentGame.rounds.set(idx, roundMs);

        updateGameTime();
        currentGame.duration = totalMs;

        currentGame.endTime   = System.currentTimeMillis();
        currentGame.isWin     = isWin;
        currentGame.isOngoing = false;
        currentGame.lastRound = currentRound;

        // If round 1 and duration > 5m30s, cap to 10s (lobby inflation)
        if (currentRound <= 1 && currentGame.duration > MAX_ROUND1_PLAY_MS) {
            currentGame.duration = RESET_CAP_MS;
        }

        // Mark as reset if ≤10s or ≥5m22s
        boolean isResetGame = (currentGame.duration <= RESET_DURATION_MS) || (currentGame.duration >= RESET_LONG_DURATION_MS);
        currentGame.isReset = isResetGame;

        if (SessionManager.currentSession != null) {
            if (SessionManager.currentSession.games == null)
                SessionManager.currentSession.games = new java.util.ArrayList<>();
            SessionManager.currentSession.games.add(currentGame);
            if (isResetGame) {
                SessionManager.currentSession.zombiesGamesReset++;
            } else {
                SessionManager.currentSession.zombiesGamesTotal++;
                if (isWin) SessionManager.currentSession.zombiesGamesWon++;
                else       SessionManager.currentSession.zombiesGamesLost++;
            }
        }
        SessionManager.saveCurrentSession();

        PowerupTracker.onGameReset();
        // Blacklist this server ID so scanForMidroundRejoin won't resurrect it
        if (currentGame != null && currentGame.serverId != null) {
            lastEndedServerId = currentGame.serverId;
        }
        isInZombies    = false;
        currentGame    = null;
        currentRound   = 0;
        gameEndPending = false;
        leaveTicks     = 0;
    }

    private static String detectMap() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return "Unknown";
        BlockPos pos = new BlockPos(0, 72, 12);
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block block = state.getBlock();
        String blockName = block.getUnlocalizedName();
        switch (blockName) {
            case "tile.air":                return "The Lab";
            case "tile.woolCarpet":         return "Alien Arcadium";
            case "tile.stonebricksmooth":   return "Bad Blood";
            case "tile.cloth":              return "Dead End";
            case "tile.clayHardenedStained":return "Prison";
        }
        return "Unknown";
    }

    private static int extractNumber(String s) {
        String num = s.replaceAll("\\D+", "");
        if (num.isEmpty()) return 0;
        try { return Integer.parseInt(num); } catch (Exception e) { return 0; }
    }
}
