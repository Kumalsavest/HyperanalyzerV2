package com.kum.hyperanalyzer.tracking;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

import java.util.List;

/**
 * Detects the Lucky Chest location by scanning for armor stand entities
 * near known coordinates for each map.
 * Also handles weapon-roll detection from chat.
 */
public class ChestTracker {

    // Tolerance radius around each chest coordinate (generous to account for entity float)
    private static final double RADIUS = 10.0;

    // ---- Bad Blood chest locations ----
    private static final double[][] BAD_BLOOD_CHESTS = {
        {-4,  74,  -7},   // Mansion
        {-14, 75, -44},   // Library
        {-20, 69, -32},   // Dungeon
        {-21, 69,  13},   // Crypts
        {-49, 69,  21},   // Balcony
        {21,  70,  12},   // Courtyard (live only, not start)
    };
    private static final String[] BAD_BLOOD_NAMES = {
        "Mansion", "Library", "Dungeon", "Crypts", "Balcony", "Courtyard"
    };
    private static final boolean[] BAD_BLOOD_START_VALID = {
        true, true, true, true, true, false  // Courtyard excluded
    };

    // ---- Dead End chest locations ----
    private static final double[][] DEAD_END_CHESTS = {
        {33,  77, 54},   // Office
        {39,  77, 65},   // Gallery
        {5,   84, 49},   // Power Station
        {-8,  77, 49},   // Apartments
        {-9,  69, 24},   // Hotel
        {16,  70, 17},   // Alley (live only, not start)
    };
    private static final String[] DEAD_END_NAMES = {
        "Office", "Gallery", "Power Station", "Apartments", "Hotel", "Alley"
    };
    private static final boolean[] DEAD_END_START_VALID = {
        true, true, true, true, true, false  // Alley excluded
    };

    // ---- Prison chest locations ----
    private static final double[][] PRISON_CHESTS = {
        {48,  73,  11},  // Shower Room
        {93,  73,   4},  // Cell Block
        {124, 73, -15},  // Visitors
        {104, 73,  11},  // Corridors
        {120, 73,  19},  // Cafeteria
        {123, 74,  19},  // Kitchen
        {133, 80,  -4},  // Library
        {109, 80,  -3},  // Monitor Room
        {98,  80,  34},  // Medbay
        {120, 73,  66},  // Courts
        {81,  73,  15},  // Yard
        {41,  75,  31},  // Guards Quarters
        {47,  73,  55},  // Isolation
        {22,  75,  42},  // Guards Gunroom
        {64,  64,   9},  // Boiler Room
        {81,  64, -15},  // The Deep
        {108, 66,   7},  // Basement Corridors
        {98,  66, -17},  // Stockage
    };
    private static final String[] PRISON_NAMES = {
        "Shower Room", "Cell Block", "Visitors", "Corridors", "Cafeteria",
        "Kitchen", "Library", "Monitor Room", "Medbay", "Courts", "Yard",
        "Guards Quarters", "Isolation", "Guards Gunroom", "Boiler Room",
        "The Deep", "Basement Corridors", "Stockage"
    };
    private static final boolean[] PRISON_START_VALID;
    static {
        PRISON_START_VALID = new boolean[PRISON_NAMES.length];
        for (int i = 0; i < PRISON_START_VALID.length; i++) PRISON_START_VALID[i] = true;
    }

    // ---- Weapon lists per map ----
    // Prison: no Blow Dart or Zombie Soaker
    public static final String[] BB_DE_WEAPONS = {
        "Zombie Zapper", "The Puncher", "Flamethrower", "Gold Digger", "Blow Dart", "Zombie Soaker", "Elder Gun"
    };
    public static final String[] PRISON_WEAPONS = {
        "Zombie Zapper", "The Puncher", "Flamethrower", "Gold Digger",
        "Lightning Rod Skill", "Heal Skill"
    };
    public static final String[] AA_WEAPONS = {
        "Zombie Zapper", "The Puncher", "Flamethrower", "Gold Digger",
        "Rainbow Rifle", "Double Barrel Shotgun", "Lightning Rod Skill", "Heal Skill"
    };

    /**
     * Detect the current chest location by scanning armor stands.
     *
     * Strategy (two-pass):
     *   Pass 1 — find an armor stand whose name contains "lucky chest" near a known coord.
     *            This is the most reliable signal and avoids false positives from powerup stands.
     *   Pass 2 — if no named stand found, look for ANY armor stand within a tighter 4-block
     *            radius of a known coord, but skip stands whose names match powerup keywords.
     *            This handles the case where the chest stand exists but hasn't received its
     *            name packet yet.
     */
    public static String detectCurrentChestLocation(String map) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return null;

