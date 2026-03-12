package zombiecat.client.module.modules.legit;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.input.Mouse;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.ComboSetting;
import zombiecat.client.module.setting.impl.DoubleSliderSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

import java.util.Random;

public class RightClicker extends Module {
   public static SliderSetting jitterRight;
   public static BooleanSetting onlyBlocks;
   public static BooleanSetting noBlockSword;
   public static BooleanSetting ignoreRods;
   public static BooleanSetting allowEat;
   public static BooleanSetting allowBow;
   public static SliderSetting rightClickDelay;
   public static DoubleSliderSetting rightCPS;
   public static ComboSetting clickStyle;
   public static ComboSetting clickTimings;
   private Random rand = null;
   private long righti;
   private long rightj;
   private long rightk;
   private long rightl;
   private double rightm;
   private boolean rightn;
   private long lastClick;
   private long rightHold;
   private boolean rightClickWaiting;
   private double rightClickWaitStartTime;
   private boolean allowedClick;
   private boolean rightDown;

   public RightClicker() {
      super("RightClicker", Module.ModuleCategory.legit);
      this.registerSetting(rightCPS = new DoubleSliderSetting("RightCPS", 12.0, 16.0, 1.0, 60.0, 0.5));
      this.registerSetting(jitterRight = new SliderSetting("Jitter right", 0.0, 0.0, 3.0, 0.1));
      this.registerSetting(rightClickDelay = new SliderSetting("Rightclick delay (ms)", 85.0, 0.0, 500.0, 1.0));
      this.registerSetting(noBlockSword = new BooleanSetting("Don't rightclick sword", true));
      this.registerSetting(ignoreRods = new BooleanSetting("Ignore rods", true));
      this.registerSetting(onlyBlocks = new BooleanSetting("Only rightclick with blocks", false));
      this.registerSetting(allowEat = new BooleanSetting("Allow eat & drink", true));
      this.registerSetting(allowBow = new BooleanSetting("Allow bow", true));
      this.registerSetting(clickTimings = new ComboSetting<>("Click event", RightClicker.ClickEvent.Render));
      this.registerSetting(clickStyle = new ComboSetting<>("Click Style", RightClicker.ClickStyle.Raven));
      this.rightClickWaiting = false;
   }

   @Override
   public void onEnable() {
      this.rightClickWaiting = false;
      this.allowedClick = false;
      this.rand = new Random();
   }

   @Override
   public void onDisable() {
      this.rightClickWaiting = false;
   }

   @SubscribeEvent
   public void onRenderTick(RenderTickEvent ev) {
      if (Utils.Client.currentScreenMinecraft()
         || Minecraft.getMinecraft().currentScreen instanceof GuiInventory
         || Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
         if (clickTimings.getMode() == RightClicker.ClickEvent.Render) {
            if (clickStyle.getMode() == RightClicker.ClickStyle.Raven) {
               this.ravenClick();
            } else if (clickStyle.getMode() == RightClicker.ClickStyle.SKid) {
               this.skidClick();
            }
         }
      }
   }

   @SubscribeEvent
   public void onTick(PlayerTickEvent ev) {
      if (Utils.Client.currentScreenMinecraft()
         || Minecraft.getMinecraft().currentScreen instanceof GuiInventory
         || Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
         if (clickTimings.getMode() == RightClicker.ClickEvent.Tick) {
            if (clickStyle.getMode() == RightClicker.ClickStyle.Raven) {
               this.ravenClick();
            } else if (clickStyle.getMode() == RightClicker.ClickStyle.SKid) {
               this.skidClick();
            }
         }
      }
   }

