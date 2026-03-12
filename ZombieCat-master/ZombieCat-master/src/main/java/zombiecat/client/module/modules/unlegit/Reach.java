package zombiecat.client.module.modules.unlegit;

import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.SliderSetting;

public class Reach extends Module {
   public static Reach INSTANCE;
   public static SliderSetting reach;
   public static SliderSetting buildReach;

   public Reach() {
      super("Reach", Module.ModuleCategory.unlegit);
      this.registerSetting(reach = new SliderSetting("CombatReach", 3.0, 2.0, 50.0, 0.05));
      this.registerSetting(buildReach = new SliderSetting("BuildReach", 4.5, 2.0, 50.0, 0.05));
      INSTANCE = this;
   }
}
