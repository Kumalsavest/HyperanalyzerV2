package zombiecat.client.mixins;

import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({C03PacketPlayer.class})
public interface IC03PacketPlayer {
   @Accessor("yaw")
   void setYaw(float var1);
   @Accessor("pitch")
   void setPitch(float var1);

   @Accessor("x")
   void setX(double var1);
   @Accessor("z")
   void setZ(double var1);
}