   private void skidClick() {
      if (Utils.Player.isPlayerInGame()) {
         if (mc.currentScreen == null && mc.inGameHasFocus) {
            double speedRight = 1.0 / ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.2, rightCPS.getInputMax());
            double rightHoldLength = speedRight / ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.02, rightCPS.getInputMax());
            if (!Mouse.isButtonDown(1) && !this.rightDown) {
               KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
               Utils.Client.setMouseButtonState(1, false);
            }

            if (!Mouse.isButtonDown(1) && !this.rightDown) {
               if (!Mouse.isButtonDown(1)) {
                  this.rightClickWaiting = false;
                  this.allowedClick = false;
               }
            } else {
               if (!this.rightClickAllowed()) {
                  return;
               }

               if (jitterRight.getValue() > 0.0) {
                  double jitterMultiplier = jitterRight.getValue() * 0.45;
                  if (this.rand.nextBoolean()) {
                     EntityPlayerSP entityPlayer = mc.thePlayer;
                     entityPlayer.rotationYaw = (float)((double)entityPlayer.rotationYaw + (double)this.rand.nextFloat() * jitterMultiplier);
                  } else {
                     EntityPlayerSP entityPlayer = mc.thePlayer;
                     entityPlayer.rotationYaw = (float)((double)entityPlayer.rotationYaw - (double)this.rand.nextFloat() * jitterMultiplier);
                  }

                  if (this.rand.nextBoolean()) {
                     EntityPlayerSP var10 = mc.thePlayer;
                     var10.rotationPitch = (float)((double)var10.rotationPitch + (double)this.rand.nextFloat() * jitterMultiplier * 0.45);
                  } else {
                     EntityPlayerSP var11 = mc.thePlayer;
                     var11.rotationPitch = (float)((double)var11.rotationPitch - (double)this.rand.nextFloat() * jitterMultiplier * 0.45);
                  }
               }

               if ((double)(System.currentTimeMillis() - this.lastClick) > speedRight * 1000.0) {
                  this.lastClick = System.currentTimeMillis();
                  if (this.rightHold < this.lastClick) {
                     this.rightHold = this.lastClick;
                  }

                  int key = mc.gameSettings.keyBindUseItem.getKeyCode();
                  KeyBinding.setKeyBindState(key, true);
                  Utils.Client.setMouseButtonState(1, true);
                  KeyBinding.onTick(key);
                  this.rightDown = false;
               } else if ((double)(System.currentTimeMillis() - this.rightHold) > rightHoldLength * 1000.0) {
                  this.rightDown = true;
                  KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                  Utils.Client.setMouseButtonState(1, false);
               }
            }
         }
      }
   }

   private void ravenClick() {
      if (Utils.Player.isPlayerInGame()) {
         if (mc.currentScreen == null && mc.inGameHasFocus) {
            Mouse.poll();
            if (Mouse.isButtonDown(1)) {
               this.rightClickExecute(mc.gameSettings.keyBindUseItem.getKeyCode());
            } else if (!Mouse.isButtonDown(1)) {
               this.rightClickWaiting = false;
               this.allowedClick = false;
               this.righti = 0L;
               this.rightj = 0L;
            }
         }
      }
   }

   public boolean rightClickAllowed() {
      ItemStack item = mc.thePlayer.getHeldItem();
      if (item != null) {
         if (allowEat.getValue()
            && (item.getItem() instanceof ItemFood || item.getItem() instanceof ItemPotion || item.getItem() instanceof ItemBucketMilk)) {
            return false;
         }

         if (ignoreRods.getValue() && item.getItem() instanceof ItemFishingRod) {
            return false;
         }

         if (allowBow.getValue() && item.getItem() instanceof ItemBow) {
            return false;
         }

         if (onlyBlocks.getValue() && !(item.getItem() instanceof ItemBlock)) {
            return false;
         }

         if (noBlockSword.getValue() && item.getItem() instanceof ItemSword) {
            return false;
         }
      }

      if (rightClickDelay.getValue() != 0.0) {
         if (!this.rightClickWaiting && !this.allowedClick) {
            this.rightClickWaitStartTime = (double)System.currentTimeMillis();
            this.rightClickWaiting = true;
            return false;
         }

         if (this.rightClickWaiting && !this.allowedClick) {
            double passedTime = (double)System.currentTimeMillis() - this.rightClickWaitStartTime;
            if (passedTime >= rightClickDelay.getValue()) {
               this.allowedClick = true;
               this.rightClickWaiting = false;
               return true;
            }

            return false;
         }
      }

      return true;
   }

   public void rightClickExecute(int key) {
      if (this.rightClickAllowed()) {
         if (jitterRight.getValue() > 0.0) {
            double jitterMultiplier = jitterRight.getValue() * 0.45;
            if (this.rand.nextBoolean()) {
               EntityPlayerSP entityPlayer = mc.thePlayer;
               entityPlayer.rotationYaw = (float)((double)entityPlayer.rotationYaw + (double)this.rand.nextFloat() * jitterMultiplier);
            } else {
               EntityPlayerSP entityPlayer = mc.thePlayer;
               entityPlayer.rotationYaw = (float)((double)entityPlayer.rotationYaw - (double)this.rand.nextFloat() * jitterMultiplier);
            }

            if (this.rand.nextBoolean()) {
               EntityPlayerSP var6 = mc.thePlayer;
               var6.rotationPitch = (float)((double)var6.rotationPitch + (double)this.rand.nextFloat() * jitterMultiplier * 0.45);
            } else {
               EntityPlayerSP var7 = mc.thePlayer;
               var7.rotationPitch = (float)((double)var7.rotationPitch - (double)this.rand.nextFloat() * jitterMultiplier * 0.45);
            }
         }

         if (this.rightj > 0L && this.righti > 0L) {
            if (System.currentTimeMillis() > this.rightj) {
               KeyBinding.setKeyBindState(key, true);
               KeyBinding.onTick(key);
               Utils.Client.setMouseButtonState(1, false);
               Utils.Client.setMouseButtonState(1, true);
               this.genRightTimings();
            } else if (System.currentTimeMillis() > this.righti) {
               KeyBinding.setKeyBindState(key, false);
            }
         } else {
            this.genRightTimings();
         }
      }
   }

   public void genRightTimings() {
      double clickSpeed = Utils.Client.ranModuleVal(rightCPS, this.rand) + 0.4 * this.rand.nextDouble();
      long delay = (long)((int)Math.round(1000.0 / clickSpeed));
      if (System.currentTimeMillis() > this.rightk) {
         if (!this.rightn && this.rand.nextInt(100) >= 85) {
            this.rightn = true;
            this.rightm = 1.1 + this.rand.nextDouble() * 0.15;
         } else {
            this.rightn = false;
         }

         this.rightk = System.currentTimeMillis() + 500L + (long)this.rand.nextInt(1500);
      }

      if (this.rightn) {
         delay = (long)((double)delay * this.rightm);
      }

      if (System.currentTimeMillis() > this.rightl) {
         if (this.rand.nextInt(100) >= 80) {
            delay += 50L + (long)this.rand.nextInt(100);
         }

         this.rightl = System.currentTimeMillis() + 500L + (long)this.rand.nextInt(1500);
      }

      this.rightj = System.currentTimeMillis() + delay;
      this.righti = System.currentTimeMillis() + delay / 2L - (long)this.rand.nextInt(10);
   }

   public static enum ClickEvent {
      Tick,
      Render;
   }

   public static enum ClickStyle {
      Raven,
      SKid;
   }
}
