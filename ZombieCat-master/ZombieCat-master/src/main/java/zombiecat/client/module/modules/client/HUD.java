package zombiecat.client.module.modules.client;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import zombiecat.client.ZombieCat;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.DescriptionSetting;
import zombiecat.client.utils.Utils;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {
   public static BooleanSetting editPosition;
   public static BooleanSetting dropShadow;
   public static BooleanSetting alphabeticalSort;
   public static DescriptionSetting colourModeDesc;
   private static int hudX = 5;
   private static int hudY = 70;
   public static Utils.PositionMode positionMode;
   public static boolean showedError;
   public static final String HUDX_prefix = "HUDX~ ";
   public static final String HUDY_prefix = "HUDY~ ";

   public HUD() {
      super("HUD", Module.ModuleCategory.client);
      this.registerSetting(editPosition = new BooleanSetting("Edit position", false));
      this.registerSetting(dropShadow = new BooleanSetting("Drop shadow", true));
      this.registerSetting(alphabeticalSort = new BooleanSetting("Alphabetical sort", false));
      this.registerSetting(colourModeDesc = new DescriptionSetting("Mode: RAVEN"));
      showedError = false;
   }

   @Override
   public void guiUpdate() {
   }

   @Override
   public void onEnable() {
      ZombieCat.moduleManager.sort();
   }

   @Override
   public void guiButtonToggled(BooleanSetting b) {
      if (b == editPosition) {
         editPosition.disable();
         mc.displayGuiScreen(new HUD.EditHudPositionScreen());
      } else if (b == alphabeticalSort) {
         ZombieCat.moduleManager.sort();
      }
   }

   @SubscribeEvent
   public void a(RenderTickEvent ev) {
      if (ev.phase == Phase.END && Utils.Player.isPlayerInGame()) {
         if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
         }

         int margin = 2;
         int y = hudY;
         int del = 0;
         if (!alphabeticalSort.getValue()) {
            if (positionMode == Utils.PositionMode.UPLEFT || positionMode == Utils.PositionMode.UPRIGHT) {
               ZombieCat.moduleManager.sortShortLong();
            } else if (positionMode == Utils.PositionMode.DOWNLEFT || positionMode == Utils.PositionMode.DOWNRIGHT) {
               ZombieCat.moduleManager.sortLongShort();
            }
         }

         List<Module> en = new ArrayList<>(ZombieCat.moduleManager.getModules());
         if (en.isEmpty()) {
            return;
         }

         int textBoxWidth = ZombieCat.moduleManager.getLongestActiveModule(mc.fontRendererObj);
         int textBoxHeight = ZombieCat.moduleManager.getBoxHeight(mc.fontRendererObj, margin);
         if (hudX < 0) {
            hudX = margin;
         }

         if (hudY < 0) {
            hudY = margin;
         }

         if (hudX + textBoxWidth > mc.displayWidth / 2) {
            hudX = mc.displayWidth / 2 - textBoxWidth - margin;
         }

         if (hudY + textBoxHeight > mc.displayHeight / 2) {
            hudY = mc.displayHeight / 2 - textBoxHeight;
         }

         for (Module m : en) {
            if (m.isOn() && m != this) {
               if (positionMode != Utils.PositionMode.DOWNRIGHT && positionMode != Utils.PositionMode.UPRIGHT) {
                  mc.fontRendererObj.drawString(m.getName(), (float)hudX, (float)y, Utils.Client.rainbow((long)del), dropShadow.getValue());
                  y += mc.fontRendererObj.FONT_HEIGHT + margin;
                  del++;
               } else {
                  mc.fontRendererObj
                     .drawString(
                        m.getName(),
                        (float)hudX + (float)(textBoxWidth - mc.fontRendererObj.getStringWidth(m.getName())),
                        (float)y,
                        Utils.Client.rainbow((long)del),
                        dropShadow.getValue()
                     );
                  y += mc.fontRendererObj.FONT_HEIGHT + margin;
                  del++;
               }
            }
         }
      }
   }

   public static int getHudX() {
      return hudX;
   }

   public static int getHudY() {
      return hudY;
   }

   public static void setHudX(int hudX) {
      HUD.hudX = hudX;
   }

   public static void setHudY(int hudY) {
      HUD.hudY = hudY;
   }

   public static enum ColourModes {
      Rainbow;
   }

   static class EditHudPositionScreen extends GuiScreen {
      final String hudTextExample = "This is an-Example-HUD";
      GuiButtonExt resetPosButton;
      boolean mouseDown = false;
      int textBoxStartX = 0;
      int textBoxStartY = 0;
      ScaledResolution sr;
      int textBoxEndX = 0;
      int textBoxEndY = 0;
      int marginX = 5;
      int marginY = 70;
      int lastMousePosX = 0;
      int lastMousePosY = 0;
      int sessionMousePosX = 0;
      int sessionMousePosY = 0;

      public void initGui() {
         super.initGui();
         this.buttonList.add(this.resetPosButton = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
         this.marginX = HUD.hudX;
         this.marginY = HUD.hudY;
         this.sr = new ScaledResolution(this.mc);
         HUD.positionMode = Utils.HUD.getPostitionMode(this.marginX, this.marginY, (double)this.sr.getScaledWidth(), (double)this.sr.getScaledHeight());
      }

      public void drawScreen(int mX, int mY, float pt) {
         drawRect(0, 0, this.width, this.height, -1308622848);
         drawRect(0, this.height / 2, this.width, this.height / 2 + 1, -1724499649);
         drawRect(this.width / 2, 0, this.width / 2 + 1, this.height, -1724499649);
         int textBoxStartX = this.marginX;
         int textBoxStartY = this.marginY;
         int textBoxEndX = textBoxStartX + 50;
         int textBoxEndY = textBoxStartY + 32;
         this.drawArrayList(this.mc.fontRendererObj, "This is an-Example-HUD");
         this.textBoxStartX = textBoxStartX;
         this.textBoxStartY = textBoxStartY;
         this.textBoxEndX = textBoxEndX;
         this.textBoxEndY = textBoxEndY;
         HUD.hudX = textBoxStartX;
         HUD.hudY = textBoxStartY;
         ScaledResolution res = new ScaledResolution(this.mc);
         int descriptionOffsetX = res.getScaledWidth() / 2 - 84;
         int descriptionOffsetY = res.getScaledHeight() / 2 - 20;
         Utils.HUD.drawColouredText(
            "Edit the HUD position by dragging.", '-', descriptionOffsetX, descriptionOffsetY, 2L, 0L, true, this.mc.fontRendererObj
         );

         try {
            this.handleInput();
         } catch (IOException var12) {
         }

         super.drawScreen(mX, mY, pt);
      }

      private void drawArrayList(FontRenderer fr, String t) {
         int x = this.textBoxStartX;
         int gap = this.textBoxEndX - this.textBoxStartX;
         int y = this.textBoxStartY;
         double marginY = (double)(fr.FONT_HEIGHT + 2);
         String[] var4 = t.split("-");
         ArrayList<String> var5 = Utils.Java.toArrayList(var4);
         if (HUD.positionMode == Utils.PositionMode.UPLEFT || HUD.positionMode == Utils.PositionMode.UPRIGHT) {
            var5.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2) - Utils.mc.fontRendererObj.getStringWidth(o1));
         } else if (HUD.positionMode == Utils.PositionMode.DOWNLEFT || HUD.positionMode == Utils.PositionMode.DOWNRIGHT) {
            var5.sort(Comparator.comparingInt(o2 -> Utils.mc.fontRendererObj.getStringWidth(o2)));
         }

         if (HUD.positionMode != Utils.PositionMode.DOWNRIGHT && HUD.positionMode != Utils.PositionMode.UPRIGHT) {
            for (String s : var5) {
               fr.drawString(s, (float)x, (float)y, Color.white.getRGB(), HUD.dropShadow.getValue());
               y = (int)((double)y + marginY);
            }
         } else {
            for (String s : var5) {
               fr.drawString(s, (float)x + (float)(gap - fr.getStringWidth(s)), (float)y, Color.white.getRGB(), HUD.dropShadow.getValue());
               y = (int)((double)y + marginY);
            }
         }
      }

      protected void mouseClickMove(int mousePosX, int mousePosY, int clickedMouseButton, long timeSinceLastClick) {
         super.mouseClickMove(mousePosX, mousePosY, clickedMouseButton, timeSinceLastClick);
         if (clickedMouseButton == 0) {
            if (this.mouseDown) {
               this.marginX = this.lastMousePosX + (mousePosX - this.sessionMousePosX);
               this.marginY = this.lastMousePosY + (mousePosY - this.sessionMousePosY);
               this.sr = new ScaledResolution(this.mc);
               HUD.positionMode = Utils.HUD.getPostitionMode(this.marginX, this.marginY, (double)this.sr.getScaledWidth(), (double)this.sr.getScaledHeight());
            } else if (mousePosX > this.textBoxStartX && mousePosX < this.textBoxEndX && mousePosY > this.textBoxStartY && mousePosY < this.textBoxEndY) {
               this.mouseDown = true;
               this.sessionMousePosX = mousePosX;
               this.sessionMousePosY = mousePosY;
               this.lastMousePosX = this.marginX;
               this.lastMousePosY = this.marginY;
            }
         }
      }

      protected void mouseReleased(int mX, int mY, int state) {
         super.mouseReleased(mX, mY, state);
         if (state == 0) {
            this.mouseDown = false;
         }
      }

      public void actionPerformed(GuiButton b) {
         if (b == this.resetPosButton) {
            this.marginX = HUD.hudX = 5;
            this.marginY = HUD.hudY = 70;
         }
      }

      public boolean doesGuiPauseGame() {
         return false;
      }
   }
}
