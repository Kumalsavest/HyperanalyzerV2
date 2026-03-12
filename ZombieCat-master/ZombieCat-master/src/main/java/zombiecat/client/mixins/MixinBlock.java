package zombiecat.client.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zombiecat.client.module.modules.bannable.CollideFly;
import zombiecat.client.module.modules.bannable.Phase;
import zombiecat.client.module.modules.legit.IgnoreBlock;

import java.util.List;

@Mixin({Block.class})
public abstract class MixinBlock {
   @Shadow
   @Final
   protected BlockState blockState;

   @Inject(
      method = "isCollidable",
      at = {@At("HEAD")},
      cancellable = true
   )
   private void isCollidable(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
      if (IgnoreBlock.isOn && (Block) (Object) this != Blocks.chest && (Block) (Object) this != Blocks.stone_button && (Block) (Object) this != Blocks.wooden_button) {
         callbackInfoReturnable.setReturnValue(false);
      }
   }

   @Shadow
   public abstract AxisAlignedBB getCollisionBoundingBox(World var1, BlockPos var2, IBlockState var3);

   /**
    * @author me
    * @reason fly
    */
   @Overwrite
   public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
      AxisAlignedBB axisalignedbb = this.getCollisionBoundingBox(worldIn, pos, state);
      CollideFly.BlockBBEvent blockBBEvent = new CollideFly.BlockBBEvent(pos, this.blockState.getBlock(), axisalignedbb);
      CollideFly.onBB(blockBBEvent);
      Phase.onBB(blockBBEvent);
      axisalignedbb = blockBBEvent.getBoundingBox();
      if (axisalignedbb != null && mask.intersectsWith(axisalignedbb)) {
         list.add(axisalignedbb);
      }
   }
}
