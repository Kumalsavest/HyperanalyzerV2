package zombiecat.client.mixins;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({EntityPlayer.class})
public interface IEntityPlayer {
   @Accessor("sleeping")
   void setSleeping(boolean var1);

   @Accessor("sleeping")
   boolean getSleeping();
}
