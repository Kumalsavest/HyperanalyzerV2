package zombiecat.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Config {
   public final File file;
   public final long creationDate;

   public Config(File pathToFile) {
      this.file = pathToFile;
      long creationDate1;
      if (!this.file.exists()) {
         creationDate1 = System.currentTimeMillis();

         try {
            this.file.createNewFile();
         } catch (IOException var6) {
            throw new RuntimeException(var6);
         }
      } else {
         try {
            creationDate1 = this.getData().get("creationTime").getAsLong();
         } catch (NullPointerException var5) {
            creationDate1 = 0L;
         }
      }

      this.creationDate = creationDate1;
   }

   public String getName() {
      return this.file.getName().replace(".bplus", "");
   }

   public JsonObject getData() {
      JsonParser jsonParser = new JsonParser();

      try (FileReader reader = new FileReader(this.file)) {
         Object obj = jsonParser.parse(reader);
         return (JsonObject)obj;
      } catch (ClassCastException | IOException | JsonSyntaxException var17) {
         var17.printStackTrace();
         return null;
      }
   }

   public void save(JsonObject data) {
      data.addProperty("creationTime", this.creationDate);

      try (PrintWriter out = new PrintWriter(new FileWriter(this.file))) {
         out.write(data.toString());
      } catch (Exception var15) {
         var15.printStackTrace();
      }
   }
}
