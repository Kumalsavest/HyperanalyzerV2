package zombiecat.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import zombiecat.client.clickgui.ClickGui;
import zombiecat.client.config.ClientConfig;
import zombiecat.client.config.ConfigManager;
import zombiecat.client.module.Module;
import zombiecat.client.module.ModuleManager;
import zombiecat.client.utils.Utils;

@Mod(
   modid = "zombiecat",
   version = "1.7.5",
   acceptedMinecraftVersions = "[1.8.9]"
)
public class ZombieCat {
   public static ConfigManager configManager;
   public static ClientConfig clientConfig;
   public static final ModuleManager moduleManager = new ModuleManager();
   public static ClickGui clickGui;

   @EventHandler
   public void init(FMLInitializationEvent event) {
      init();
   }

   public static void init() {
      MinecraftForge.EVENT_BUS.register(new ZombieCat());
      clickGui = new ClickGui();
      configManager = new ConfigManager();
      clientConfig = new ClientConfig();
      clientConfig.applyConfig();
      ClientCommandHandler.instance.registerCommand(new CopyCommand());
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.phase == Phase.END && Utils.Player.isPlayerInGame()) {
         for (int i = 0; i < moduleManager.numberOfModules(); i++) {
            Module module = moduleManager.getModules().get(i);
            if (Minecraft.getMinecraft().currentScreen == null) {
               module.keybind();
            } else if (Minecraft.getMinecraft().currentScreen instanceof ClickGui) {
               module.guiUpdate();
            }

            if (module.isOn()) {
               module.update();
            }
         }
      }
   }
}
