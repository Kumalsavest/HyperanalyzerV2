package zombiecat.client.module;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import zombiecat.client.module.setting.Setting;
import zombiecat.client.module.setting.impl.BooleanSetting;

import java.util.ArrayList;

public class Module {
   protected ArrayList<Setting> settings;
   private final String moduleName;
   private final Module.ModuleCategory moduleCategory;
   boolean enabled = false;
   protected boolean defaultEnabled = this.enabled;
   protected int keycode = 0;
   protected int defualtKeyCode = this.keycode;
   protected static Minecraft mc;
   private boolean isToggled = false;

   public Module(String name, Module.ModuleCategory moduleCategory) {
      this.moduleName = name;
      this.moduleCategory = moduleCategory;
      this.settings = new ArrayList<>();
      mc = Minecraft.getMinecraft();
   }

   protected <E extends Module> E withKeycode(int i) {
      this.keycode = i;
      this.defualtKeyCode = i;
      return (E)this;
   }

   protected <E extends Module> E withEnabled(boolean i) {
      this.enabled = i;
      this.defaultEnabled = i;

      try {
         this.setToggled(i);
      } catch (Exception var3) {
      }

      return (E)this;
   }

   public JsonObject getConfigAsJson() {
      JsonObject settings = new JsonObject();

      for (Setting setting : this.settings) {
         JsonObject settingData = setting.getConfigAsJson();
         settings.add(setting.settingName, settingData);
      }

      JsonObject data = new JsonObject();
      data.addProperty("enabled", this.enabled);
      data.addProperty("keycode", this.keycode);
      data.add("settings", settings);
      return data;
   }

   public void applyConfigFromJson(JsonObject data) {
      try {
         this.keycode = data.get("keycode").getAsInt();
         this.setToggled(data.get("enabled").getAsBoolean());
         JsonObject settingsData = data.get("settings").getAsJsonObject();

         for (Setting setting : this.getSettings()) {
            if (settingsData.has(setting.getName())) {
               setting.applyConfigFromJson(settingsData.get(setting.getName()).getAsJsonObject());
            }
         }
      } catch (NullPointerException var5) {
      }
   }

   public void keybind() {
      if (this.keycode != 0 && this.canBeEnabled()) {
         if (!this.isToggled && Keyboard.isKeyDown(this.keycode)) {
            this.toggle();
            this.isToggled = true;
         } else if (!Keyboard.isKeyDown(this.keycode)) {
            this.isToggled = false;
         }
      }
   }

   public boolean canBeEnabled() {
      return true;
   }

   public void enable() {
      this.enabled = true;
      this.onEnable();
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void disable() {
      this.enabled = false;
      MinecraftForge.EVENT_BUS.unregister(this);
      this.onDisable();
   }

   public void setToggled(boolean enabled) {
      if (enabled) {
         this.enable();
      } else {
         this.disable();
      }
   }

   public String getName() {
      return this.moduleName;
   }

   public ArrayList<Setting> getSettings() {
      return this.settings;
   }

   public Setting getSettingByName(String name) {
      for (Setting setting : this.settings) {
         if (setting.getName().equalsIgnoreCase(name)) {
            return setting;
         }
      }

      return null;
   }

   public BooleanSetting register(BooleanSetting bs) {
      this.registerSetting(bs);
      return bs;
   }

   public void registerSetting(Setting Setting) {
      this.settings.add(Setting);
   }

   public Module.ModuleCategory moduleCategory() {
      return this.moduleCategory;
   }

   public boolean isOn() {
      return this.enabled;
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void toggle() {
      if (this.enabled) {
         this.disable();
      } else {
         this.enable();
      }
   }

   public void update() {
   }

   public void guiUpdate() {
   }

   public void guiButtonToggled(BooleanSetting b) {
   }

   public int getKeycode() {
      return this.keycode;
   }

   public void setbind(int keybind) {
      this.keycode = keybind;
   }

   public void resetToDefaults() {
      this.keycode = this.defualtKeyCode;
      this.setToggled(this.defaultEnabled);

      for (Setting setting : this.settings) {
         setting.resetToDefaults();
      }
   }

   public void onGuiClose() {
   }

   public String getBindAsString() {
      return this.keycode == 0 ? "None" : Keyboard.getKeyName(this.keycode);
   }

   public void clearBinds() {
      this.keycode = 0;
   }

   public static enum ModuleCategory {
      legit,
      unlegit,
      client,
      bannable;
   }
}
