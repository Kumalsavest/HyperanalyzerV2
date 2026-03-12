package com.kum.hyperanalyzer.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    public static ConfigData instance = new ConfigData();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    public static void init() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config/hyperanalyzer/");
        if (!configDir.exists()) configDir.mkdirs();
        configFile = new File(configDir, "config.json");
        load();
    }

    public static void load() {
        if (configFile != null && configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                instance = GSON.fromJson(reader, ConfigData.class);
                if (instance == null) instance = new ConfigData();
                instance.validate();
            } catch (Exception e) {
                e.printStackTrace();
                instance = new ConfigData();
            }
        } else {
            save();
        }
    }

    public static void save() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ConfigData {
        // Give info HUD toggle
        public boolean giveInfoEnabled = false;

        // Remembered difficulty filter across screens
        public String savedDifficulty = "Normal";

        // Favorite game IDs: "yyyy-MM-dd:gameIndex" (1-based)
        public java.util.List<String> favoriteGames = new java.util.ArrayList<>();

        // Custom names for favorite games: key → name
        public java.util.Map<String, String> favoriteNames = new java.util.LinkedHashMap<>();

        // HUD positions (fractional 0..1)
        public float[] posOverallFastest  = {0.01f, 0.18f};
        public float[] posDailyAverages   = {0.01f, 0.50f};
        public float[] posChestStats      = {0.78f, 0.05f};
        public float[] posWeaponStats     = {0.78f, 0.40f};
        public float[] posSessionFastest  = {0.02f, 0.32f};
        public float[] posSessionAvgTime  = {0.35f, 0.32f};
        public float[] posR2Stats         = {0.75f, 0.32f};
        public float[] posSessionChests   = {0.78f, 0.05f};
        public float[] posSessionWeapons  = {0.78f, 0.45f};
        public float[] posGiveInfoKeyPart = {0.01f, 0.02f};
        public float[] posGiveInfoChest   = {0.01f, 0.06f};

        // Map toggle memory
        public int savedChestMapIndex  = 0;
        public int savedWeaponMapIndex = 0;

        // Auto Reset — global toggle + map selection
        public boolean autoResetEnabled = false;
        public String  autoResetMap     = "Bad Blood";

        // Per-map target slots.  Each slot is a chest name or "NA".
        // Bad Blood: 5 slots (one per valid start chest)
        public String[] arBadBloodTargets  = {"NA","NA","NA","NA","NA"};
        // Dead End: 5 slots
        public String[] arDeadEndTargets   = {"NA","NA","NA","NA","NA"};
        // Prison: 3 slots + keyPartYard toggle
        public String[] arPrisonTargets    = {"NA","NA","NA"};
        public boolean  arPrisonYardOnly   = false;

        /** Ensure arrays are correct length after loading from old config. */
        public void validate() {
            if (arBadBloodTargets == null || arBadBloodTargets.length != 5) {
                arBadBloodTargets = new String[]{"NA","NA","NA","NA","NA"};
            }
            if (arDeadEndTargets == null || arDeadEndTargets.length != 5) {
                arDeadEndTargets = new String[]{"NA","NA","NA","NA","NA"};
            }
            if (arPrisonTargets == null || arPrisonTargets.length != 3) {
                arPrisonTargets = new String[]{"NA","NA","NA"};
            }
            for (int i = 0; i < arBadBloodTargets.length; i++)
                if (arBadBloodTargets[i] == null) arBadBloodTargets[i] = "NA";
            for (int i = 0; i < arDeadEndTargets.length; i++)
                if (arDeadEndTargets[i] == null) arDeadEndTargets[i] = "NA";
            for (int i = 0; i < arPrisonTargets.length; i++)
                if (arPrisonTargets[i] == null) arPrisonTargets[i] = "NA";
            if (savedDifficulty == null) savedDifficulty = "Normal";
            if (favoriteGames == null) favoriteGames = new java.util.ArrayList<>();
            if (favoriteNames == null) favoriteNames = new java.util.LinkedHashMap<>();
        }
    }
}
