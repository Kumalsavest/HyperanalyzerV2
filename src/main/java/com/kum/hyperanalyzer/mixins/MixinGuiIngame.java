package com.kum.hyperanalyzer.mixins;

import com.kum.hyperanalyzer.tracking.ZombiesDetector;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "displayTitle", at = @At(value = "RETURN"))
    private void displayTitle(String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut,
            CallbackInfo callbackInfo) {
        if (title != null) {
            String strippedTitle = net.minecraft.util.StringUtils.stripControlCodes(title).trim();
            ZombiesDetector.onTitle(strippedTitle);
        }
    }
}
