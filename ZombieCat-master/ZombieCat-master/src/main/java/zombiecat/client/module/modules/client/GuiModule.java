package zombiecat.client.module.modules.client;

import zombiecat.client.ZombieCat;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.DescriptionSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

public class GuiModule extends Module {
   public static SliderSetting guiTheme;
   public static SliderSetting backgroundOpacity;
   public static DescriptionSetting guiThemeDesc;
   public static BooleanSetting categoryBackground;

   public GuiModule() {
      super("Gui", Module.ModuleCategory.client);
      this.withKeycode(54);
      this.registerSetting(guiTheme = new SliderSetting("Theme", 3.0, 1.0, 4.0, 1.0));
      this.registerSetting(guiThemeDesc = new DescriptionSetting("Mode: b+"));
      this.registerSetting(backgroundOpacity = new SliderSetting("Background Opacity %", 43.0, 0.0, 100.0, 1.0));
      this.registerSetting(categoryBackground = new BooleanSetting("Category Background", true));
   }

   @Override
   public void onEnable() {
      if (Utils.Player.isPlayerInGame() && mc.currentScreen != ZombieCat.clickGui) {
         mc.displayGuiScreen(ZombieCat.clickGui);
         ZombieCat.clickGui.initMain();
      }

      this.disable();
   }

   @Override
   public void guiUpdate() {
      switch ((int)guiTheme.getValue()) {
         case 1:
            guiThemeDesc.setDesc("Mode: b1");
            break;
         case 2:
            guiThemeDesc.setDesc("Mode: b2");
            break;
         case 3:
            guiThemeDesc.setDesc("Mode: b3");
            break;
         case 4:
            guiThemeDesc.setDesc("Mode: b+");
      }
   }
}
