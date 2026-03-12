package zombiecat.client.module.modules.bannable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import zombiecat.client.module.Module;

public class CollideFly extends Module {
   public static boolean isOn = false;

   public CollideFly() {
      super("CollideFly", Module.ModuleCategory.bannable);
   }

   @Override
   public void onEnable() {
      isOn = true;
   }

   @Override
   public void onDisable() {
      isOn = false;
   }

   public static void onBB(CollideFly.BlockBBEvent event) {
      if (isOn) {
         if (mc.gameSettings.keyBindJump.isKeyDown() || !mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (!event.block.getMaterial().blocksMovement()
               && event.block.getMaterial() != Material.carpet
               && event.block.getMaterial() != Material.vine
               && event.block.getMaterial() != Material.snow
               && !(event.block instanceof BlockLadder)) {
               event.setBoundingBox(new AxisAlignedBB(-2.0, -1.0, -2.0, 2.0, 1.0, 2.0).offset((double)event.x, (double)event.y, (double)event.z));
            }
         }
      }
   }

   public static final class BlockBBEvent {
      public final int x;
      public final int y;
      public final int z;
      private final Block block;
      public AxisAlignedBB boundingBox;

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getZ() {
         return this.z;
      }

      public Block getBlock() {
         return this.block;
      }

      public AxisAlignedBB getBoundingBox() {
         return this.boundingBox;
      }

      public void setBoundingBox(AxisAlignedBB var1) {
         this.boundingBox = var1;
      }

      public BlockBBEvent(BlockPos blockPos, Block block, AxisAlignedBB boundingBox) {
         this.block = block;
         this.boundingBox = boundingBox;
         this.x = blockPos.getX();
         this.y = blockPos.getY();
         this.z = blockPos.getZ();
      }
   }
}
