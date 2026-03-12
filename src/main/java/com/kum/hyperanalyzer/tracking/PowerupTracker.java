package com.kum.hyperanalyzer.tracking;

import com.kum.hyperanalyzer.data.SessionManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashSet;
import java.util.Set;

public class PowerupTracker {

    private static Set<Integer> countedEntityIds = new HashSet<>();
    private static int chestCheckTick = 0;

    // ── R2 powerup-mob tracking (ZombiesExplorer approach) ──────────────────
    // IDs of qualifying mobs that spawned in R2 (first wave).
    private static final Set<Integer> r2Wave1Mobs     = new HashSet<>();
    private static final Set<Integer> r2Wave1MobsDead = new HashSet<>();
    
    // Specifically track the first 2 mobs which are the actual powerup holders
    private static final Set<Integer> r2PowerupHolders     = new HashSet<>();
    private static final Set<Integer> r2PowerupHoldersDead = new HashSet<>();
    // True while we are in R2 and still collecting powerup-mob IDs.
    // Stays true until the first mob death — at that point we lock the set.
    private static boolean collectingR2Mobs = false;
    private static int wave1CollectionTicks = 0;
    // True once R2 has started and we haven't yet resolved max/insta outcome.
    private static boolean r2Active = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ZombiesDetector.tickDifficultyCheck();
        AutoReset.tick();

        if (!ZombiesDetector.isInZombies) {
            countedEntityIds.clear();
            resetR2State();
            return;
        }

        if (ZombiesDetector.currentGame == null) return;

        // Scan for chest every 2 ticks
        chestCheckTick++;
        if (chestCheckTick >= 2) {
            chestCheckTick = 0;

            String map = ZombiesDetector.currentGame.map;
            String chestLoc = ChestTracker.detectCurrentChestLocation(map);

            if (chestLoc != null) {
                ZombiesDetector.currentGame.currentChestLocation = chestLoc;
            }

            boolean isEarlyGame = ZombiesDetector.currentRound <= 1;

            if (isEarlyGame
                    && ZombiesDetector.currentGame.startChestLocation == null
                    && chestLoc != null
                    && ChestTracker.isValidStartChest(map, chestLoc)) {
                ZombiesDetector.currentGame.startChestLocation = chestLoc;
                SessionManager.saveCurrentSession();
                AutoReset.onChestDetected(map, chestLoc);
            }
        }



        // If we are in R2 powerup-mob tracking mode, poll dead status each tick
        // as a safety net in case LivingDeathEvent fires before entity is removed.
        if (r2Active && !r2Wave1Mobs.isEmpty()) {
            checkR2MobsDead();
        }
        
