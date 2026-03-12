package zombiecat.client.module.setting.impl;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;
import zombiecat.client.module.setting.Setting;

public class DescriptionSetting extends Setting {
   private String desc;
   private final String defaultDesc;

   public DescriptionSetting(String t) {
      super(t);
      this.desc = t;
      this.defaultDesc = t;
   }

   public String getDesc() {
      return this.desc;
   }

   public void setDesc(String t) {
      this.desc = t;
   }

   @Override
   public void resetToDefaults() {
      this.desc = this.defaultDesc;
   }

   @Override
   public JsonObject getConfigAsJson() {
      JsonObject data = new JsonObject();
      data.addProperty("type", this.getSettingType());
      data.addProperty("value", this.getDesc());
      return data;
   }

   @Override
   public String getSettingType() {
      return "desc";
   }

   @Override
   public void applyConfigFromJson(JsonObject data) {
      if (data.get("type").getAsString().equals(this.getSettingType())) {
         this.setDesc(data.get("value").getAsString());
      }
   }

   @Override
   public Component createComponent(ModuleComponent moduleComponent) {
      return null;
   }
}