        double[][] coords;
        String[] names;
        switch (map) {
            case "Bad Blood": coords = BAD_BLOOD_CHESTS; names = BAD_BLOOD_NAMES; break;
            case "Dead End":  coords = DEAD_END_CHESTS;  names = DEAD_END_NAMES;  break;
            case "Prison":    coords = PRISON_CHESTS;    names = PRISON_NAMES;    break;
            default: return null;
        }

        List<Entity> entities = mc.theWorld.loadedEntityList;

        // Pass 1: look for stand explicitly named "lucky chest"
        for (Entity e : entities) {
            if (!(e instanceof EntityArmorStand)) continue;
            String rawName = e.getCustomNameTag();
            if (rawName == null) continue;
            String stripped = net.minecraft.util.StringUtils.stripControlCodes(rawName).toLowerCase();
            if (!stripped.contains("lucky chest")) continue;
            // Named lucky chest stand — find which coord it belongs to
            double ex = e.posX, ey = e.posY, ez = e.posZ;
            String bestName = null;
            double bestDist = RADIUS;
            for (int i = 0; i < coords.length; i++) {
                double d = dist(ex, ey, ez, coords[i][0], coords[i][1], coords[i][2]);
                if (d < bestDist) { bestDist = d; bestName = names[i]; }
            }
            if (bestName != null) return bestName;
        }

        // Pass 2: position-only within tight radius, skipping powerup/mob stands
        String bestName = null;
        double bestDist = 4.0; // tighter than RADIUS to avoid false positives
        for (Entity e : entities) {
            if (!(e instanceof EntityArmorStand)) continue;
            String rawName = e.getCustomNameTag();
            if (rawName != null) {
                String low = net.minecraft.util.StringUtils.stripControlCodes(rawName).toLowerCase();
                if (low.contains("max ammo") || low.contains("insta kill")
                        || low.contains("double gold") || low.contains("shopping spree")
                        || low.contains("bonus gold") || low.contains("zombie")
                        || low.contains("spider") || low.contains("creeper")
                        || low.contains("skeleton") || low.contains("enderman")
                        || low.contains("witch") || low.contains("slime")
                        || low.contains("health") || low.contains("hp")) continue;
            }
            double ex = e.posX, ey = e.posY, ez = e.posZ;
            for (int i = 0; i < coords.length; i++) {
                double d = dist(ex, ey, ez, coords[i][0], coords[i][1], coords[i][2]);
                if (d < bestDist) { bestDist = d; bestName = names[i]; }
            }
        }
        return bestName;
    }

    private static double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1-x2, dy = y1-y2, dz = z1-z2;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /** Returns true if the given chest name is valid as a round-1 start chest for this map. */
    public static boolean isValidStartChest(String map, String chestName) {
        String[] names;
        boolean[] valid;
        switch (map) {
            case "Bad Blood": names = BAD_BLOOD_NAMES; valid = BAD_BLOOD_START_VALID; break;
            case "Dead End":  names = DEAD_END_NAMES;  valid = DEAD_END_START_VALID;  break;
            case "Prison":    names = PRISON_NAMES;    valid = PRISON_START_VALID;    break;
            default: return false;
        }
        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(chestName)) return valid[i];
        }
        return false;
    }

    /** Get the weapon list for a given map. */
    public static String[] getWeaponsForMap(String map) {
        switch (map) {
            case "Prison":         return PRISON_WEAPONS;
            case "Alien Arcadium": return AA_WEAPONS;
            default:               return BB_DE_WEAPONS;
        }
    }

    /**
     * Parse a weapon name from chat:
     * "You found Zombie Zapper in the Lucky Chest! You have 10s to claim it before it disappears!"
     */
    public static String parseWeaponFromChat(String message, String map) {
        String lower = message.toLowerCase();
        // "You found X in the Lucky Chest" (self) OR "[Name] found X in the Lucky Chest" (teammate)
        boolean isWeaponFind = lower.contains("lucky chest")
                && (lower.contains("you found") || lower.contains("found"));
        if (!isWeaponFind) return null;
        // Use all known weapons across all maps so we never miss one
        String[] allWeapons = {
            "Zombie Zapper", "The Puncher", "Flamethrower", "Gold Digger",
            "Blow Dart", "Zombie Soaker", "Lightning Rod Skill", "Heal Skill",
            "Rainbow Rifle", "Double Barrel Shotgun", "Elder Gun"
        };
        for (String w : allWeapons) {
            if (lower.contains(w.toLowerCase())) return w;
        }
        return null;
    }
}
