package zombiecat.client.clickgui.components;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import zombiecat.client.clickgui.Component;
import zombiecat.client.module.Module;
import zombiecat.client.module.modules.client.GuiModule;
import zombiecat.client.module.setting.impl.BooleanSetting;

public class BooleanComponent implements Component {
   private final int c = new Color(20, 255, 0).getRGB();
   private final int boxC = new Color(169, 169, 169).getRGB();
   private final Module mod;
   private final BooleanSetting cl1ckbUtt0n;
   private final ModuleComponent module;
   private int o;
   private int x;
   private int y;
   private int boxSize = 6;

   public BooleanComponent(Module mod, BooleanSetting op, ModuleComponent b, int o) {
      this.mod = mod;
      this.cl1ckbUtt0n = op;
      this.module = b;
      this.x = b.category.getX() + b.category.getWidth();
      this.y = b.category.getY() + b.o;
      this.o = o;
   }

   public static void e() {
      GL11.glDisable(2929);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glDepthMask(true);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
   }

   public static void renderMain() {
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
   }

   public static void renderMain(float x, float y, float x1, float y1, int c) {
      e();
      colour(c);
      renderMain(x, y, x1, y1);
      renderMain();
   }

   public static void renderMain(float x, float y, float x1, float y1) {
      GL11.glBegin(7);
      GL11.glVertex2f(x, y1);
      GL11.glVertex2f(x1, y1);
      GL11.glVertex2f(x1, y);
      GL11.glVertex2f(x, y);
      GL11.glEnd();
   }

   public static void colour(int h) {
      float a1pha = (float)(h >> 24 & 0xFF) / 350.0F;
      GL11.glColor4f(0.0F, 0.0F, 0.0F, a1pha);
   }

   @Override
   public void draw() {
      if (GuiModule.guiTheme.getValue() == 4.0) {
         Gui.drawRect(
            this.module.category.getX() + 4,
            this.module.category.getY() + this.o + 4,
            this.module.category.getX() + 4 + this.boxSize,
            this.module.category.getY() + this.o + 4 + this.boxSize,
            this.boxC
         );
         if (this.cl1ckbUtt0n.getValue()) {
            Gui.drawRect(
               this.module.category.getX() + 5,
               this.module.category.getY() + this.o + 5,
               this.module.category.getX() + 5 + this.boxSize - 2,
               this.module.category.getY() + this.o + 5 + this.boxSize - 2,
               this.c
            );
         }
      }

      GL11.glPushMatrix();
      GL11.glScaled(0.5, 0.5, 0.5);
      if (GuiModule.guiTheme.getValue() == 4.0) {
         Minecraft.getMinecraft()
            .fontRendererObj
            .drawString(
               this.cl1ckbUtt0n.getValue() ? "     " + this.cl1ckbUtt0n.getName() : "     " + this.cl1ckbUtt0n.getName(),
               (float)((this.module.category.getX() + 4) * 2),
               (float)((this.module.category.getY() + this.o + 5) * 2),
               this.cl1ckbUtt0n.getValue() ? this.c : -1,
               false
            );
      } else {
         Minecraft.getMinecraft()
            .fontRendererObj
            .drawString(
               this.cl1ckbUtt0n.getValue() ? "[+]  " + this.cl1ckbUtt0n.getName() : "[-]  " + this.cl1ckbUtt0n.getName(),
               (float)((this.module.category.getX() + 4) * 2),
               (float)((this.module.category.getY() + this.o + 5) * 2),
               this.cl1ckbUtt0n.getValue() ? this.c : -1,
               false
            );
      }

      GL11.glPopMatrix();
   }

   @Override
   public void setComponentStartAt(int n) {
      this.o = n;
   }

   @Override
   public int getHeight() {
      return 0;
   }

   @Override
   public void update(int mousePosX, int mousePosY) {
      this.y = this.module.category.getY() + this.o;
      this.x = this.module.category.getX();
   }

   @Override
   public void mouseDown(int x, int y, int b) {
      if (this.i(x, y) && b == 0 && this.module.po) {
         this.cl1ckbUtt0n.toggle();
         this.mod.guiButtonToggled(this.cl1ckbUtt0n);
      }
   }

   @Override
   public void mouseReleased(int x, int y, int m) {
   }

   @Override
   public void keyTyped(char t, int k) {
   }

   public boolean i(int x, int y) {
      return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 11;
   }
}
