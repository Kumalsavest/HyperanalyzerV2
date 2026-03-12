package com.kum.hyperanalyzer.tracking;

import com.kum.hyperanalyzer.data.ModConfig;
import net.minecraft.client.Minecraft;

/**
 * Handles the Auto Reset feature.
 *
 * Logic per map:
 *   Bad Blood / Dead End  — up to 5 target chest slots (each can be a chest name or "NA").
 *                           If ALL slots are NA → any chest passes (no reset).
 *                           Otherwise reset unless detected chest matches at least one non-NA slot.
 *   Prison                — up to 3 target chest slots, PLUS an optional "Yard key-part only" toggle.
 *                           Chest slots: same logic as above (NA = ignore chest check).
 *                           Yard toggle: if enabled, reset unless key part == Yard.
 *                           Both conditions must pass if both are active.
 *
 * Fire the requeue command IMMEDIATELY when a mismatch is detected (no delay).
 */
public class AutoReset {

    private static boolean hasCheckedThisGame = false;
    // Prison needs key-part too, which arrives separately from chest
    private static String pendingMap = null;
    private static String pendingChest = null;
    private static boolean chestChecked = false;
    private static boolean keyPartChecked = false;

    // Delay before firing the reset command (~2.5s = 50 ticks)
    private static final int RESET_DELAY_TICKS = 50;
    private static int resetCountdown = 0;        // >0 means a reset is pending
    private static String pendingResetMap = null; // map to reset for when countdown fires

    public static void tick() {
        if (resetCountdown > 0) {
            resetCountdown--;
            if (resetCountdown == 0 && pendingResetMap != null) {
                fireRequeue(pendingResetMap);
                pendingResetMap = null;
            }
        }
    }

    /** Called when a new game starts — re-arm all flags. */
    public static void onGameStart() {
        hasCheckedThisGame = false;
        pendingMap    = null;
        pendingChest  = null;
        chestChecked  = false;
        keyPartChecked = false;
        resetCountdown = 0;
        pendingResetMap = null;
    }

    /**
     * Called when the Round 1 chest location is confirmed.
     * For BB/DE this is all we need.  For Prison we may also need key part.
     */
    public static void onChestDetected(String map, String chestLocation) {
        if (!ModConfig.instance.autoResetEnabled) return;
        if (!"Bad Blood".equals(map) && !"Dead End".equals(map) && !"Prison".equals(map)) return;
        if (!map.equals(ModConfig.instance.autoResetMap)) return;
        if (chestChecked) return;

        chestChecked = true;
        pendingMap   = map;
        pendingChest = chestLocation;

        if ("Prison".equals(map)) {
            // For Prison we might need to wait for key-part too
            evaluatePrison();
        } else {
            // BB / DE: evaluate now
            evaluateNonPrison(map, chestLocation);
        }
    }

    /**
     * Called when Prison key part is identified (Yard / Courts / Unidentified).
     */
    public static void onKeyPartDetected(String keyPart) {
        if (!ModConfig.instance.autoResetEnabled) return;
        if (!"Prison".equals(ModConfig.instance.autoResetMap)) return;
        if (keyPartChecked) return;

        keyPartChecked = true;
        evaluatePrison();
    }

    // ---- Evaluation ----

    private static void evaluateNonPrison(String map, String chest) {
        if (hasCheckedThisGame) return;
        hasCheckedThisGame = true;

        String[] targets = getTargetsForMap(map);
        if (allNA(targets)) return; // nothing configured — don't reset

        if (!matchesAnyTarget(chest, targets)) {
            scheduleRequeue(map);
        }
    }

    private static void evaluatePrison() {
        if (hasCheckedThisGame) return;

        boolean yardOnly   = ModConfig.instance.arPrisonYardOnly;
        String[] targets   = ModConfig.instance.arPrisonTargets;
        boolean allChestNA = allNA(targets);

        // We need chest data if chest slots are active, and key-part data if yard toggle is on
        boolean needChest   = !allChestNA;
        boolean needKeyPart = yardOnly;

        // If neither condition is configured, do nothing
        if (!needChest && !needKeyPart) return;

        // Wait until all needed data is available
        if (needChest   && !chestChecked)   return;
        if (needKeyPart && !keyPartChecked) return;

        hasCheckedThisGame = true;

        boolean shouldReset = false;

        // Chest check
        if (needChest && pendingChest != null) {
            if (!matchesAnyTarget(pendingChest, targets)) {
                shouldReset = true;
            }
        }

        // Key-part check — reset IF key part IS Yard (user wants to avoid Yard games)
        if (!shouldReset && needKeyPart) {
            String kp = com.kum.hyperanalyzer.tracking.ZombiesDetector.currentGame != null
                    ? com.kum.hyperanalyzer.tracking.ZombiesDetector.currentGame.keyPart : null;
            if (kp != null && kp.startsWith("Y")) { // "Yard" starts with Y → reset
                shouldReset = true;
            }
        }

        if (shouldReset) {
            scheduleRequeue("Prison");
        }
    }

    // ---- Helpers ----

    private static boolean allNA(String[] targets) {
        for (String t : targets) if (t != null && !t.equals("NA")) return false;
        return true;
    }

    private static boolean matchesAnyTarget(String chest, String[] targets) {
        for (String t : targets) {
            if (t != null && !t.equals("NA") && t.equalsIgnoreCase(chest)) return true;
        }
        return false;
    }

    /** Schedule a reset after RESET_DELAY_TICKS ticks (~1.5s). */
    private static void scheduleRequeue(String map) {
        pendingResetMap = map;
        resetCountdown  = RESET_DELAY_TICKS;
    }

    private static void fireRequeue(String map) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        String cmd = getRequeueCommand(map);
        if (cmd != null) mc.thePlayer.sendChatMessage(cmd);
    }

    public static String getRequeueCommand(String map) {
        switch (map) {
            case "Bad Blood": return "/play Arcade_Zombies_Bad_Blood";
            case "Dead End":  return "/play Arcade_Zombies_Dead_End";
            case "Prison":    return "/play Arcade_Zombies_Prison";
            default:          return null;
        }
    }

    public static String[] getTargetsForMap(String map) {
        switch (map) {
            case "Bad Blood": return ModConfig.instance.arBadBloodTargets;
            case "Dead End":  return ModConfig.instance.arDeadEndTargets;
            case "Prison":    return ModConfig.instance.arPrisonTargets;
            default:          return new String[]{};
        }
    }

    /** All valid round-1 start chests for a map (no "NA" here — that's added in UI). */
    public static String[] getStartChestOptions(String map) {
        switch (map) {
            case "Bad Blood": return new String[]{"Mansion","Library","Dungeon","Crypts","Balcony"};
            case "Dead End":  return new String[]{"Office","Gallery","Power Station","Apartments","Hotel"};
            case "Prison":    return new String[]{
                "Shower Room","Cell Block","Visitors","Corridors","Cafeteria","Kitchen","Library",
                "Monitor Room","Medbay","Courts","Yard","Guards Quarters","Isolation",
                "Guards Gunroom","Boiler Room","The Deep","Basement Corridors","Stockage"};
            default:          return new String[]{};
        }
    }

    public static final String[] AUTO_RESET_MAPS = {"Bad Blood", "Dead End", "Prison"};
}
