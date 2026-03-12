package zombiecat.client.module.modules.legit;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;

public class Fullbright extends Module {
   private float defaultGamma;

   public Fullbright() {
      super("Fullbright", Module.ModuleCategory.legit);
   }

   @Override
   public void onEnable() {
      this.defaultGamma = mc.gameSettings.gammaSetting;
   }

   @Override
   public void onDisable() {
      mc.gameSettings.gammaSetting = this.defaultGamma;
   }

   @SubscribeEvent
   public void onPlayerTick(PlayerTickEvent e) {
      if (!Utils.Player.isPlayerInGame()) {
         this.onDisable();
      } else {
         if (mc.gameSettings.gammaSetting != 10000.0F) {
            mc.gameSettings.gammaSetting = 10000.0F;
         }
      }
   }
}
