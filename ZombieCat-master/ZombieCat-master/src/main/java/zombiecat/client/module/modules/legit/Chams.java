package zombiecat.client.module.modules.legit;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent.Post;
import net.minecraftforge.client.event.RenderLivingEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import zombiecat.client.module.Module;

public class Chams extends Module {
   public Chams() {
      super("Chams", Module.ModuleCategory.legit);
   }

   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public void r1(Pre<?> e) {
      if (!e.isCanceled()) {
         if (e.entity != mc.thePlayer && !(e.entity instanceof EntityArmorStand) && e.entity.isEntityAlive()) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0F, -1100000.0F);
         }
      }
   }

   @SubscribeEvent
   public void r2(Post<?> e) {
      if (e.entity != mc.thePlayer && !(e.entity instanceof EntityArmorStand) && e.entity.isEntityAlive()) {
         GL11.glDisable(32823);
         GL11.glPolygonOffset(1.0F, 1100000.0F);
      }
   }
}
