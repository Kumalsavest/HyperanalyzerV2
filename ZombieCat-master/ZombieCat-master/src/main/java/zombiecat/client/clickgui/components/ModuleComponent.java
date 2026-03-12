package zombiecat.client.clickgui.components;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import zombiecat.client.clickgui.Component;
import zombiecat.client.module.Module;
import zombiecat.client.module.modules.client.GuiModule;
import zombiecat.client.module.setting.Setting;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.ComboSetting;
import zombiecat.client.module.setting.impl.DescriptionSetting;
import zombiecat.client.module.setting.impl.DoubleSliderSetting;
import zombiecat.client.module.setting.impl.SliderSetting;

public class ModuleComponent implements Component {
   private final int c1 = new Color(0, 85, 255).getRGB();
   private final int c2 = new Color(154, 2, 255).getRGB();
   private final int c3 = new Color(175, 143, 233).getRGB();
   public Module mod;
   public CategoryComponent category;
   public int o;
   private final ArrayList<Component> settings;
   public boolean po;

   public ModuleComponent(Module mod, CategoryComponent p, int o) {
      this.mod = mod;
      this.category = p;
      this.o = o;
      this.settings = new ArrayList<>();
      this.po = false;
      int y = o + 12;
      if (!mod.getSettings().isEmpty()) {
         for (Setting v : mod.getSettings()) {
            if (v instanceof SliderSetting) {
               SliderSetting n = (SliderSetting)v;
               SliderComponent s = new SliderComponent(n, this, y);
               this.settings.add(s);
               y += 16;
            } else if (v instanceof BooleanSetting) {
               BooleanSetting b = (BooleanSetting)v;
               BooleanComponent c = new BooleanComponent(mod, b, this, y);
               this.settings.add(c);
               y += 12;
            } else if (v instanceof DescriptionSetting) {
               DescriptionSetting d = (DescriptionSetting)v;
               DescriptionComponent m = new DescriptionComponent(d, this, y);
               this.settings.add(m);
               y += 12;
            } else if (v instanceof DoubleSliderSetting) {
               DoubleSliderSetting n = (DoubleSliderSetting)v;
               RangeSliderComponent s = new RangeSliderComponent(n, this, y);
               this.settings.add(s);
               y += 16;
            } else if (v instanceof ComboSetting) {
               ComboSetting n = (ComboSetting)v;
               ModeComponent s = new ModeComponent(n, this, y);
               this.settings.add(s);
               y += 12;
            }
         }
      }

      this.settings.add(new BindComponent(this, y));
   }

   @Override
   public void setComponentStartAt(int n) {
      this.o = n;
      int y = this.o + 16;

      for (Component c : this.settings) {
         c.setComponentStartAt(y);
         if (c instanceof SliderComponent || c instanceof RangeSliderComponent) {
            y += 16;
         } else if (c instanceof BooleanComponent || c instanceof DescriptionComponent || c instanceof ModeComponent || c instanceof BindComponent) {
            y += 12;
         }
      }
   }

   public static void e() {
      GL11.glDisable(2929);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glDepthMask(true);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
   }

   public static void f() {
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glEnable(2929);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
      GL11.glEdgeFlag(true);
   }

   public static void g(int h) {
      float a = 0.0F;
      float r = 0.0F;
      float g = 0.0F;
      float b = 0.0F;
      if (GuiModule.guiTheme.getValue() == 1.0) {
         a = (float)(h >> 14 & 0xFF) / 255.0F;
         r = (float)(h >> 5 & 0xFF) / 255.0F;
         g = (float)(h >> 5 & 0xFF) / 2155.0F;
         b = (float)(h & 0xFF);
      } else if (GuiModule.guiTheme.getValue() == 2.0) {
         a = (float)(h >> 14 & 0xFF) / 255.0F;
         r = (float)(h >> 5 & 0xFF) / 2155.0F;
         g = (float)(h >> 5 & 0xFF) / 255.0F;
         b = (float)(h & 0xFF);
      }

      GL11.glColor4f(r, g, b, a);
   }

   public static void v(float x, float y, float x1, float y1, int t, int b) {
      e();
      GL11.glShadeModel(7425);
      GL11.glBegin(7);
      g(t);
      GL11.glVertex2f(x, y1);
      GL11.glVertex2f(x1, y1);
      g(b);
      GL11.glVertex2f(x1, y);
      GL11.glVertex2f(x, y);
      GL11.glEnd();
      GL11.glShadeModel(7424);
      f();
   }

   @Override
   public void draw() {
      v(
         (float)this.category.getX(),
         (float)(this.category.getY() + this.o),
         (float)(this.category.getX() + this.category.getWidth()),
         (float)(this.category.getY() + 15 + this.o),
         this.mod.isOn() ? this.c2 : -12829381,
         this.mod.isOn() ? this.c2 : -12302777
      );
      GL11.glPushMatrix();
      int button_rgb;
      switch ((int)GuiModule.guiTheme.getValue()) {
         case 3:
            if (this.mod.isOn()) {
               button_rgb = this.c1;
            } else if (this.mod.canBeEnabled()) {
               button_rgb = Color.lightGray.getRGB();
            } else {
               button_rgb = new Color(102, 102, 102).getRGB();
            }
            break;
         case 4:
            if (this.mod.isOn()) {
               button_rgb = this.c3;
            } else if (this.mod.canBeEnabled()) {
               button_rgb = Color.lightGray.getRGB();
            } else {
               button_rgb = new Color(102, 102, 102).getRGB();
            }
            break;
         default:
            if (this.mod.canBeEnabled()) {
               button_rgb = Color.lightGray.getRGB();
            } else {
               button_rgb = new Color(102, 102, 102).getRGB();
            }
      }

      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(
            this.mod.getName(),
            (float)(this.category.getX() + this.category.getWidth() / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.mod.getName()) / 2),
            (float)(this.category.getY() + this.o + 4),
            button_rgb
         );
      GL11.glPopMatrix();
      if (this.po && !this.settings.isEmpty()) {
         for (Component c : this.settings) {
            c.draw();
         }
      }
   }

   @Override
   public int getHeight() {
      if (!this.po) {
         return 16;
      } else {
         int h = 16;

         for (Component c : this.settings) {
            if (c instanceof SliderComponent || c instanceof RangeSliderComponent) {
               h += 16;
            } else if (c instanceof BooleanComponent || c instanceof DescriptionComponent || c instanceof ModeComponent || c instanceof BindComponent) {
               h += 12;
            }
         }

         return h;
      }
   }

   @Override
   public void update(int mousePosX, int mousePosY) {
      if (!this.settings.isEmpty()) {
         for (Component c : this.settings) {
            c.update(mousePosX, mousePosY);
         }
      }
   }

   @Override
   public void mouseDown(int x, int y, int b) {
      if (this.mod.canBeEnabled() && this.ii(x, y) && b == 0) {
         this.mod.toggle();
      }

      if (this.ii(x, y) && b == 1) {
         this.po = !this.po;
         this.category.r3nd3r();
      }

      for (Component c : this.settings) {
         c.mouseDown(x, y, b);
      }
   }

   @Override
   public void mouseReleased(int x, int y, int m) {
      for (Component c : this.settings) {
         c.mouseReleased(x, y, m);
      }
   }

   @Override
   public void keyTyped(char t, int k) {
      for (Component c : this.settings) {
         c.keyTyped(t, k);
      }
   }

   public boolean ii(int x, int y) {
      return x > this.category.getX()
         && x < this.category.getX() + this.category.getWidth()
         && y > this.category.getY() + this.o
         && y < this.category.getY() + 16 + this.o;
   }
}