        // Strict 10-second (200 tick) window to collect Wave 1 mobs.
        // Wave 2 universally spawns at 10.0 seconds on Round 2 across all maps (ShowSpawnTime logic).
        // Once this expires, we lock the array to prevent Wave 2 contamination.
        if (r2Active && collectingR2Mobs) {
            wave1CollectionTicks++;
            if (wave1CollectionTicks >= 200) {
                collectingR2Mobs = false;
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (mc != null && mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                        "\u00a78[Debug] \u00a77Wave 1 Collection Window Locked! Total: " + r2Wave1Mobs.size()
                    ));
                }
            }
        }
    }

    // ── Called by ZombiesDetector.splitRound() when round becomes 2 ──────────
    public static void onRound2Start() {
        resetR2State();
        r2Active         = true;
        collectingR2Mobs = true;
        
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null && ZombiesDetector.currentGame != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                "\u00a78[Debug] \u00a77R2 Started. (Double Gold Drops: " + ZombiesDetector.currentGame.doubleGoldCount + ")"
            ));
        }
    }

    // ── Called by ZombiesDetector.splitRound() when round becomes 3 ──────────
    public static void onRound3Start() {
        if (ZombiesDetector.currentGame == null) return;
        
        boolean hadMaxInR2 = !ZombiesDetector.currentGame.maxAmmoPattern.isEmpty();
        boolean hadInsInR2 = !ZombiesDetector.currentGame.instaKillPattern.isEmpty();
        
        boolean madePrediction = false;
        if (!hadMaxInR2) {
            ZombiesDetector.currentGame.maxAmmoPattern.add("r3");
            madePrediction = true;
        }
        if (!hadInsInR2) {
            ZombiesDetector.currentGame.instaKillPattern.add("r3");
            madePrediction = true;
        }
        
        if (madePrediction) {
            SessionManager.saveCurrentSession();
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "\u00a7b[HyperAnalyzer] \u00a7eRound 3 Insta/Max Predicted \u00a77(Round 2 Ended)\u00a7e!"
                ));
            }
        }
        
        resetR2State();
    }

    // ── Called by ZombiesDetector when a new game starts or ends ─────────────
    public static void onGameReset() {
        resetR2State();
        countedEntityIds.clear();
        chestCheckTick = 0;
    }

    private static void resetR2State() {
        r2Active         = false;
        collectingR2Mobs = false;
        wave1CollectionTicks = 0;
        r2Wave1Mobs.clear();
        r2Wave1MobsDead.clear();
        r2PowerupHolders.clear();
        r2PowerupHoldersDead.clear();
    }

    // ── Entity join: collect qualifying mobs during R2 collection window ──────
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!r2Active || !collectingR2Mobs) return;
        if (!ZombiesDetector.isInZombies || ZombiesDetector.currentGame == null) return;

        if (!(event.entity instanceof EntityLivingBase)) return;
        EntityLivingBase mob = (EntityLivingBase) event.entity;

        // Skip the local player
        net.minecraft.client.entity.EntityPlayerSP player =
                net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (player != null && mob.equals(player)) return;

        // Only count mob types that Zombies spawns as powerup carriers
        if (!isZombiesMob(mob)) return;

        int id = mob.getEntityId();
        if (r2Wave1Mobs.add(id)) {
            // The first 2 mobs which are the actual powerup holders
            if (r2PowerupHolders.size() < 2) {
                r2PowerupHolders.add(id);
            }
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "\u00a78[Debug] \u00a77Tracked R2 Mob " + id + ". Total Wave 1 Mobs: " + r2Wave1Mobs.size()
                ));
            }
        }
    }

    // ── Entity death: track dead R2 mobs ────────────────────────────────────
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!r2Active) return;
        int id = event.entity.getEntityId();

        // First death in R2: lock the collection window so no more mobs are added
        if (collectingR2Mobs && r2Wave1Mobs.size() > 0) {
            collectingR2Mobs = false;
        }

        boolean tracked = false;
        if (r2Wave1Mobs.contains(id)) {
            if (r2Wave1MobsDead.add(id)) {
                tracked = true;
            }
        }
        if (r2PowerupHolders.contains(id)) {
            if (r2PowerupHoldersDead.add(id)) {
                tracked = true;
            }
        }

        if (tracked) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "\u00a78[Debug] \u00a77R2 Mob Died! Wave 1: " + r2Wave1MobsDead.size() + "/" + r2Wave1Mobs.size() + 
                    " | Holders: " + r2PowerupHoldersDead.size() + "/" + r2PowerupHolders.size()
                ));
            }
            checkR2MobsDead();
        }
    }

    /**
     * Called each tick (safety net) and after each death event.
     * If all collected R2 mobs are dead and no max/insta dropped → record R3.
     */
    private static void checkR2MobsDead() {
        if (!r2Active) return;
        if (collectingR2Mobs) return; // still in collection window (no death seen yet)
        if (r2Wave1Mobs.isEmpty()) return; // never tracked any mobs, can't conclude

        // Safety net: poll entity health each call in case death event was missed
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            for (int id : r2Wave1Mobs) {
                if (r2Wave1MobsDead.contains(id)) continue;
                net.minecraft.entity.Entity e = mc.theWorld.getEntityByID(id);
                if (e instanceof EntityLivingBase) {
                    EntityLivingBase mob = (EntityLivingBase) e;
                    if (mob.isDead || mob.getHealth() <= 0) {
                        r2Wave1MobsDead.add(id);
                        if (r2PowerupHolders.contains(id)) r2PowerupHoldersDead.add(id);
                    }
                } else if (e == null) {
                    // Entity removed from world = dead
                    r2Wave1MobsDead.add(id);
                    if (r2PowerupHolders.contains(id)) r2PowerupHoldersDead.add(id);
                }
            }
        }

        if (ZombiesDetector.currentGame == null) return;

        boolean requiresAllWave1 = ZombiesDetector.currentGame.doubleGoldCount > 0;

        boolean ready = false;
        if (requiresAllWave1) {
            // Must wait for all Wave 1 mobs
            ready = (r2Wave1MobsDead.size() == r2Wave1Mobs.size());
        } else {
            // Only wait for the 2 powerup holders
            ready = (r2PowerupHoldersDead.size() == r2PowerupHolders.size());
        }

        if (ready) {
            net.minecraft.client.Minecraft m = net.minecraft.client.Minecraft.getMinecraft();
            if (m != null && m.thePlayer != null) {
                m.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "\u00a78[Debug] \u00a77all tracked mobs dead, insta pattern: none, setting it automatically to R3"
                ));
            }
            resolveR2Outcome();
        }
    }

    private static void resolveR2Outcome() {
        if (!r2Active) return;
        r2Active = false;

        if (ZombiesDetector.currentGame == null) return;

        boolean hadMaxInR2 = !ZombiesDetector.currentGame.maxAmmoPattern.isEmpty();
        boolean hadInsInR2 = !ZombiesDetector.currentGame.instaKillPattern.isEmpty();

        if (!hadMaxInR2) {
            ZombiesDetector.currentGame.maxAmmoPattern.add("r3");
        }
        if (!hadInsInR2) {
            ZombiesDetector.currentGame.instaKillPattern.add("r3");
        }

        if (!hadMaxInR2 || !hadInsInR2) {
            SessionManager.saveCurrentSession();
            
            // Notify player in chat about the early prediction
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "\u00a7b[HyperAnalyzer] \u00a7eRound 3 Insta/Max Predicted!"
                ));
            }
        }
    }

    /**
     * Mirror of ZombiesExplorer's mob type filter.
     * Returns true for entity types that Hypixel Zombies spawns as powerup carriers.
     * Excludes squids, invisible chickens, mooshrooms, and the AA wolf near spawn.
     */
    private static boolean isZombiesMob(EntityLivingBase e) {
        if (e instanceof EntityZombie)      return true;
        if (e instanceof EntitySlime)       return true;
        if (e instanceof EntityWitch)       return true;
        if (e instanceof EntityEndermite)   return true;
        if (e instanceof EntityCreeper)     return true;
        if (e instanceof EntityBlaze)       return true;
        if (e instanceof EntitySkeleton)    return true;
        if (e instanceof EntityGhast)       return true;
        if (e instanceof EntityGolem)       return true;
        if (e instanceof EntitySilverfish)  return true;
        if (e instanceof EntityGiantZombie) return true;
        if (e instanceof EntityCaveSpider)  return true;
        if (e instanceof EntityOcelot)      return true;
        // Guardian only if large (max health > 30)
        if (e instanceof EntityGuardian)    return e.getMaxHealth() > 30;
        // Visible chickens count; invisible don't
        if (e instanceof EntityChicken)     return !e.isInvisible();
        // Wolf: exclude the AA spawn-area wolf (within 6 blocks of -16.5,72,-0.5)
        if (e instanceof EntityWolf) {
            String map = ZombiesDetector.currentGame != null ? ZombiesDetector.currentGame.map : "";
            if ("Alien Arcadium".equals(map) && e.getDistanceSq(-16.5, 72, -0.5) <= 36) return false;
            return true;
        }
        // Mooshroom and Squid excluded (not powerup carriers)
        if (e instanceof EntityMooshroom)   return false;
        return false;
    }


    public static void onArmorStandSpawn(String name, int entityId) {
        if (!ServerTracker.isOnHypixel && !ServerTracker.isOnZombiesServer) return;
        if (!ZombiesDetector.isInZombies || ZombiesDetector.currentGame == null) return;
        if (countedEntityIds.contains(entityId)) return;

        String lower = name.toLowerCase();
        int round = ZombiesDetector.currentRound;
        String map = ZombiesDetector.currentGame.map;
        if (round == 0) return;

        boolean counted = false;

        if (lower.contains("double gold")) {
            ZombiesDetector.currentGame.doubleGoldCount++;
            counted = true;
        } else if (lower.contains("bonus gold") && "Alien Arcadium".equals(map)) {
            ZombiesDetector.currentGame.bonusGoldCount++;
            counted = true;
        } else if (lower.contains("max ammo") || lower.contains("maks ammo")) {
            if (ZombiesDetector.currentGame.maxAmmoPattern.isEmpty()) {
                int effectiveRound = resolveEffectiveRound(round);
                ZombiesDetector.currentGame.maxAmmoPattern.add("r" + effectiveRound);
            }
            counted = true;
        } else if (lower.contains("insta kill")) {
            if (ZombiesDetector.currentGame.instaKillPattern.isEmpty()) {
                int effectiveRound = resolveEffectiveRound(round);
                ZombiesDetector.currentGame.instaKillPattern.add("r" + effectiveRound);
            }
            counted = true;
        } else if (lower.contains("shop") && lower.contains("spree")) {
            if ("Alien Arcadium".equals(map) || "The Lab".equals(map)) {
                if (ZombiesDetector.currentGame.shoppingSpreePattern.isEmpty()) {
                    ZombiesDetector.currentGame.shoppingSpreePattern.add("r" + round);
                }
            }
            counted = true;
        }

        if (counted) countedEntityIds.add(entityId);
    }

    /**
     * ShowSpawnTime carry-over logic: if a powerup armor stand fires in round 3
     * but within R2_CARRY_OVER_MS of the round starting, it was a R2 mob that
     * died just after the round ticked over — count it as R2.
     */
    private static final long R2_CARRY_OVER_MS = 10_000L;

    private static int resolveEffectiveRound(int round) {
        if (round != 3) return round;
        // In round 3: check how long since round 3 started
        long elapsedMs = 0;
        long tick = ZombiesDetector.currentTick();
        if (ZombiesDetector.roundStartTick > 0 && tick >= ZombiesDetector.roundStartTick) {
            elapsedMs = ZombiesDetector.ticksToMs(tick - ZombiesDetector.roundStartTick);
        }
        return elapsedMs < R2_CARRY_OVER_MS ? 2 : 3;
    }

    /**
     * Called from onChatReceived when we see a powerup activation broadcast.
     * Uses SST's approach as a secondary confirmation source — if the armor stand
     * detection already set the pattern, this is a no-op.
     */
    private static void onPowerupActivatedChat(String type) {
        if (!ZombiesDetector.isInZombies || ZombiesDetector.currentGame == null) return;
        int round = ZombiesDetector.currentRound;
        if (round < 2 || round > 3) return;

        int effectiveRound = resolveEffectiveRound(round);

        if ("max".equals(type) && ZombiesDetector.currentGame.maxAmmoPattern.isEmpty()) {
            ZombiesDetector.currentGame.maxAmmoPattern.add("r" + effectiveRound);
        } else if ("ins".equals(type) && ZombiesDetector.currentGame.instaKillPattern.isEmpty()) {
            ZombiesDetector.currentGame.instaKillPattern.add("r" + effectiveRound);
        }
    }


    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!ServerTracker.isOnHypixel && !ServerTracker.isOnZombiesServer) return;
        if (event.type != 0 && event.type != 1) return;

        String message = event.message.getUnformattedText().trim();

        // Try the click event value first — ZombiesAutoSplits sets SUGGEST_COMMAND to the clean
        // split text e.g. "Round 2 took 0:36.55!" or "Round 2 finished at 1:10.55!"
        // This is the most reliable source because getUnformattedText() flattens multi-line components.
        try {
            net.minecraft.event.ClickEvent ce = event.message.getChatStyle().getChatClickEvent();
            if (ce != null && ce.getAction() == net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND) {
                String val = ce.getValue();
                if (val != null && (val.contains(" took ") || val.contains(" finished at "))) {
                    ZombiesDetector.onSplitChat(val);
                }
            }
        } catch (Exception ignored) {}

        // Also pass formatted text (preserves \n between component siblings) for win/loss detection
        // and as a fallback split source
        String formatted = event.message.getFormattedText();
        ZombiesDetector.onChat(formatted.isEmpty() ? message : formatted);

        // ── Powerup activation chat detection (SST approach) ──────────────────
        // Activation broadcasts have no colon (not player chat). Only listen in R2/R3 window.
        if (ZombiesDetector.isInZombies && ZombiesDetector.currentGame != null
                && !message.contains(":")) {
            String lmsg = message.toLowerCase();
            if (lmsg.contains("max ammo") || lmsg.contains("maks ammo")) {
                onPowerupActivatedChat("max");
            } else if (lmsg.contains("insta kill")) {
                onPowerupActivatedChat("ins");
            }
        }

        // Weapon roll detection — use unformatted for simple string matching
        if (ZombiesDetector.isInZombies && ZombiesDetector.currentGame != null) {
            String map = ZombiesDetector.currentGame.map;
            String weapon = ChestTracker.parseWeaponFromChat(message, map);
            if (weapon != null) {
                java.util.Map<String, Integer> rolls = ZombiesDetector.currentGame.weaponRolls;
                if (rolls == null) {
                    rolls = new java.util.LinkedHashMap<>();
                    ZombiesDetector.currentGame.weaponRolls = rolls;
                }
                rolls.put(weapon, rolls.getOrDefault(weapon, 0) + 1);
                SessionManager.saveCurrentSession();
            }
        }
    }
}
