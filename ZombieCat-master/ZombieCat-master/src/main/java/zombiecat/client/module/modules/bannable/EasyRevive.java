package zombiecat.client.module.modules.bannable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.mixins.IEntityPlayer;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;

public class EasyRevive extends Module {
   /*   public static BooleanSetting onlyClick;*/
   public static EasyRevive INSTANCE;

   public EasyRevive() {
      super("EasyRevive", ModuleCategory.bannable);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (Utils.Player.isPlayerInGame()) {
         for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer) entity;
               if (((IEntityPlayer) player).getSleeping()) {
                  player.setEntityBoundingBox(new AxisAlignedBB(
                          player.posX - 0.3,
                          player.posY,
                          player.posZ - 0.3,
                          player.posX + 0.3,
                          player.posY + 1.8,
                          player.posZ + 0.3
                  ));
               }
            }
         }
      }
   }
}
