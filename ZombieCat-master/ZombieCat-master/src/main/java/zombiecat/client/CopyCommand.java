package zombiecat.client;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.apache.commons.lang3.StringUtils;

public class CopyCommand extends CommandBase {
   public String getCommandName() {
      return "zbcopy";
   }

   public String getCommandUsage(ICommandSender iCommandSender) {
      return "zbcopy <message>";
   }

   public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
      if (strings.length != 0) {
         String message = CommandBase.buildString(strings, 0);
         setClipboardString(message);
      }
   }

   public boolean canCommandSenderUseCommand(ICommandSender p_canCommandSenderUseCommand_1_) {
      return true;
   }

   public static void setClipboardString(String p_setClipboardString_0_) {
      if (!StringUtils.isEmpty(p_setClipboardString_0_)) {
         try {
            StringSelection stringselection = new StringSelection(p_setClipboardString_0_);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
         } catch (Exception var2) {
         }
      }
   }
}
