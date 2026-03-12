package com.kum.hyperanalyzer.data;

import java.util.ArrayList;
import java.util.List;

public class SessionData {
    public String date; // "yyyy-MM-dd"
    public long totalHypixelPlaytime;
    public long totalZombiesPlaytime;
    public int zombiesGamesTotal;
    public int zombiesGamesWon;
    public int zombiesGamesLost;
    public int zombiesGamesReset; // games that were resets (round 1, ≤15s)
    public List<ZombiesGame> games;

    public SessionData(String date) {
        this.date = date;
        this.totalHypixelPlaytime = 0;
        this.totalZombiesPlaytime = 0;
        this.zombiesGamesTotal = 0;
        this.zombiesGamesWon = 0;
        this.zombiesGamesLost = 0;
        this.zombiesGamesReset = 0;
        this.games = new ArrayList<>();
    }

    /**
     * Returns zombies time computed from completed game durations.
     * More accurate than the tick-based totalZombiesPlaytime because it stops
     * exactly when each game ends rather than relying on tick accumulation.
     */
    public long accurateZombiesPlaytime() {
        if (games == null) return 0;
        long total = 0;
        for (ZombiesGame g : games) {
            if (g.duration > 0) total += g.duration;
        }
        return total;
    }

    /** Count of games that were resets (round 1, ≤15s). */
    public int countResets() {
        if (games == null) return 0;
        int n = 0;
        for (ZombiesGame g : games) if (g.isReset) n++;
        return n;
    }

    /** Count of real played games (not resets). */
    public int countPlayed() {
        if (games == null) return 0;
        int n = 0;
        for (ZombiesGame g : games) if (!g.isReset) n++;
        return n;
    }
}
