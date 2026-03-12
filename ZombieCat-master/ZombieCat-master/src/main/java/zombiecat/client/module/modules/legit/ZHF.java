package zombiecat.client.module.modules.legit;

import zombiecat.client.module.Module;

public class ZHF extends Module {
   public static boolean isOn = false;

   public ZHF() {
      super("ZHF", Module.ModuleCategory.legit);
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
