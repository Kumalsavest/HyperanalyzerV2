package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zombiecat.client.mixins.IKeyBinding;
import zombiecat.client.module.Module;

public class FastStair extends Module {

   public FastStair() {
      super("FastStair", ModuleCategory.unlegit);
   }
   boolean jumping = false;
   @SubscribeEvent
   public void update(TickEvent event) {
      if (mc.thePlayer == null) return;
      if (String.valueOf(mc.thePlayer.posY).endsWith(".5") && mc.thePlayer.onGround) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
         ((IKeyBinding) mc.gameSettings.keyBindJump).setPressTime(1);
         jumping = true;
      } else if (jumping) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
         ((IKeyBinding) mc.gameSettings.keyBindJump).setPressTime(0);
         jumping = false;
      }
   }
}
