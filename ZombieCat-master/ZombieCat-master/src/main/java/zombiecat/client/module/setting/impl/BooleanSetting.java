package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class BooleanSetting extends Setting {
   private final String name;
   private boolean isEnabled;
   private final boolean defaultValue;

   public BooleanSetting(String name, boolean isEnabled) {
      super(name);
      this.name = name;
      this.isEnabled = isEnabled;
      this.defaultValue = isEnabled;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public void resetToDefaults() {
      this.isEnabled = this.defaultValue;
   }

   @Override
   public JsonObject getConfigAsJson() {
      JsonObject data = new JsonObject();
      data.addProperty("type", this.getSettingType());
      data.addProperty("value", this.getValue());
      return data;
   }

   @Override
   public String getSettingType() {
      return "tick";
   }

   @Override
   public void applyConfigFromJson(JsonObject data) {
      if (data.get("type").getAsString().equals(this.getSettingType())) {
         this.setEnabled(data.get("value").getAsBoolean());
      }
   }

   @Override
   public Component createComponent(ModuleComponent moduleComponent) {
      return null;
   }

   public boolean getValue() {
      return this.isEnabled;
   }

   public void toggle() {
      this.isEnabled = !this.isEnabled;
   }

   public void enable() {
      this.isEnabled = true;
   }

   public void disable() {
      this.isEnabled = false;
   }

   public void setEnabled(boolean b) {
      this.isEnabled = b;
   }
}
