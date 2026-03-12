package zombiecat.client.clickgui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import zombiecat.client.ZombieCat;
import zombiecat.client.clickgui.components.CategoryComponent;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Timer;

public class ClickGui extends GuiScreen {
   private Timer aE;
   private final ArrayList<CategoryComponent> categoryList = new ArrayList<>();

   public ClickGui() {
      int topOffset = 5;

      Module.ModuleCategory[] values;
      for (Module.ModuleCategory moduleCategory : values = Module.ModuleCategory.values()) {
         CategoryComponent currentModuleCategory = new CategoryComponent(moduleCategory);
         currentModuleCategory.setY(topOffset);
         this.categoryList.add(currentModuleCategory);
         topOffset += 20;
      }
   }

   public void initMain() {
      (this.aE = new Timer(500.0F)).start();
   }

   public void initGui() {
      super.initGui();
   }

   public void drawScreen(int x, int y, float p) {
      float speed = 4890.0F;

      for (CategoryComponent category : this.categoryList) {
         category.rf(this.fontRendererObj);
         category.up(x, y);

         for (Component module : category.getModules()) {
            module.update(x, y);
         }
      }

      GuiInventory.drawEntityOnScreen(
         this.width + 15 - this.aE.getValueInt(0, 40, 2),
         this.height - 19 - this.fontRendererObj.FONT_HEIGHT,
         40,
         (float)(this.width - 25 - x),
         (float)(this.height - 50 - y),
         this.mc.thePlayer
      );
   }

   public void mouseClicked(int x, int y, int mouseButton) {
      for (CategoryComponent category : this.categoryList) {
         if (category.insideArea(x, y) && !category.i(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
            category.mousePressed(true);
            category.xx = x - category.getX();
            category.yy = y - category.getY();
         }

         if (category.mousePressed(x, y) && mouseButton == 0) {
            category.setOpened(!category.isOpened());
         }

         if (category.i(x, y) && mouseButton == 0) {
            category.cv(!category.p());
         }

         if (category.isOpened() && !category.getModules().isEmpty()) {
            for (Component c : category.getModules()) {
               c.mouseDown(x, y, mouseButton);
            }
         }
      }
   }

   public void mouseReleased(int x, int y, int s) {
      if (s != 0) {
         if (ZombieCat.clientConfig != null) {
            ZombieCat.clientConfig.saveConfig();
         }
      } else {
         for (CategoryComponent c4t : this.categoryList) {
            c4t.mousePressed(false);
         }

         for (CategoryComponent c4t : this.categoryList) {
            if (c4t.isOpened() && !c4t.getModules().isEmpty()) {
               for (Component c : c4t.getModules()) {
                  c.mouseReleased(x, y, s);
               }
            }
         }
      }
   }

   public void keyTyped(char t, int k) {
      if (k == 1) {
         this.mc.displayGuiScreen(null);
      } else {
         for (CategoryComponent cat : this.categoryList) {
            if (cat.isOpened() && !cat.getModules().isEmpty()) {
               for (Component c : cat.getModules()) {
                  c.keyTyped(t, k);
               }
            }
         }
      }
   }

   public void onGuiClosed() {
      ZombieCat.configManager.save();
      ZombieCat.clientConfig.saveConfig();
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   public ArrayList<CategoryComponent> getCategoryList() {
      return this.categoryList;
   }
}
