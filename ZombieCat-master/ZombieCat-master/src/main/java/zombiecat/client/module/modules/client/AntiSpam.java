package zombiecat.client.module.modules.client;

import java.util.HashMap;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import zombiecat.client.module.Module;

public class AntiSpam extends Module {
   public static boolean isOn;
   private static final HashMap<String, AntiSpam.text> StringMap = new HashMap<>();
   static int size = 0;
   public static boolean ignore;

   public AntiSpam() {
      super("AntiSpam", Module.ModuleCategory.client);
   }

   @Override
   public void onEnable() {
      isOn = true;
   }

   @Override
   public void onDisable() {
      isOn = false;
   }

   public static void interact(IChatComponent chat) {
      ignore = true;
      String message = chat.getUnformattedText();
      if (StringMap.containsKey(message)) {
         StringMap.get(message).addNumber();
         chat.appendSibling(new ChatComponentText(" §7x" + StringMap.get(message).number));
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, StringMap.get(message).size);
      } else {
         size++;
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chat, size);
         StringMap.put(message, new AntiSpam.text(size));
      }

      ignore = false;
   }

   private static class text {
      int number = 1;
      int size;

      public text(int size) {
         this.size = size;
      }

      public void addNumber() {
         this.number++;
      }
   }
}
