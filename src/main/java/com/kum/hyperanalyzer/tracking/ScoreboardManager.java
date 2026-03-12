package com.kum.hyperanalyzer.tracking;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScoreboardManager {
    public static String title = "";
    public static List<String> content = new ArrayList<>();
    public static List<String> rawContent = new ArrayList<>(); // with color codes
    private int tick = 0;

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.isSingleplayer()) {
            clear();
            return;
        }

        tick++;
        if (tick % 5 == 0) {
            updateScoreboardContent(mc);
            tick = 0;
        }
    }

    private void updateScoreboardContent(Minecraft mc) {
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebarObjective == null) {
            clear();
            return;
        }

        title = net.minecraft.util.StringUtils.stripControlCodes(sidebarObjective.getDisplayName()).trim();

        List<String> scoreboardLines = new ArrayList<>();
        List<String> rawLines = new ArrayList<>();
        Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);

        List<Score> filteredScores = new ArrayList<>();
        for (Score score : scores) {
            if (score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                filteredScores.add(score);
            }
        }

        java.util.Collections.reverse(filteredScores);
        for (Score line : filteredScores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(line.getPlayerName());
            String formatted = ScorePlayerTeam.formatPlayerName(team, line.getPlayerName());
            String stripped = net.minecraft.util.StringUtils.stripControlCodes(formatted).trim();
            scoreboardLines.add(stripped);
            rawLines.add(formatted.trim());
        }
        content = scoreboardLines;
        rawContent = rawLines;
    }

    private void clear() {
        title = "";
        content.clear();
        rawContent.clear();
    }
}
