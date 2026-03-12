package zombiecat.client.module.modules.legit;

import zombiecat.client.module.Module;

public class IgnoreEntity extends Module {
   public static boolean isOn = false;

   public IgnoreEntity() {
      super("IgnoreEntity", Module.ModuleCategory.legit);
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
