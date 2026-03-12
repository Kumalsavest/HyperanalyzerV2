package zombiecat.client.module.modules.unlegit;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import zombiecat.client.mixins.IMinecraft;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;

public class FastPlace extends Module {
   public static SliderSetting delaySlider;
   public static BooleanSetting blockOnly;

   public FastPlace() {
      super("FastPlace", Module.ModuleCategory.unlegit);
      this.registerSetting(delaySlider = new SliderSetting("Delay", 0.0, 0.0, 4.0, 1.0));
      this.registerSetting(blockOnly = new BooleanSetting("Blocks only", true));
   }

   @SubscribeEvent
   public void onPlayerTick(TickEvent event) {
      if (event.side == Side.CLIENT && event.phase == Phase.START && Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
         if (blockOnly.getValue()) {
            ItemStack item = mc.thePlayer.getHeldItem();
            if (item == null || !(item.getItem() instanceof ItemBlock)) {
               return;
            }
         }
         if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.chest)
            return;

         try {
            int c = (int) delaySlider.getValue();
            if (c == 0) {
               ((IMinecraft) mc).setRightClickDelayTimer(0);
            } else {
               if (c == 4) {
                  return;
               }

               int d = ((IMinecraft) mc).getRightClickDelayTimer();
               if (d == 4) {
                  ((IMinecraft) mc).setRightClickDelayTimer(c);
               }
            }
         } catch (Exception e) {
         }
      }
   }
}
