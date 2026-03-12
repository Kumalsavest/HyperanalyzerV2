package zombiecat.client.module.modules.legit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;

public class Sprint extends Module {
   public Sprint() {
      super("Sprint", Module.ModuleCategory.legit);
   }

   @SubscribeEvent
   public void p(PlayerTickEvent e) {
      if (Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
         KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
      }
   }
}
