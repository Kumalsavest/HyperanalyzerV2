package zombiecat.client.module;

import net.minecraft.client.gui.FontRenderer;
import zombiecat.client.module.modules.bannable.*;
import zombiecat.client.module.modules.client.AntiSpam;
import zombiecat.client.module.modules.client.ChatCopy;
import zombiecat.client.module.modules.client.GuiModule;
import zombiecat.client.module.modules.client.HUD;
import zombiecat.client.module.modules.legit.*;
import zombiecat.client.module.modules.unlegit.*;
import zombiecat.client.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManager {
   private final List<Module> modules = new ArrayList<>();
   public static boolean initialized = false;

   public ModuleManager() {
      if (!initialized) {
         addModule(new Disable());
         this.addModule(new Aimbot());
         this.addModule(new EasyRevive());
         addModule(new MapGB());
         this.addModule(new HidePlayer());
         this.addModule(new AntiSpam());
         this.addModule(new ChatCopy());
         this.addModule(new Chams());
         this.addModule(new CollideFly());
         this.addModule(new ESP());
         this.addModule(new FakeBlock());
         this.addModule(new FastPlace());
         addModule(new FastStair());
         this.addModule(new FireAlpha());
         this.addModule(new Freecam());
         addModule(new ItemESP());
         this.addModule(new Fullbright());
         this.addModule(new GhostBlock());
         addModule(new Phase());
         this.addModule(new IgnoreBlock());
         this.addModule(new GuiModule());
         this.addModule(new HUD());
         this.addModule(new IgnoreEntity());
         this.addModule(new QuickSwitch());
         this.addModule(new QuickSwitch2());
         this.addModule(new QuickSwitch3());
         this.addModule(new Switch());
         this.addModule(new Reach());
         this.addModule(new RightClicker());
         this.addModule(new Sprint());
         this.addModule(new Velocity());
         this.addModule(new ZHF());
         initialized = true;
      }
   }

   private void addModule(Module m) {
      this.modules.add(m);
   }

   public List<Module> getModules() {
      return this.modules;
   }

   public List<Module> getModulesInCategory(Module.ModuleCategory categ) {
      ArrayList<Module> modulesOfCat = new ArrayList<>();

      for (Module mod : this.modules) {
         if (mod.moduleCategory().equals(categ)) {
            modulesOfCat.add(mod);
         }
      }

      return modulesOfCat;
   }

   public Module getModuleByName(String name) {
      if (!initialized) {
         return null;
      } else {
         for (Module module : this.modules) {
            if (module.getName().equalsIgnoreCase(name)) {
               return module;
            }
         }

         return null;
      }
   }

   public Module getModuleByClazz(Class<? extends Module> c) {
      if (!initialized) {
         return null;
      } else {
         for (Module module : this.modules) {
            if (module.getClass().equals(c)) {
               return module;
            }
         }

         return null;
      }
   }

   public void sort() {
      if (HUD.alphabeticalSort.getValue()) {
         this.modules.sort(Comparator.comparing(Module::getName));
      } else {
         this.modules.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getName()) - Utils.mc.fontRendererObj.getStringWidth(o1.getName()));
      }
   }

   public int numberOfModules() {
      return this.modules.size();
   }

   public void sortLongShort() {
      this.modules.sort(Comparator.comparingInt(o2 -> Utils.mc.fontRendererObj.getStringWidth(o2.getName())));
   }

   public void sortShortLong() {
      this.modules.sort((o1, o2) -> Utils.mc.fontRendererObj.getStringWidth(o2.getName()) - Utils.mc.fontRendererObj.getStringWidth(o1.getName()));
   }

   public int getLongestActiveModule(FontRenderer fr) {
      int length = 0;

      for (Module mod : this.modules) {
         if (mod.isOn() && fr.getStringWidth(mod.getName()) > length) {
            length = fr.getStringWidth(mod.getName());
         }
      }

      return length;
   }

   public int getBoxHeight(FontRenderer fr, int margin) {
      int length = 0;

      for (Module mod : this.modules) {
         if (mod.isOn()) {
            length += fr.FONT_HEIGHT + margin;
         }
      }

      return length;
   }
}
