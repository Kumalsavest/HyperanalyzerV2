package zombiecat.client.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public boolean noClip;
    @Shadow
    public void moveEntity(double x, double y, double z) {
    }
    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;
    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);
}
