package com.kum.hyperanalyzer.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZombiesGame {
    public long startTime;
    public long endTime;
    public String map;
    public long duration;
    public boolean isWin;
    public boolean isOngoing;
    public boolean isReset; // true = this was a reset (round 1, ≤15s) — not counted as a real game
    public String difficulty; // "Normal", "Hard", "RIP"
    public boolean isEscape; // Prison Escape
    public int lastRound; // round game ended on
    public String keyPart; // "Yard", "Courts", "Unidentified" (Prison only)
    public String serverId; // unique server instance id from scoreboard (e.g. m61BR)
    public List<Long> rounds;
    public List<String> maxAmmoPattern;
    public List<String> instaKillPattern;
    public List<String> shoppingSpreePattern;
    public int doubleGoldCount;
    public int bonusGoldCount; // Alien Arcadium only

    // Chest tracking
    public String startChestLocation; // chest location detected at round 1
    public String currentChestLocation; // live chest location (not persisted meaningfully)

    // Weapon rolls: weapon name -> count
    public Map<String, Integer> weaponRolls;

    // Precise round splits from ZombiesAutoSplits chat (stored as microseconds for 2-decimal display)
    // Each entry = round duration in microseconds (1/1000 ms).  Using long to avoid float serialization drift.
    // Display: divide by 1000 to get ms, then format as s.cs
    public List<Long> roundsUs; // microseconds per round

    // Cumulative game time at each round end from "Round X finished at M:SS.TT" chat (microseconds)
    public List<Long> roundTotalsUs;

    // Index (0-based) of the escape split in roundsUs/roundTotalsUs (null = no escape split)
    public Integer escapeSplitIndex;

    // Is this game favorited by the player
    public boolean isFavorite;

    public ZombiesGame() {
        this.rounds = new ArrayList<>();
        this.roundsUs = new ArrayList<>();
        this.roundTotalsUs = new ArrayList<>();
        this.maxAmmoPattern = new ArrayList<>();
        this.instaKillPattern = new ArrayList<>();
        this.shoppingSpreePattern = new ArrayList<>();
        this.doubleGoldCount = 0;
        this.bonusGoldCount = 0;
        this.isOngoing = true;
        this.isReset   = false;
        this.difficulty = "Normal";
        this.isEscape = false;
        this.lastRound = 0;
        this.keyPart = null;
        this.startChestLocation = null;
        this.currentChestLocation = null;
        this.weaponRolls = new LinkedHashMap<>();
        this.escapeSplitIndex = null;
        this.isFavorite = false;
    }
}
