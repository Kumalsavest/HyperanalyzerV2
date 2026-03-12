package zombiecat.client.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zombiecat.client.module.modules.bannable.Phase;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {

    @Inject(
            method = "onLivingUpdate",
            at = {@At("HEAD")}
    )
    public void onHead(CallbackInfo ci) {
        Phase.onUpdate();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (Phase.isOn || noClip) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        if (noClip) {
            setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2;
        } else {
            super.moveEntity(x, y, z);
        }
    }
}
