package zombiecat.client.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Minecraft.class})
public interface IMinecraft {
   @Accessor("rightClickDelayTimer")
   void setRightClickDelayTimer(int i);

   @Accessor("rightClickDelayTimer")
   int getRightClickDelayTimer();
}
