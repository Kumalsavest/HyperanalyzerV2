package com.kum.hyperanalyzer.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SessionManager {
    public static SessionData currentSession;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static File sessionDir;

    public static void init() {
        sessionDir = new File(Minecraft.getMinecraft().mcDataDir, "config/hyperanalyzer/sessions/");
        if (!sessionDir.exists()) {
            sessionDir.mkdirs();
        }
        loadCurrentSession();
    }

    public static void loadCurrentSession() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File sessionFile = new File(sessionDir, today + ".json");
        if (sessionFile.exists()) {
            try (FileReader reader = new FileReader(sessionFile)) {
                currentSession = GSON.fromJson(reader, SessionData.class);
            } catch (Exception e) {
                e.printStackTrace();
                currentSession = new SessionData(today);
            }
        } else {
            currentSession = new SessionData(today);
            saveCurrentSession();
        }
        // Safety: GSON may deserialise a null games list from a malformed/empty file
        if (currentSession != null && currentSession.games == null)
            currentSession.games = new java.util.ArrayList<>();
    }

    public static void saveCurrentSession() {
        if (currentSession == null)
            return;
        File sessionFile = new File(sessionDir, currentSession.date + ".json");
        try (FileWriter writer = new FileWriter(sessionFile)) {
            GSON.toJson(currentSession, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkDayChange() {
        if (currentSession == null)
            return;
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (!currentSession.date.equals(today)) {
            saveCurrentSession();
            currentSession = new SessionData(today);
            saveCurrentSession();
        }
    }

    public static List<String> getAllSavedSessions() {
        List<String> list = new ArrayList<>();
        if (sessionDir != null && sessionDir.listFiles() != null) {
            for (File file : sessionDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    list.add(file.getName().replace(".json", ""));
                }
            }
        }
        java.util.Collections.sort(list, java.util.Collections.reverseOrder());
        return list;
    }

    public static SessionData loadSession(String date) {
        if (currentSession != null && currentSession.date.equals(date)) {
            return currentSession;
        }
        File file = new File(sessionDir, date + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return GSON.fromJson(reader, SessionData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Completely delete a past session file (non-today sessions).
     * Wipes all games and stats permanently.
     */
    public static void deleteSession(String date) {
        File file = new File(sessionDir, date + ".json");
        if (file.exists()) file.delete();
    }

    /**
     * Clear today's current session — removes all games and resets all counters,
     * but keeps the empty session file so today starts fresh.
     */
    public static void clearCurrentSession() {
        if (currentSession == null) return;
        currentSession.games = new java.util.ArrayList<>();
        currentSession.zombiesGamesTotal = 0;
        currentSession.zombiesGamesWon   = 0;
        currentSession.zombiesGamesLost  = 0;
        currentSession.zombiesGamesReset = 0;
        currentSession.totalZombiesPlaytime = 0;
        currentSession.totalHypixelPlaytime = 0;
        saveCurrentSession();
    }
}
