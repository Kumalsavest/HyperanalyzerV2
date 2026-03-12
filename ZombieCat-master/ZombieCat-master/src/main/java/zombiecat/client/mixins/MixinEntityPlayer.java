package zombiecat.client.mixins;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zombiecat.client.module.modules.bannable.EasyRevive;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntity {
    @Shadow
    protected boolean sleeping;
    /**
     * @author me
     * @reason easy rev
     */
    @Overwrite
    public boolean isPlayerSleeping() {
        return sleeping && !EasyRevive.INSTANCE.isOn();
    }
}
