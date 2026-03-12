package zombiecat.client.module.modules.client;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zombiecat.client.module.Module;

public class ChatCopy extends Module {
   public ChatCopy() {
      super("ChatCopy", Module.ModuleCategory.client);
   }

   @SubscribeEvent
   public void onChat(ClientChatReceivedEvent event) {
      if (this.isOn()) {
         String text = event.message.getFormattedText().replaceAll("§.", "");
         IChatComponent message = event.message;
         ChatStyle chatStyle = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/zbcopy " + text));
         message.appendSibling(
            new ChatComponentText(EnumChatFormatting.GRAY + " (C)")
               .setChatStyle(
                  chatStyle.setChatHoverEvent(
                     new HoverEvent(
                        net.minecraft.event.HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText(new StringBuilder().insert(0, "Click to copy ").append(text).toString())
                     )
                  )
               )
         );
      }
   }
}
