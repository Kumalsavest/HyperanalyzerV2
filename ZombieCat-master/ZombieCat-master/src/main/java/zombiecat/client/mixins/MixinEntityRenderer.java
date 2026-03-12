package zombiecat.client.mixins;

import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombiecat.client.module.modules.legit.IgnoreEntity;
import zombiecat.client.module.modules.legit.ZHF;
import zombiecat.client.module.modules.unlegit.Reach;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin({EntityRenderer.class})
public class MixinEntityRenderer {
   @Shadow
   private Entity pointedEntity;
   @Shadow
   private Minecraft mc;

   @Inject(
      method = "getMouseOver",
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
      Entity entity = this.mc.getRenderViewEntity();
      if (entity != null && this.mc.theWorld != null) {
         this.mc.mcProfiler.startSection("pick");
         this.mc.pointedEntity = null;
         Reach reach = Reach.INSTANCE;
         double d0 = reach.isOn() ? Math.max(Reach.reach.getValue(), Reach.buildReach.getValue()) : (double)this.mc.playerController.getBlockReachDistance();
         this.mc.objectMouseOver = entity.rayTrace(reach.isOn() ? Reach.buildReach.getValue() : d0, p_getMouseOver_1_);
         double d1 = d0;
         Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
         boolean flag = false;
         if (this.mc.playerController.extendedReach()) {
            d0 = 6.0;
            d1 = 6.0;
         } else if (d0 > 3.0) {
            flag = true;
         }

         if (this.mc.objectMouseOver != null) {
            d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
         }

         if (reach.isOn()) {
            MovingObjectPosition movingObjectPosition = entity.rayTrace(Reach.buildReach.getValue(), p_getMouseOver_1_);
            if (movingObjectPosition != null) {
               d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }
         }

         Vec3 vec31 = entity.getLook(p_getMouseOver_1_);
         Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
         this.pointedEntity = null;
         Vec3 vec33 = null;
         float f = 1.0F;
         List<Entity> list = this.mc
            .theWorld
            .getEntitiesInAABBexcluding(
               entity,
               entity.getEntityBoundingBox()
                  .addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
                  .expand((double)f, (double)f, (double)f),
               Predicates.and(EntitySelectors.NOT_SPECTATING, p_apply_1_ -> p_apply_1_ != null && p_apply_1_.canBeCollidedWith())
            );
         if (ZHF.isOn) {
            list.removeIf(tempEntity -> tempEntity instanceof EntityArmorStand);
         }

         if (IgnoreEntity.isOn) {
            list.removeIf(tempEntity -> !(tempEntity instanceof EntityArmorStand) && !(tempEntity instanceof EntityPlayer));
         }

         double d2 = d1;

         for (Entity entity1 : list) {
            float f1 = entity1.getCollisionBorderSize();
            ArrayList<AxisAlignedBB> boxes = new ArrayList<>();
            boxes.add(entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1));

            for (AxisAlignedBB axisalignedbb : boxes) {
               MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
               if (axisalignedbb.isVecInside(vec3)) {
                  if (d2 >= 0.0) {
                     this.pointedEntity = entity1;
                     vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                     d2 = 0.0;
                  }
               } else if (movingobjectposition != null) {
                  double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                  if (d3 < d2 || d2 == 0.0) {
                     if (entity1 != entity.ridingEntity || entity.canRiderInteract()) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                     } else if (d2 == 0.0) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                     }
                  }
               }
            }
         }

         if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > (reach.isOn() ? Reach.reach.getValue() : 3.0)) {
            this.pointedEntity = null;
            this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectType.MISS, Objects.requireNonNull(vec33), null, new BlockPos(vec33));
         }

         if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
            this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
            if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
               this.mc.pointedEntity = this.pointedEntity;
            }
         }

         this.mc.mcProfiler.endSection();
      }

      ci.cancel();
   }
}
