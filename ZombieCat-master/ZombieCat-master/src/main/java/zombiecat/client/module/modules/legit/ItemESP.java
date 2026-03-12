package zombiecat.client.module.modules.legit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;
import zombiecat.client.utils.Utils;

import java.awt.*;

public class ItemESP extends Module {
   public ItemESP() {
      super("ItemESP", ModuleCategory.legit);
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (Utils.Player.isPlayerInGame()) {
         for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityItem) {
               Utils.HUD.drawBoxAroundEntity(entity, true, Color.green.getRGB());
            }
         }
      }
   }
}
