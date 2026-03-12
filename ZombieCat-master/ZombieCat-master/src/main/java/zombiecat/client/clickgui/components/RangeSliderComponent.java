package zombiecat.client.clickgui.components;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import zombiecat.client.clickgui.Component;
import zombiecat.client.module.setting.impl.DoubleSliderSetting;
import zombiecat.client.utils.Utils;

public class RangeSliderComponent implements Component {
   private final DoubleSliderSetting doubleSlider;
   private final ModuleComponent module;
   private double barWidth;
   private double blankWidth;
   private int sliderStartX;
   private int sliderStartY;
   private int moduleStartY;
   private boolean mouseDown;
   private boolean inMotion;
   private RangeSliderComponent.Helping mode = RangeSliderComponent.Helping.NONE;
   private final int boxMargin = 4;
   private final int boxHeight = 4;
   private final int textSize = 11;

   public RangeSliderComponent(DoubleSliderSetting doubleSlider, ModuleComponent module, int moduleStartY) {
      this.doubleSlider = doubleSlider;
      this.module = module;
      this.sliderStartX = this.module.category.getX() + 4;
      this.sliderStartY = moduleStartY + module.category.getY();
      this.moduleStartY = moduleStartY;
   }

   @Override
   public void draw() {
      Gui.drawRect(
         this.module.category.getX() + 4,
         this.module.category.getY() + this.moduleStartY + 11,
         this.module.category.getX() - 4 + this.module.category.getWidth(),
         this.module.category.getY() + this.moduleStartY + 11 + 4,
         -12302777
      );
      int startToDrawFrom = this.module.category.getX() + 4 + (int)this.blankWidth;
      int finishDrawingAt = startToDrawFrom + (int)this.barWidth;
      int middleThing = (int)Utils.Java.round(this.barWidth / 2.0, 0) + this.module.category.getX() + (int)this.blankWidth + 4 - 1;
      Gui.drawRect(
         startToDrawFrom,
         this.module.category.getY() + this.moduleStartY + 11,
         finishDrawingAt,
         this.module.category.getY() + this.moduleStartY + 11 + 4,
         Utils.Client.astolfoColorsDraw(14, 10)
      );
      Gui.drawRect(
         middleThing,
         this.module.category.getY() + this.moduleStartY + 11 - 1,
         middleThing + (middleThing % 2 == 0 ? 2 : 1),
         this.module.category.getY() + this.moduleStartY + 11 + 4 + 1,
         -14869217
      );
      GL11.glPushMatrix();
      GL11.glScaled(0.5, 0.5, 0.5);
      Minecraft.getMinecraft()
         .fontRendererObj
         .drawStringWithShadow(
            this.doubleSlider.getName() + ": " + this.doubleSlider.getInputMin() + ", " + this.doubleSlider.getInputMax(),
            (float)((int)((float)(this.module.category.getX() + 4) * 2.0F)),
            (float)((int)((float)(this.module.category.getY() + this.moduleStartY + 3) * 2.0F)),
            -1
         );
      GL11.glPopMatrix();
   }

   @Override
   public void setComponentStartAt(int posY) {
      this.moduleStartY = posY;
   }

   @Override
   public int getHeight() {
      return 0;
   }

   @Override
   public void update(int mousePosX, int mousePosY) {
      this.sliderStartY = this.module.category.getY() + this.moduleStartY;
      this.sliderStartX = this.module.category.getX() + 4;
      double mousePressedAt = (double)Math.min(this.module.category.getWidth() - 8, Math.max(0, mousePosX - this.sliderStartX));
      this.blankWidth = (double)(this.module.category.getWidth() - 8)
         * (this.doubleSlider.getInputMin() - this.doubleSlider.getMin())
         / (this.doubleSlider.getMax() - this.doubleSlider.getMin());
      this.barWidth = (double)(this.module.category.getWidth() - 8)
         * (this.doubleSlider.getInputMax() - this.doubleSlider.getInputMin())
         / (this.doubleSlider.getMax() - this.doubleSlider.getMin());
      if (this.mouseDown) {
         if (mousePressedAt > this.blankWidth + this.barWidth / 2.0 || this.mode == RangeSliderComponent.Helping.MAX) {
            if (this.mode == RangeSliderComponent.Helping.NONE) {
               this.mode = RangeSliderComponent.Helping.MAX;
            }

            if (this.mode == RangeSliderComponent.Helping.MAX) {
               if (mousePressedAt <= this.blankWidth) {
                  this.doubleSlider.setValueMax(this.doubleSlider.getInputMin());
               } else {
                  double n = r(
                     mousePressedAt / (double)(this.module.category.getWidth() - 8) * (this.doubleSlider.getMax() - this.doubleSlider.getMin())
                        + this.doubleSlider.getMin(),
                     2
                  );
                  this.doubleSlider.setValueMax(n);
               }
            }
         }

         if (mousePressedAt < this.blankWidth + this.barWidth / 2.0 || this.mode == RangeSliderComponent.Helping.MIN) {
            if (this.mode == RangeSliderComponent.Helping.NONE) {
               this.mode = RangeSliderComponent.Helping.MIN;
            }

            if (this.mode == RangeSliderComponent.Helping.MIN) {
               if (mousePressedAt == 0.0) {
                  this.doubleSlider.setValueMin(this.doubleSlider.getMin());
               } else if (mousePressedAt >= this.barWidth + this.blankWidth) {
                  this.doubleSlider.setValueMin(this.doubleSlider.getMax());
               } else {
                  double n = r(
                     mousePressedAt / (double)(this.module.category.getWidth() - 8) * (this.doubleSlider.getMax() - this.doubleSlider.getMin())
                        + this.doubleSlider.getMin(),
                     2
                  );
                  this.doubleSlider.setValueMin(n);
               }
            }
         }
      } else if (this.mode != RangeSliderComponent.Helping.NONE) {
         this.mode = RangeSliderComponent.Helping.NONE;
      }
   }

   private static double r(double v, int p) {
      if (p < 0) {
         return 0.0;
      } else {
         BigDecimal bd = new BigDecimal(v);
         bd = bd.setScale(p, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }

   @Override
   public void mouseDown(int x, int y, int b) {
      if (this.u(x, y) && b == 0 && this.module.po) {
         this.mouseDown = true;
      }

      if (this.i(x, y) && b == 0 && this.module.po) {
         this.mouseDown = true;
      }
   }

   @Override
   public void mouseReleased(int x, int y, int m) {
      this.mouseDown = false;
   }

   @Override
   public void keyTyped(char t, int k) {
   }

   public boolean u(int x, int y) {
      return x > this.sliderStartX && x < this.sliderStartX + this.module.category.getWidth() / 2 + 1 && y > this.sliderStartY && y < this.sliderStartY + 16;
   }

   public boolean i(int x, int y) {
      return x > this.sliderStartX + this.module.category.getWidth() / 2
         && x < this.sliderStartX + this.module.category.getWidth()
         && y > this.sliderStartY
         && y < this.sliderStartY + 16;
   }

   public static enum Helping {
      MIN,
      MAX,
      NONE;
   }
}
