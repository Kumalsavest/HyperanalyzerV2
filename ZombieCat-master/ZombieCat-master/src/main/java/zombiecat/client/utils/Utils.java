package zombiecat.client.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import zombiecat.client.module.setting.impl.DoubleSliderSetting;

import java.awt.*;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

public class Utils {
   private static final Random rand = new Random();
   public static final Minecraft mc = Minecraft.getMinecraft();

   public static class Client {
      public static void setMouseButtonState(int mouseButton, boolean held) {
         MouseEvent m = new MouseEvent();
         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
         ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
         MinecraftForge.EVENT_BUS.post(m);
         ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, new String[]{"buttons"});
         buttons.put(mouseButton, (byte)(held ? 1 : 0));
         ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");
      }

      public static double ranModuleVal(DoubleSliderSetting a, Random r) {
         return a.getInputMin() == a.getInputMax() ? a.getInputMin() : a.getInputMin() + r.nextDouble() * (a.getInputMax() - a.getInputMin());
      }

      public static net.minecraft.util.Timer getTimer() {
         return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
      }

      public static int rainbow(long delay) {
         double rainbowState = Math.ceil((double)(System.currentTimeMillis() + delay * 300L) / 20.0);
         return Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), 0.49019608F, 1.0F).getRGB();
      }

      public static int rainbowDraw(long speed, long... delay) {
         long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
         return Color.getHSBColor((float)(time % (15000L / speed)) / (15000.0F / (float)speed) * 0.7F, 0.7F, 1.0F).getRGB();
      }

      public static int astolfoColorsDraw(int yOffset, int yTotal, float speed) {
         float hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9);

         while (hue > speed) {
            hue -= speed;
         }

         hue /= speed;
         if ((double)hue > 0.5) {
            hue = 0.5F - (hue - 0.5F);
         }

         hue += 0.5F;
         return Color.HSBtoRGB(hue, 0.5F, 1.0F);
      }

      public static int astolfoColorsDraw(int yOffset, int yTotal) {
         return astolfoColorsDraw(yOffset, yTotal, 2900.0F);
      }
      public static boolean currentScreenMinecraft() {
         return Utils.mc.currentScreen == null;
      }

      public static int serverResponseTime() {
         return Utils.mc.getNetHandler().getPlayerInfo(Utils.mc.thePlayer.getUniqueID()).getResponseTime();
      }

      public static List<String> getPlayersFromScoreboard() {
         List<String> lines = new ArrayList<>();
         if (Utils.mc.theWorld == null) {
            return lines;
         } else {
            Scoreboard scoreboard = Utils.mc.theWorld.getScoreboard();
            if (scoreboard != null) {
               ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
               if (objective != null) {
                  Collection<Score> scores = scoreboard.getSortedScores(objective);
                  List<Score> list = new ArrayList<>();

                  for (Score score : scores) {
                     if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                        list.add(score);
                     }
                  }

                  if (list.size() > 15) {
                     scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
                  } else {
                     scores = list;
                  }

                  for (Score scorex : scores) {
                     ScorePlayerTeam team = scoreboard.getPlayersTeam(scorex.getPlayerName());
                     lines.add(ScorePlayerTeam.formatPlayerName(team, scorex.getPlayerName()));
                  }
               }
            }

            return lines;
         }
      }

      public static String reformat(String txt) {
         return txt.replace("&", "§");
      }
   }

   public static class HUD {
      private static final Minecraft mc = Minecraft.getMinecraft();

      public static void drawBoxAroundEntity(Entity e, boolean expand, int color) {
          //if (e instanceof EntityLivingBase) {
            double x = e.lastTickPosX
               + (e.posX - e.lastTickPosX) * (double)Utils.Client.getTimer().renderPartialTicks
               - mc.getRenderManager().viewerPosX;
            double y = e.lastTickPosY
               + (e.posY - e.lastTickPosY) * (double)Utils.Client.getTimer().renderPartialTicks
               - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ
               + (e.posZ - e.lastTickPosZ) * (double)Utils.Client.getTimer().renderPartialTicks
               - mc.getRenderManager().viewerPosZ;

            GlStateManager.pushMatrix();
             if (color == 0) {
                 color = Client.rainbowDraw(2L, 0L);
             }

             float a = (float)(color >> 24 & 0xFF) / 255.0F;
             float r = (float)(color >> 16 & 0xFF) / 255.0F;
             float g = (float)(color >> 8 & 0xFF) / 255.0F;
             float b = (float)(color & 0xFF) / 255.0F;
             AxisAlignedBB bbox = e.getEntityBoundingBox();
             double ex = expand ? e.width / 3 : 0;
             AxisAlignedBB axis = new AxisAlignedBB(
                bbox.minX - e.posX + x - ex,
                bbox.minY - e.posY + y,
                bbox.minZ - e.posZ + z - ex,
                bbox.maxX - e.posX + x + ex,
                bbox.maxY - e.posY + y,
                bbox.maxZ - e.posZ + z + ex
             );
             GL11.glBlendFunc(770, 771);
             GL11.glEnable(3042);
             GL11.glDisable(3553);
             GL11.glDisable(2929);
             GL11.glDepthMask(false);
             GL11.glLineWidth(2.0F);
             GL11.glColor4f(r, g, b, a);
             RenderGlobal.drawSelectionBoundingBox(axis);

             GL11.glEnable(3553);
             GL11.glEnable(2929);
             GL11.glDepthMask(true);
             GL11.glDisable(3042);

             GlStateManager.popMatrix();
        //}
      }

      public static void drawColouredText(
         String text, char lineSplit, int leftOffset, int topOffset, long colourParam1, long shift, boolean rect, FontRenderer fontRenderer
      ) {
         int bX = leftOffset;
         int l = 0;
         long colourControl = 0L;

         for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == lineSplit) {
               l++;
               leftOffset = bX;
               topOffset += fontRenderer.FONT_HEIGHT + 5;
               colourControl = shift * (long)l;
            } else {
               fontRenderer.drawString(
                  String.valueOf(c), (float)leftOffset, (float)topOffset, Utils.Client.astolfoColorsDraw((int)colourParam1, (int)colourControl), rect
               );
               leftOffset += fontRenderer.getCharWidth(c);
               if (c != ' ') {
                  colourControl -= 90L;
               }
            }
         }
      }

      public static Utils.PositionMode getPostitionMode(int marginX, int marginY, double height, double width) {
         int halfHeight = (int)(height / 4.0);
         int halfWidth = (int)width;
         Utils.PositionMode positionMode = null;
         if (marginY < halfHeight) {
            if (marginX < halfWidth) {
               positionMode = Utils.PositionMode.UPLEFT;
            }

            if (marginX > halfWidth) {
               positionMode = Utils.PositionMode.UPRIGHT;
            }
         }

         if (marginY > halfHeight) {
            if (marginX < halfWidth) {
               positionMode = Utils.PositionMode.DOWNLEFT;
            }

            if (marginX > halfWidth) {
               positionMode = Utils.PositionMode.DOWNRIGHT;
            }
         }

         return positionMode;
      }

      public static void d2p(double x, double y, int radius, int sides, int color) {
         float a = (float)(color >> 24 & 0xFF) / 255.0F;
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         Tessellator tessellator = Tessellator.getInstance();
         WorldRenderer worldrenderer = tessellator.getWorldRenderer();
         GlStateManager.enableBlend();
         GlStateManager.disableTexture2D();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.color(r, g, b, a);
         worldrenderer.begin(6, DefaultVertexFormats.POSITION);

         for (int i = 0; i < sides; i++) {
            double angle = (Math.PI * 2) * (double)i / (double)sides + Math.toRadians(180.0);
            worldrenderer.pos(x + Math.sin(angle) * (double)radius, y + Math.cos(angle) * (double)radius, 0.0).endVertex();
         }

         tessellator.draw();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
      }

      public static void d3p(double x, double y, double z, double radius, int sides, float lineWidth, int color, boolean chroma) {
         float a = (float)(color >> 24 & 0xFF) / 255.0F;
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         mc.entityRenderer.disableLightmap();
         GL11.glDisable(3553);
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glDisable(2929);
         GL11.glEnable(2848);
         GL11.glDepthMask(false);
         GL11.glLineWidth(lineWidth);
         if (!chroma) {
            GL11.glColor4f(r, g, b, a);
         }

         GL11.glBegin(1);
         long d = 0L;
         long ed = 15000L / (long)sides;
         long hed = ed / 2L;

         for (int i = 0; i < sides * 2; i++) {
            if (chroma) {
               if (i % 2 != 0) {
                  if (i == 47) {
                     d = hed;
                  }

                  d += ed;
               }

               int c = Utils.Client.rainbowDraw(2L, d);
               float r2 = (float)(c >> 16 & 0xFF) / 255.0F;
               float g2 = (float)(c >> 8 & 0xFF) / 255.0F;
               float b2 = (float)(c & 0xFF) / 255.0F;
               GL11.glColor3f(r2, g2, b2);
            }

            double angle = (Math.PI * 2) * (double)i / (double)sides + Math.toRadians(180.0);
            GL11.glVertex3d(x + Math.cos(angle) * radius, y, z + Math.sin(angle) * radius);
         }

         GL11.glEnd();
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         GL11.glDepthMask(true);
         GL11.glDisable(2848);
         GL11.glEnable(2929);
         GL11.glDisable(3042);
         GL11.glEnable(3553);
         mc.entityRenderer.enableLightmap();
      }
   }

   public enum PositionMode {
      UPLEFT,
      UPRIGHT,
      DOWNLEFT,
      DOWNRIGHT
   }
   
   public static class Java {
      public static int getValue(JsonObject type, String member) {
         try {
            return type.get(member).getAsInt();
         } catch (NullPointerException var3) {
            return 0;
         }
      }

      public static int indexOf(String key, String[] wut) {
         for (int o = 0; o < wut.length; o++) {
            if (wut[o].equals(key)) {
               return o;
            }
         }

         return -1;
      }

      public static long getSystemTime() {
         return Sys.getTime() * 1000L / Sys.getTimerResolution();
      }

      public static Random rand() {
         return Utils.rand;
      }

      public static double round(double n, int d) {
         if (d == 0) {
            return (double)Math.round(n);
         } else {
            double p = Math.pow(10.0, d);
            return (double)Math.round(n * p) / p;
         }
      }

      public static String str(String s) {
         char[] n = StringUtils.stripControlCodes(s).toCharArray();
         StringBuilder v = new StringBuilder();

         for (char c : n) {
            if (c < 127 && c > 20) {
               v.append(c);
            }
         }

         return v.toString();
      }

      public static String capitalizeWord(String s) {
         return s.substring(0, 1).toUpperCase() + s.substring(1);
      }

      public static String getDate() {
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
         LocalDateTime now = LocalDateTime.now();
         return dtf.format(now);
      }

      public static String joinStringList(String[] wtf, String okwaht) {
         if (wtf == null) {
            return "";
         } else if (wtf.length <= 1) {
            return "";
         } else {
            StringBuilder finalString = new StringBuilder(wtf[0]);

            for (int i = 1; i < wtf.length; i++) {
               finalString.append(okwaht).append(wtf[i]);
            }

            return finalString.toString();
         }
      }

      public static ArrayList<String> toArrayList(String[] fakeList) {
         return new ArrayList<>(Arrays.asList(fakeList));
      }

      public static List<String> StringListToList(String[] whytho) {
         List<String> howTohackNasaWorking2021NoScamDotCom = new ArrayList<>();
         Collections.addAll(howTohackNasaWorking2021NoScamDotCom, whytho);
         return howTohackNasaWorking2021NoScamDotCom;
      }

      public static JsonObject getStringAsJson(String text) {
         return new JsonParser().parse(text).getAsJsonObject();
      }

      public static String randomChoice(String[] strings) {
         return strings[Utils.rand.nextInt(strings.length)];
      }

      public static int randomInt(double inputMin, double v) {
         return (int)(Math.random() * (v - inputMin) + inputMin);
      }
   }

   public static class Modes {
      public enum BridgeMode {
         GODBRIDGE,
         MOONWALK,
         BREEZILY,
         NORMAL
      }

      public enum ClickEvents {
         RENDER,
         TICK
      }

      public enum ClickTimings {
         RAVEN,
         SKID
      }

      public enum SprintResetTimings {
         PRE,
         POST
      }
   }

   public static class Player {
      public static void hotkeyToSlot(int slot) {
         if (isPlayerInGame()) {
            Utils.mc.thePlayer.inventory.currentItem = slot;
         }
      }

      public static void sendMessageToSelf(String txt) {
         if (isPlayerInGame()) {
            String m = Utils.Client.reformat("&7[&dR&7]&r " + txt);
            Utils.mc.thePlayer.addChatMessage(new ChatComponentText(m));
         }
      }

      public static boolean isPlayerInGame() {
         return Utils.mc.thePlayer != null && Utils.mc.theWorld != null;
      }

      public static boolean isMoving() {
         return Utils.mc.thePlayer.moveForward != 0.0F || Utils.mc.thePlayer.moveStrafing != 0.0F;
      }

      public static void aim(Entity en, float ps, boolean pc) {
         if (en != null) {
            float[] t = getTargetRotations(en);
            if (t != null) {
               float y = t[0];
               float p = t[1] + 4.0F + ps;
               if (pc) {
                  Utils.mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(y, p, Utils.mc.thePlayer.onGround));
               } else {
                  Utils.mc.thePlayer.rotationYaw = y;
                  Utils.mc.thePlayer.rotationPitch = p;
               }
            }
         }
      }

      public static double fovFromEntity(Entity en) {
         return ((double)(Utils.mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0 + 540.0) % 360.0 - 180.0;
      }

      public static float fovToEntity(Entity ent) {
         double x = ent.posX - Utils.mc.thePlayer.posX;
         double z = ent.posZ - Utils.mc.thePlayer.posZ;
         double yaw = Math.atan2(x, z) * 57.2957795;
         return (float)(yaw * -1.0);
      }

      public static boolean fov(Entity entity, float fov) {
         fov = (float)((double)fov * 0.5);
         double v = ((double)(Utils.mc.thePlayer.rotationYaw - fovToEntity(entity)) % 360.0 + 540.0) % 360.0 - 180.0;
         return v > 0.0 && v < (double)fov || (double)(-fov) < v && v < 0.0;
      }

      public static double getPlayerBPS(Entity en, int d) {
         double x = en.posX - en.prevPosX;
         double z = en.posZ - en.prevPosZ;
         double sp = Math.sqrt(x * x + z * z) * 20.0;
         return Utils.Java.round(sp, d);
      }

      public static boolean playerOverAir() {
         double x = Utils.mc.thePlayer.posX;
         double y = Utils.mc.thePlayer.posY - 1.0;
         double z = Utils.mc.thePlayer.posZ;
         BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
         return Utils.mc.theWorld.isAirBlock(p);
      }

      public static boolean playerUnderBlock() {
         double x = Utils.mc.thePlayer.posX;
         double y = Utils.mc.thePlayer.posY + 2.0;
         double z = Utils.mc.thePlayer.posZ;
         BlockPos p = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
         return Utils.mc.theWorld.isBlockFullCube(p) || Utils.mc.theWorld.isBlockNormalCube(p, false);
      }

      public static int getCurrentPlayerSlot() {
         return Utils.mc.thePlayer.inventory.currentItem;
      }

      public static boolean isPlayerHoldingWeapon() {
         if (Utils.mc.thePlayer.getCurrentEquippedItem() == null) {
            return false;
         } else {
            Item item = Utils.mc.thePlayer.getCurrentEquippedItem().getItem();
            return item instanceof ItemSword || item instanceof ItemAxe;
         }
      }

      public static int getMaxDamageSlot() {
         int index = -1;
         double damage = -1.0;

         for (int slot = 0; slot <= 8; slot++) {
            ItemStack itemInSlot = Utils.mc.thePlayer.inventory.getStackInSlot(slot);
            if (itemInSlot != null) {
               for (AttributeModifier mooommHelp : itemInSlot.getAttributeModifiers().values()) {
                  if (mooommHelp.getAmount() > damage) {
                     damage = mooommHelp.getAmount();
                     index = slot;
                  }
               }
            }
         }

         return index;
      }

      public static double getSlotDamage(int slot) {
         ItemStack itemInSlot = Utils.mc.thePlayer.inventory.getStackInSlot(slot);
         if (itemInSlot == null) {
            return -1.0;
         } else {
            Iterator var2 = itemInSlot.getAttributeModifiers().values().iterator();
            if (var2.hasNext()) {
               AttributeModifier mooommHelp = (AttributeModifier)var2.next();
               return mooommHelp.getAmount();
            } else {
               return -1.0;
            }
         }
      }

      public static ArrayList<Integer> playerWearingArmor() {
         ArrayList<Integer> wearingArmor = new ArrayList<>();

         for (int armorPiece = 0; armorPiece < 4; armorPiece++) {
            if (Utils.mc.thePlayer.getCurrentArmor(armorPiece) != null) {
               if (armorPiece == 0) {
                  wearingArmor.add(3);
               } else if (armorPiece == 1) {
                  wearingArmor.add(2);
               } else if (armorPiece == 2) {
                  wearingArmor.add(1);
               } else {
                  wearingArmor.add(0);
               }
            }
         }

         return wearingArmor;
      }

      public static int getBlockAmountInCurrentStack(int currentItem) {
         if (Utils.mc.thePlayer.inventory.getStackInSlot(currentItem) == null) {
            return 0;
         } else {
            ItemStack itemStack = Utils.mc.thePlayer.inventory.getStackInSlot(currentItem);
            return itemStack.getItem() instanceof ItemBlock ? itemStack.stackSize : 0;
         }
      }

      public static boolean tryingToCombo() {
         return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
      }

      public static float[] getTargetRotations(Entity q) {
         if (q == null) {
            return null;
         } else {
            double diffX = q.posX - Utils.mc.thePlayer.posX;
            double diffY;
            if (q instanceof EntityLivingBase) {
               EntityLivingBase en = (EntityLivingBase)q;
               diffY = en.posY
                  + (double)en.getEyeHeight() * 0.9
                  - (Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight());
            } else {
               diffY = (q.getEntityBoundingBox().minY + q.getEntityBoundingBox().maxY) / 2.0
                  - (Utils.mc.thePlayer.posY + (double)Utils.mc.thePlayer.getEyeHeight());
            }

            double diffZ = q.posZ - Utils.mc.thePlayer.posZ;
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float)(-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
            return new float[]{
               Utils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - Utils.mc.thePlayer.rotationYaw),
               Utils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - Utils.mc.thePlayer.rotationPitch)
            };
         }
      }

      public static void fixMovementSpeed(double s, boolean m) {
         if (!m || isMoving()) {
            Utils.mc.thePlayer.motionX = -Math.sin(correctRotations()) * s;
            Utils.mc.thePlayer.motionZ = Math.cos(correctRotations()) * s;
         }
      }

      public static void bop(double s) {
         double forward = Utils.mc.thePlayer.movementInput.moveForward;
         double strafe = Utils.mc.thePlayer.movementInput.moveStrafe;
         float yaw = Utils.mc.thePlayer.rotationYaw;
         if (forward == 0.0 && strafe == 0.0) {
            Utils.mc.thePlayer.motionX = 0.0;
            Utils.mc.thePlayer.motionZ = 0.0;
         } else {
            if (forward != 0.0) {
               if (strafe > 0.0) {
                  yaw += (float)(forward > 0.0 ? -45 : 45);
               } else if (strafe < 0.0) {
                  yaw += (float)(forward > 0.0 ? 45 : -45);
               }

               strafe = 0.0;
               if (forward > 0.0) {
                  forward = 1.0;
               } else if (forward < 0.0) {
                  forward = -1.0;
               }
            }

            double rad = Math.toRadians(yaw + 90.0F);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);
            Utils.mc.thePlayer.motionX = forward * s * cos + strafe * s * sin;
            Utils.mc.thePlayer.motionZ = forward * s * sin - strafe * s * cos;
         }
      }

      public static float correctRotations() {
         float yw = Utils.mc.thePlayer.rotationYaw;
         if (Utils.mc.thePlayer.moveForward < 0.0F) {
            yw += 180.0F;
         }

         float f;
         if (Utils.mc.thePlayer.moveForward < 0.0F) {
            f = -0.5F;
         } else if (Utils.mc.thePlayer.moveForward > 0.0F) {
            f = 0.5F;
         } else {
            f = 1.0F;
         }

         if (Utils.mc.thePlayer.moveStrafing > 0.0F) {
            yw -= 90.0F * f;
         }

         if (Utils.mc.thePlayer.moveStrafing < 0.0F) {
            yw += 90.0F * f;
         }

         return yw * (float) (Math.PI / 180.0);
      }

      public static double pythagorasMovement() {
         return Math.sqrt(
            Utils.mc.thePlayer.motionX * Utils.mc.thePlayer.motionX
               + Utils.mc.thePlayer.motionZ * Utils.mc.thePlayer.motionZ
         );
      }

      public static void swing() {
         EntityPlayerSP p = Utils.mc.thePlayer;
         int armSwingEnd = p.isPotionActive(Potion.digSpeed)
            ? 6 - (1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier())
            : (p.isPotionActive(Potion.digSlowdown) ? 6 + (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
         if (!p.isSwingInProgress || p.swingProgressInt >= armSwingEnd / 2 || p.swingProgressInt < 0) {
            p.swingProgressInt = -1;
            p.isSwingInProgress = true;
         }
      }
   }
}
