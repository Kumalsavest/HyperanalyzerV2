package zombiecat.client.module.setting;

import com.google.gson.JsonObject;
import zombiecat.client.clickgui.Component;
import zombiecat.client.clickgui.components.ModuleComponent;

public abstract class Setting {
   public String settingName;

   public Setting(String name) {
      this.settingName = name;
   }

   public String getName() {
      return this.settingName;
   }

   public abstract void resetToDefaults();

   public abstract JsonObject getConfigAsJson();

   public abstract String getSettingType();

   public abstract void applyConfigFromJson(JsonObject var1);

   public abstract Component createComponent(ModuleComponent var1);
}
