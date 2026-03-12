package zombiecat.client.tweaker;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.List;

@SortingIndex(1)
public class FMLLoadingPlugin implements ITweaker {
   public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
   }

   public void injectIntoClassLoader(LaunchClassLoader classLoader) {
      MixinBootstrap.init();
      Mixins.addConfiguration("mixins.zombiecat.json");
      MixinEnvironment.getDefaultEnvironment().setObfuscationContext("name");
      MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
      CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();

      try {
         Class<?> aClass = Class.forName("net.minecraftforge.fml.relauncher.CoreModManager");
         Method getIgnoredMods = null;

         try {
            getIgnoredMods = aClass.getDeclaredMethod("getIgnoredMods");
         } catch (NoSuchMethodException var11) {
            var11.printStackTrace();
         }

         try {
            if (getIgnoredMods == null) {
               getIgnoredMods = aClass.getDeclaredMethod("getLoadedCoremods");
            }
         } catch (NoSuchMethodException var10) {
            var10.printStackTrace();
         }

         if (codeSource != null) {
            URL location = codeSource.getLocation();

            try {
               File file = new File(location.toURI());
               if (file.isFile()) {
                  try {
                     if (getIgnoredMods != null) {
                        ((List)getIgnoredMods.invoke(null)).remove(file.getName());
                     }
                  } catch (Throwable var8) {
                     var8.printStackTrace();
                  }
               }
            } catch (URISyntaxException var9) {
               var9.printStackTrace();
            }
         } else {
            System.out.println("No CodeSource, if this is not a development environment we might run into problems!");
         }
      } catch (ClassNotFoundException var12) {
         var12.printStackTrace();
      }
   }

   public String getLaunchTarget() {
      return "net.minecraft.client.main.Main";
   }

   public String[] getLaunchArguments() {
      return new String[0];
   }
}
