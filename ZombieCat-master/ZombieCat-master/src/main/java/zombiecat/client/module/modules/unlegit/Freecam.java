package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

import java.awt.*;

public class Freecam extends Module {
   public static SliderSetting a;
   public static BooleanSetting b;
   public static EntityOtherPlayerMP en = null;
   private int[] lcc = new int[]{Integer.MAX_VALUE, 0};
   private final float[] sAng = new float[]{0.0F, 0.0F};

   public Freecam() {
      super("Freecam", Module.ModuleCategory.unlegit);
      this.registerSetting(a = new SliderSetting("Speed", 2.5, 0.5, 10.0, 0.5));
      this.registerSetting(b = new BooleanSetting("Disable on damage", true));
   }

   @Override
   public void onEnable() {
      if (Utils.Player.isPlayerInGame()) {
         if (!mc.thePlayer.onGround) {
            this.disable();
         } else {
            en = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
            en.copyLocationAndAnglesFrom(mc.thePlayer);
            this.sAng[0] = en.rotationYawHead = mc.thePlayer.rotationYawHead;
            this.sAng[1] = mc.thePlayer.rotationPitch;
            en.setVelocity(0.0, 0.0, 0.0);
            en.setInvisible(true);
            mc.theWorld.addEntityToWorld(-8008, en);
            mc.setRenderViewEntity(en);
         }
      }
   }

   @Override
   public void onDisable() {
      if (Utils.Player.isPlayerInGame()) {
         if (en != null) {
            mc.setRenderViewEntity(mc.thePlayer);
            mc.thePlayer.rotationYaw = mc.thePlayer.rotationYawHead = this.sAng[0];
            mc.thePlayer.rotationPitch = this.sAng[1];
            mc.theWorld.removeEntity(en);
            en = null;
         }

         this.lcc = new int[]{Integer.MAX_VALUE, 0};
         int x = mc.thePlayer.chunkCoordX;
         int z = mc.thePlayer.chunkCoordZ;

         for (int x2 = -1; x2 <= 1; x2++) {
            for (int z2 = -1; z2 <= 1; z2++) {
               int a = x + x2;
               int b = z + z2;
               mc.theWorld.markBlockRangeForRenderUpdate(a * 16, 0, b * 16, a * 16 + 15, 256, b * 16 + 15);
            }
         }
      }
   }

   @Override
   public void update() {
      if (Utils.Player.isPlayerInGame() && en != null) {
         if (b.getValue() && mc.thePlayer.hurtTime != 0) {
            this.disable();
         } else {
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.moveForward = 0.0F;
            mc.thePlayer.moveStrafing = 0.0F;
            en.rotationYaw = en.rotationYawHead = mc.thePlayer.rotationYaw;
            en.rotationPitch = mc.thePlayer.rotationPitch;
            double s = 0.215 * a.getValue();
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
               double rad = (double)en.rotationYawHead * (Math.PI / 180.0);
               double dx = -1.0 * Math.sin(rad) * s;
               double dz = Math.cos(rad) * s;
               EntityOtherPlayerMP var10000 = en;
               var10000.posX += dx;
               var10000.posZ += dz;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
               double rad = (double)en.rotationYawHead * (Math.PI / 180.0);
               double dx = -1.0 * Math.sin(rad) * s;
               double dz = Math.cos(rad) * s;
               EntityOtherPlayerMP var10000 = en;
               var10000.posX -= dx;
               var10000.posZ -= dz;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
               double rad = (double)(en.rotationYawHead - 90.0F) * (Math.PI / 180.0);
               double dx = -1.0 * Math.sin(rad) * s;
               double dz = Math.cos(rad) * s;
               EntityOtherPlayerMP var10000 = en;
               var10000.posX += dx;
               var10000.posZ += dz;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
               double rad = (double)(en.rotationYawHead + 90.0F) * (Math.PI / 180.0);
               double dx = -1.0 * Math.sin(rad) * s;
               double dz = Math.cos(rad) * s;
               EntityOtherPlayerMP var10000 = en;
               var10000.posX += dx;
               var10000.posZ += dz;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
               EntityOtherPlayerMP var10000 = en;
               var10000.posY += 0.93 * s;
            }

            if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
               EntityOtherPlayerMP var10000 = en;
               var10000.posY -= 0.93 * s;
            }

            mc.thePlayer.setSneaking(false);
            if (this.lcc[0] != Integer.MAX_VALUE && (this.lcc[0] != en.chunkCoordX || this.lcc[1] != en.chunkCoordZ)) {
               int x = en.chunkCoordX;
               int z = en.chunkCoordZ;
               mc.theWorld.markBlockRangeForRenderUpdate(x * 16, 0, z * 16, x * 16 + 15, 256, z * 16 + 15);
            }

            this.lcc[0] = en.chunkCoordX;
            this.lcc[1] = en.chunkCoordZ;
         }
      }
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (Utils.Player.isPlayerInGame()) {
         mc.thePlayer.renderArmPitch = mc.thePlayer.prevRenderArmPitch = 700.0F;
         Utils.HUD.drawBoxAroundEntity(mc.thePlayer, false, Color.green.getRGB());
         Utils.HUD.drawBoxAroundEntity(mc.thePlayer, false, Color.green.getRGB());
      }
   }

   @SubscribeEvent
   public void m(MouseEvent e) {
      if (Utils.Player.isPlayerInGame() && e.button != -1) {
         e.setCanceled(true);
      }
   }
}
