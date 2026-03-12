package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class ComboSetting<T extends Enum<?>> extends Setting {
   private T[] options;
   private T currentOption;
   private final T defaultOption;

   public ComboSetting(String settingName, T defaultOption) {
      super(settingName);
      this.currentOption = defaultOption;
      this.defaultOption = defaultOption;

      try {
         this.options = (T[])((Enum[])defaultOption.getClass().getMethod("values").invoke(null));
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   @Override
   public void resetToDefaults() {
      this.currentOption = this.defaultOption;
   }

   @Override
   public JsonObject getConfigAsJson() {
      JsonObject data = new JsonObject();
      data.addProperty("type", this.getSettingType());
      data.addProperty("value", this.getMode().toString());
      return data;
   }

   @Override
   public String getSettingType() {
      return "mode";
   }

   @Override
   public void applyConfigFromJson(JsonObject data) {
      if (data.get("type").getAsString().equals(this.getSettingType())) {
         String bruh = data.get("value").getAsString();

         for (T opt : this.options) {
            if (opt.toString().equals(bruh)) {
               this.setMode(opt);
            }
         }
      }
   }

   @Override
   public Component createComponent(ModuleComponent moduleComponent) {
      return null;
   }

   public T getMode() {
      return this.currentOption;
   }

   public void setMode(T value) {
      this.currentOption = value;
   }

   public void nextMode() {
      for (int i = 0; i < this.options.length; i++) {
         if (this.options[i] == this.currentOption) {
            this.currentOption = this.options[(i + 1) % this.options.length];
            return;
         }
      }
   }
}
