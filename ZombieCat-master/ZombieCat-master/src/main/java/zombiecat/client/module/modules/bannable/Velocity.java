package zombiecat.client.module.modules.bannable;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

public class Velocity extends Module {
   public static SliderSetting a;
   public static SliderSetting b;
   public static SliderSetting c;
   public static BooleanSetting d;
   public static BooleanSetting e;

   public Velocity() {
      super("Velocity", Module.ModuleCategory.bannable);
      this.registerSetting(a = new SliderSetting("Horizontal", 90.0, 0.0, 100.0, 1.0));
      this.registerSetting(b = new SliderSetting("Vertical", 100.0, 0.0, 100.0, 1.0));
      this.registerSetting(c = new SliderSetting("Chance", 100.0, 0.0, 100.0, 1.0));
      this.registerSetting(d = new BooleanSetting("Only while targeting", false));
      this.registerSetting(e = new BooleanSetting("Disable while holding S", false));
   }

   @Override
   public void update() {
      this.c(null);
   }

   @SubscribeEvent
   public void c(LivingUpdateEvent ev) {
      if (Utils.Player.isPlayerInGame() && mc.thePlayer.maxHurtTime > 0 && mc.thePlayer.hurtTime == mc.thePlayer.maxHurtTime) {
         if (d.getValue() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
            return;
         }

         if (e.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            return;
         }

         if (c.getValue() != 100.0) {
            double ch = Math.random();
            if (ch >= c.getValue() / 100.0) {
               return;
            }
         }

         if (a.getValue() != 100.0) {
            mc.thePlayer.motionX = mc.thePlayer.motionX * (a.getValue() / 100.0);
            mc.thePlayer.motionZ = mc.thePlayer.motionZ * (a.getValue() / 100.0);
         }

         if (b.getValue() != 100.0) {
            mc.thePlayer.motionY = mc.thePlayer.motionY * (b.getValue() / 100.0);
         }
      }
   }
}
