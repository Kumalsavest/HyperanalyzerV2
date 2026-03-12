package com.kum.hyperanalyzer.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeybindHandler {
    public static KeyBinding openMenuKey;

    public static void init() {
        openMenuKey = new KeyBinding("Open HyperAnalyzer Menu", Keyboard.KEY_H, "HyperAnalyzer");
        ClientRegistry.registerKeyBinding(openMenuKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (openMenuKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiGameSessions());
        }
    }
}
