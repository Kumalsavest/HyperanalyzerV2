package zombiecat.client.module.modules.legit;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;

public class HidePlayer extends Module {
   public HidePlayer() {
      super("HidePlayer", Module.ModuleCategory.legit);
   }

   @SubscribeEvent
   public void onRenderLiving(Pre<EntityLivingBase> event) {
      if (this.isOn()
         && event.entity instanceof EntityPlayer
         && event.entity != mc.thePlayer
         && event.entity.getDistanceSqToEntity(mc.thePlayer) < 6.25
         && !event.entity.isPlayerSleeping()) {
         event.setCanceled(true);
      }
   }
}
