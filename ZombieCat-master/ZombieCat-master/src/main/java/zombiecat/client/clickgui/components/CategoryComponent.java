package zombiecat.client.clickgui.components;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import zombiecat.client.ZombieCat;
import zombiecat.client.clickgui.Component;
import zombiecat.client.module.Module;
import zombiecat.client.module.modules.client.GuiModule;

public class CategoryComponent {
   public ArrayList<Component> modulesInCategory = new ArrayList<>();
   public Module.ModuleCategory categoryName;
   private boolean categoryOpened;
   private int width;
   private int y;
   private int x;
   private final int bh;
   public boolean inUse;
   public int xx;
   public int yy;
   public boolean n4m = false;
   public String pvp;
   public boolean pin = false;
   private int chromaSpeed;
   private double marginY;
   private double marginX;

   public CategoryComponent(Module.ModuleCategory category) {
      this.categoryName = category;
      this.width = 92;
      this.x = 5;
      this.y = 5;
      this.bh = 13;
      this.xx = 0;
      this.categoryOpened = false;
      this.inUse = false;
      this.chromaSpeed = 3;
      int tY = this.bh + 3;
      this.marginX = 80.0;
      this.marginY = 4.5;

      for (Module mod : ZombieCat.moduleManager.getModulesInCategory(this.categoryName)) {
         ModuleComponent b = new ModuleComponent(mod, this, tY);
         this.modulesInCategory.add(b);
         tY += 16;
      }
   }

   public ArrayList<Component> getModules() {
      return this.modulesInCategory;
   }

   public void setX(int n) {
      this.x = n;
      if (ZombieCat.clientConfig != null) {
         ZombieCat.clientConfig.saveConfig();
      }
   }

   public void setY(int y) {
      this.y = y;
      if (ZombieCat.clientConfig != null) {
         ZombieCat.clientConfig.saveConfig();
      }
   }

   public void mousePressed(boolean d) {
      this.inUse = d;
   }

   public boolean p() {
      return this.pin;
   }

   public void cv(boolean on) {
      this.pin = on;
   }

   public boolean isOpened() {
      return this.categoryOpened;
   }

   public void setOpened(boolean on) {
      this.categoryOpened = on;
      if (ZombieCat.clientConfig != null) {
         ZombieCat.clientConfig.saveConfig();
      }
   }

   public void rf(FontRenderer renderer) {
      this.width = 92;
      if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
         int categoryHeight = 0;

         for (Component moduleRenderManager : this.modulesInCategory) {
            categoryHeight += moduleRenderManager.getHeight();
         }

         Gui.drawRect(
            this.x - 1,
            this.y,
            this.x + this.width + 1,
            this.y + this.bh + categoryHeight + 4,
            new Color(0, 0, 0, (int)(GuiModule.backgroundOpacity.getValue() / 100.0 * 255.0)).getRGB()
         );
      }

      if (GuiModule.categoryBackground.getValue()) {
         BooleanComponent.renderMain((float)(this.x - 2), (float)this.y, (float)(this.x + this.width + 2), (float)(this.y + this.bh + 3), -1);
      }

      renderer.drawString(
         this.n4m ? this.pvp : this.categoryName.name(),
         (float)(this.x + 2),
         (float)(this.y + 4),
         Color.getHSBColor((float)(System.currentTimeMillis() % (7500L / (long)this.chromaSpeed)) / (7500.0F / (float)this.chromaSpeed), 1.0F, 1.0F).getRGB(),
         false
      );
      if (!this.n4m) {
         GL11.glPushMatrix();
         renderer.drawString(
            this.categoryOpened ? "-" : "+", (float)((double)this.x + this.marginX), (float)((double)this.y + this.marginY), Color.white.getRGB(), false
         );
         GL11.glPopMatrix();
         if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {
            for (Component c2 : this.modulesInCategory) {
               c2.draw();
            }
         }
      }
   }

   public void r3nd3r() {
      int o = this.bh + 3;

      for (Component c : this.modulesInCategory) {
         c.setComponentStartAt(o);
         o += c.getHeight();
      }
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public void up(int x, int y) {
      if (this.inUse) {
         this.setX(x - this.xx);
         this.setY(y - this.yy);
      }
   }

   public boolean i(int x, int y) {
      return x >= this.x + 92 - 13 && x <= this.x + this.width && (float)y >= (float)this.y + 2.0F && y <= this.y + this.bh + 1;
   }

   public boolean mousePressed(int x, int y) {
      return x >= this.x + 77 && x <= this.x + this.width - 6 && (float)y >= (float)this.y + 2.0F && y <= this.y + this.bh + 1;
   }

   public boolean insideArea(int x, int y) {
      return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
   }

   public String getName() {
      return String.valueOf(this.modulesInCategory);
   }

   public void setLocation(int parseInt, int parseInt1) {
      this.x = parseInt;
      this.y = parseInt1;
   }
}
