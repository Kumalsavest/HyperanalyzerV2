package com.kum.hyperanalyzer.commands;

import com.kum.hyperanalyzer.data.SessionManager;
import com.kum.hyperanalyzer.data.SessionData;
import com.kum.hyperanalyzer.data.ZombiesGame;
import com.kum.hyperanalyzer.gui.GuiGameSessions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

import java.text.SimpleDateFormat;
import java.util.*;

public class SessionStatsCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "sessionstats";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sessionstats";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        SessionData session = SessionManager.currentSession;
        if (session == null || session.games == null || session.games.isEmpty()) {
            sender.addChatMessage(new ChatComponentText("\u00a7c[HyperAnalyzer] No games in current session."));
            return;
        }

        StringBuilder copyText = new StringBuilder();
        SimpleDateFormat timeFmt = new SimpleDateFormat("m:ss");

        sender.addChatMessage(new ChatComponentText("\u00a76\u00a7l--- Session Stats ---\u00a7r"));
        copyText.append("--- Session Stats ---\n");

        for (int i = 0; i < session.games.size(); i++) {
            ZombiesGame g = session.games.get(i);
            if (g.isReset) continue;

            String timeStr = timeFmt.format(new Date(g.duration));
            String keyStr = "";
            if (g.map.equals("Prison") && g.keyPart != null) {
                keyStr = " (" + g.keyPart.substring(0, 1) + ")";
            }
            String outcome = g.isWin ? "Win" : "Loss";
            String escapeTag = g.isEscape ? " Escape" : "";
            String roundStr = "R" + g.lastRound;

            String line = String.format("Game %d: %s %s%s %ddg %s %s%s",
                    i + 1, g.map, timeStr, keyStr, g.doubleGoldCount, outcome, roundStr, escapeTag);

            sender.addChatMessage(new ChatComponentText("\u00a7e" + line));
            copyText.append(line).append("\n");
        }

        int totalDgs = 0;
        int yardCount = 0;
        int courtsCount = 0;
        for (ZombiesGame g : session.games) {
            totalDgs += g.doubleGoldCount;
            if (g.keyPart != null) {
                if (g.keyPart.startsWith("Y"))
                    yardCount++;
                else if (g.keyPart.startsWith("C"))
                    courtsCount++;
            }
        }
        double wr = session.zombiesGamesTotal > 0 ? (session.zombiesGamesWon * 100.0 / session.zombiesGamesTotal) : 0;

        // Playtime lines
        long zombiesMs = session.accurateZombiesPlaytime();
        long hypixelMs = session.totalHypixelPlaytime;
        String zombiesTime = formatTime(zombiesMs);
        String hypixelTime = formatTime(hypixelMs);
        String timeLine = "Zombies Time: " + zombiesTime + " | Hypixel Time: " + hypixelTime;
        sender.addChatMessage(new ChatComponentText("\u00a7b" + timeLine));
        copyText.append(timeLine).append("\n");

        String summary = String.format("Total Games: %d | W: %d L: %d | WR: %.0f%% | Total DGs: %d",
                session.zombiesGamesTotal, session.zombiesGamesWon, session.zombiesGamesLost, wr, totalDgs);
        String ycLine = String.format("Yard/Courts: %d/%d", yardCount, courtsCount);

        sender.addChatMessage(new ChatComponentText("\u00a7a" + ycLine));
        sender.addChatMessage(new ChatComponentText("\u00a7a" + summary));
        copyText.append(ycLine).append("\n");
        copyText.append(summary).append("\n");

        // Fastest times on maps played
        Set<String> mapsPlayed = new HashSet<>();
        for (ZombiesGame g : session.games) {
            if (g.isReset) continue;
            String mapKey = g.map;
            if (g.isEscape)
                mapKey = "Prison Escape";
            mapsPlayed.add(mapKey);
        }

        boolean hasFastest = false;
        for (String mapName : mapsPlayed) {
            long fastest = Long.MAX_VALUE;
            for (ZombiesGame g : session.games) {
                if (g.isReset)
                    continue;
                if (!g.isWin)
                    continue;
                if (!GuiGameSessions.matchesMap(g, mapName))
                    continue;
                if (g.duration < fastest)
                    fastest = g.duration;
            }
            if (fastest != Long.MAX_VALUE) {
                if (!hasFastest) {
                    sender.addChatMessage(new ChatComponentText("\u00a7b--- Fastest Times ---"));
                    copyText.append("--- Fastest Times ---\n");
                    hasFastest = true;
                }
                String ft = GuiGameSessions.formatDuration(fastest);
                String ftLine = mapName + ": " + ft;
                sender.addChatMessage(new ChatComponentText("\u00a7a" + ftLine));
                copyText.append(ftLine).append("\n");
            }
        }

        // Copy button
        String fullText = copyText.toString();
        ChatComponentText copyButton = new ChatComponentText("\u00a7e\u00a7l[Click to Copy]\u00a7r");
        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hacopy " + fullText.hashCode()));
        style.setChatHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to copy stats")));
        copyButton.setChatStyle(style);

        // Store the copy text for the copy command
        lastCopyText = fullText;

        sender.addChatMessage(copyButton);
    }

    public static String lastCopyText = "";

    private static String formatTime(long ms) {
        long s = ms / 1000, m = s / 60, h = m / 60;
        m %= 60; s %= 60;
        if (h > 0) return h + "h " + String.format("%02d", m) + "m " + String.format("%02d", s) + "s";
        if (m > 0) return m + "m " + String.format("%02d", s) + "s";
        return s + "s";
    }
}
