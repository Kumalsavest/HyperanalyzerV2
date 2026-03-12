package zombiecat.client.mixins;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombiecat.client.module.modules.client.AntiSpam;

@Mixin({GuiNewChat.class})
public class MixinGuiNewChat {
   @Inject(
      method = "printChatMessage",
      at = {@At("HEAD")},
      cancellable = true
   )
   public void printChatMessage(IChatComponent p_printChatMessage_1_, CallbackInfo ci) {
      if (AntiSpam.isOn && !AntiSpam.ignore) {
         AntiSpam.interact(p_printChatMessage_1_);
         ci.cancel();
      }
   }
}
