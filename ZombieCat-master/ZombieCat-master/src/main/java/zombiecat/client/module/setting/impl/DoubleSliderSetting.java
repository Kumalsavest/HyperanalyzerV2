package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class DoubleSliderSetting extends Setting {
   private final String name;
   private double valMax;
   private double valMin;
   private final double max;
   private final double min;
   private final double interval;
   private final double defaultValMin;
   private final double defaultValMax;

   public DoubleSliderSetting(String settingName, double defaultValueMin, double defaultValueMax, double min, double max, double intervals) {
      super(settingName);
      this.name = settingName;
      this.valMin = defaultValueMin;
      this.valMax = defaultValueMax;
      this.min = min;
      this.max = max;
      this.interval = intervals;
      this.defaultValMin = this.valMin;
      this.defaultValMax = this.valMax;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public void resetToDefaults() {
      this.setValueMin(this.defaultValMin);
      this.setValueMax(this.defaultValMax);
   }

   @Override
   public JsonObject getConfigAsJson() {
      JsonObject data = new JsonObject();
      data.addProperty("type", this.getSettingType());
      data.addProperty("valueMin", this.getInputMin());
      data.addProperty("valueMax", this.getInputMax());
      return data;
   }

   @Override
   public String getSettingType() {
      return "doubleslider";
   }

   @Override
   public void applyConfigFromJson(JsonObject data) {
      if (data.get("type").getAsString().equals(this.getSettingType())) {
         this.setValueMax(data.get("valueMax").getAsDouble());
         this.setValueMin(data.get("valueMin").getAsDouble());
      }
   }

   @Override
   public Component createComponent(ModuleComponent moduleComponent) {
      return null;
   }

   public double getInputMin() {
      return round(this.valMin, 2);
   }

   public double getInputMax() {
      return round(this.valMax, 2);
   }

   public double getMin() {
      return this.min;
   }

   public double getMax() {
      return this.max;
   }

   public void setValueMin(double n) {
      n = correct(n, this.min, this.valMax);
      n = (double)Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
      this.valMin = n;
   }

   public void setValueMax(double n) {
      n = correct(n, this.valMin, this.max);
      n = (double)Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
      this.valMax = n;
   }

   public static double correct(double val, double min, double max) {
      val = Math.max(min, val);
      return Math.min(max, val);
   }

   public static double round(double val, int p) {
      if (p < 0) {
         return 0.0;
      } else {
         BigDecimal bd = new BigDecimal(val);
         bd = bd.setScale(p, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }
}
