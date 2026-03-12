package zombiecat.client.module.modules.bannable;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import zombiecat.client.module.Module;

public class FakeBlock extends Module {
   public FakeBlock() {
      super("FakeBlock", Module.ModuleCategory.bannable);
   }

   @Override
   public void onEnable() {
      MovingObjectPosition ray = mc.objectMouseOver;
      if (ray != null && ray.typeOfHit == MovingObjectType.BLOCK) {
         BlockPos blockpos = ray.getBlockPos().offset(ray.sideHit);
         mc.theWorld.setBlockState(blockpos, Blocks.diamond_block.getDefaultState());
      }

      this.disable();
   }
}
