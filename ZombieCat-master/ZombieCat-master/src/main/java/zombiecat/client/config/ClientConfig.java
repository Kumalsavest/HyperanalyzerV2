package zombiecat.client.config;

import net.minecraft.client.Minecraft;
import zombiecat.client.ZombieCat;
import zombiecat.client.clickgui.components.CategoryComponent;
import zombiecat.client.module.modules.client.HUD;
import zombiecat.client.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientConfig {
   private final File configFile;

   public ClientConfig() {
      File configDir = new File(Minecraft.getMinecraft().mcDataDir, "zombiecat");
      if (!configDir.exists()) {
         configDir.mkdir();
      }

      String fileName = "config";
      this.configFile = new File(configDir, fileName);
      if (!this.configFile.exists()) {
         try {
            this.configFile.createNewFile();
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   public void saveConfig() {
      List<String> config = new ArrayList<>();
      config.add("clickgui-pos~ " + this.getClickGuiPos());
      config.add("loaded-cfg~ " + ZombieCat.configManager.getConfig().getName());
      config.add("HUDX~ " + HUD.getHudX());
      config.add("HUDY~ " + HUD.getHudY());

      try {
         PrintWriter writer = new PrintWriter(this.configFile);

         for (String line : config) {
            writer.println(line);
         }

         writer.close();
      } catch (IOException var5) {
         var5.printStackTrace();
      }
   }

   public void applyConfig() {
      for (String line : this.parseConfigFile()) {
         if (line.startsWith("clickgui-pos~ ")) {
            this.loadClickGuiCoords(line.replace("clickgui-pos~ ", ""));
         } else if (line.startsWith("loaded-cfg~ ")) {
            ZombieCat.configManager.loadConfigByName(line.replace("loaded-cfg~ ", ""));
         } else if (line.startsWith("HUDX~ ")) {
            try {
               HUD.setHudX(Integer.parseInt(line.replace("HUDX~ ", "")));
            } catch (Exception var6) {
               var6.printStackTrace();
            }
         } else if (line.startsWith("HUDY~ ")) {
            try {
               HUD.setHudY(Integer.parseInt(line.replace("HUDY~ ", "")));
            } catch (Exception var5) {
               var5.printStackTrace();
            }
         }
      }
   }

   private List<String> parseConfigFile() {
      List<String> configFileContents = new ArrayList<>();
      Scanner reader = null;

      try {
         reader = new Scanner(this.configFile);
      } catch (FileNotFoundException var4) {
         var4.printStackTrace();
      }

      while (reader.hasNextLine()) {
         configFileContents.add(reader.nextLine());
      }

      return configFileContents;
   }

   private void loadClickGuiCoords(String decryptedString) {
      for (String what : decryptedString.split("/")) {
         for (CategoryComponent cat : ZombieCat.clickGui.getCategoryList()) {
            if (what.startsWith(cat.categoryName.name())) {
               List<String> cfg = Utils.Java.StringListToList(what.split("~"));
               cat.setX(Integer.parseInt(cfg.get(1)));
               cat.setY(Integer.parseInt(cfg.get(2)));
               cat.setOpened(Boolean.parseBoolean(cfg.get(3)));
            }
         }
      }
   }

   public String getClickGuiPos() {
      StringBuilder posConfig = new StringBuilder();

      for (CategoryComponent cat : ZombieCat.clickGui.getCategoryList()) {
         posConfig.append(cat.categoryName.name());
         posConfig.append("~");
         posConfig.append(cat.getX());
         posConfig.append("~");
         posConfig.append(cat.getY());
         posConfig.append("~");
         posConfig.append(cat.isOpened());
         posConfig.append("/");
      }

      return posConfig.substring(0, posConfig.toString().length() - 2);
   }
}
