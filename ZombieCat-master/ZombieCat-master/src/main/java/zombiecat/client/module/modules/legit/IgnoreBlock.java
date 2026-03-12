package zombiecat.client.module.modules.legit;

import zombiecat.client.module.Module;

public class IgnoreBlock extends Module {
   public static boolean isOn = false;

   public IgnoreBlock() {
      super("IgnoreBlock", Module.ModuleCategory.legit);
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
