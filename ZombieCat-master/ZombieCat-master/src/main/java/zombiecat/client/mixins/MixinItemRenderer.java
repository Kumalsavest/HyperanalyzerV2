package zombiecat.client.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zombiecat.client.module.modules.legit.FireAlpha;

@Mixin({ItemRenderer.class})
public class MixinItemRenderer {
   @Redirect(
      method = "renderFireInFirstPerson",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"
      )
   )
   private void renderFireInFirstPerson(float p_color_0_, float p_color_1_, float p_color_2_, float p_color_3_) {
      if (p_color_3_ != 1.0F && FireAlpha.isOn) {
         GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, 0.15F);
      } else {
         GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
      }
   }
}
