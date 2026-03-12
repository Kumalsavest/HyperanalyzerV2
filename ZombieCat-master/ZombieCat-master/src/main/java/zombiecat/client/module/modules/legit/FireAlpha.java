package zombiecat.client.module.modules.legit;

import zombiecat.client.module.Module;

public class FireAlpha extends Module {
   public static boolean isOn;

   public FireAlpha() {
      super("FireAlpha", Module.ModuleCategory.legit);
   }

   @Override
   public void onEnable() {
      isOn = true;
   }

   @Override
   public void onDisable() {
      isOn = false;
   }
}
