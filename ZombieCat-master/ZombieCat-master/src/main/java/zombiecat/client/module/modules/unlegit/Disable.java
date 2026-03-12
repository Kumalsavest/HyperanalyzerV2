package zombiecat.client.module.modules.unlegit;

import zombiecat.client.module.Module;

public class Disable extends Module {

   public Disable() {
      super("Disable", ModuleCategory.unlegit);
   }

   @Override
   public void onEnable() {
      if (QuickSwitch.INSTANCE.isOn()) {
         QuickSwitch.INSTANCE.disable();
      }
      if (QuickSwitch2.INSTANCE.isOn()) {
         QuickSwitch2.INSTANCE.disable();
      }
      if (QuickSwitch3.INSTANCE.isOn()) {
         QuickSwitch3.INSTANCE.disable();
      }
      disable();
   }
}
