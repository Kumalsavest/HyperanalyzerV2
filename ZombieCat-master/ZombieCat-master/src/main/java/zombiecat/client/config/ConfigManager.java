package zombiecat.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import zombiecat.client.ZombieCat;
import zombiecat.client.module.Module;

public class ConfigManager {
   private final File configDirectory = new File(Minecraft.getMinecraft().mcDataDir + File.separator + "zombiecat" + File.separator + "configs");
   private Config config;
   private final ArrayList<Config> configs = new ArrayList<>();

   public ConfigManager() {
      if (!this.configDirectory.isDirectory()) {
         this.configDirectory.mkdirs();
      }

      this.discoverConfigs();
      File defaultFile = new File(this.configDirectory, "default.bplus");
      this.config = new Config(defaultFile);
      if (!defaultFile.exists()) {
         this.save();
      }
   }

   public static boolean isOutdated(File file) {
      JsonParser jsonParser = new JsonParser();

      try (FileReader reader = new FileReader(file)) {
         Object obj = jsonParser.parse(reader);
         JsonObject data = (JsonObject)obj;
         return false;
      } catch (ClassCastException | IOException | JsonSyntaxException var18) {
         var18.printStackTrace();
         return true;
      }
   }

   public void discoverConfigs() {
      this.configs.clear();
      if (this.configDirectory.listFiles() != null && Objects.requireNonNull(this.configDirectory.listFiles()).length > 0) {
         for (File file : Objects.requireNonNull(this.configDirectory.listFiles())) {
            if (file.getName().endsWith(".bplus") && !isOutdated(file)) {
               this.configs.add(new Config(new File(file.getPath())));
            }
         }
      }
   }

   public Config getConfig() {
      return this.config;
   }

   public void save() {
      JsonObject data = new JsonObject();
      data.addProperty("author", "Unknown");
      data.addProperty("notes", "");
      data.addProperty("intendedServer", "");
      data.addProperty("usedFor", 0);
      data.addProperty("lastEditTime", System.currentTimeMillis());
      JsonObject modules = new JsonObject();

      for (Module module : ZombieCat.moduleManager.getModules()) {
         modules.add(module.getName(), module.getConfigAsJson());
      }

      data.add("modules", modules);
      this.config.save(data);
   }

   public void setConfig(Config config) {
      this.config = config;
      JsonObject data = config.getData().get("modules").getAsJsonObject();

      for (Module module : new ArrayList<>(ZombieCat.moduleManager.getModules())) {
         if (data.has(module.getName())) {
            module.applyConfigFromJson(data.get(module.getName()).getAsJsonObject());
         } else {
            module.resetToDefaults();
         }
      }
   }

   public void loadConfigByName(String replace) {
      this.discoverConfigs();

      for (Config config : this.configs) {
         if (config.getName().equals(replace)) {
            this.setConfig(config);
         }
      }
   }

   public ArrayList<Config> getConfigs() {
      this.discoverConfigs();
      return this.configs;
   }

   public void copyConfig(Config config, String s) {
      File file = new File(this.configDirectory, s);
      Config newConfig = new Config(file);
      newConfig.save(config.getData());
   }

   public void resetConfig() {
      for (Module module : ZombieCat.moduleManager.getModules()) {
         module.resetToDefaults();
      }

      this.save();
   }

   public void deleteConfig(Config config) {
      config.file.delete();
      if (config.getName().equals(this.config.getName())) {
         this.discoverConfigs();
         if (this.configs.size() < 2) {
            this.resetConfig();
            File defaultFile = new File(this.configDirectory, "default.bplus");
            this.config = new Config(defaultFile);
            this.save();
         } else {
            this.config = this.configs.get(0);
         }

         this.save();
      }
   }
}
