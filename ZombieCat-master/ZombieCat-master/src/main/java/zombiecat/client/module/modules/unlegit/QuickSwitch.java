package zombiecat.client.module.modules.unlegit;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import zombiecat.client.mixins.IKeyBinding;
import zombiecat.client.module.Module;
import zombiecat.client.module.setting.impl.BooleanSetting;
import zombiecat.client.module.setting.impl.SliderSetting;
import zombiecat.client.utils.Utils;
import zombiecat.client.utils.WatchTimer;

public class QuickSwitch extends Module {
   public static BooleanSetting onlyFire;
   public static SliderSetting delay;
   public static BooleanSetting anySlot;
   public static BooleanSetting s2;
   public static BooleanSetting s3;
   public static BooleanSetting s4;
   public static SliderSetting check;
   public static QuickSwitch INSTANCE;
   private final WatchTimer timer = new WatchTimer();
   boolean hold = false;

   public QuickSwitch() {
      super("QuickSwitch", Module.ModuleCategory.unlegit);
      this.registerSetting(onlyFire = new BooleanSetting("OnlyFire", true));
      this.registerSetting(delay = new SliderSetting("Delay", 50.0, 0.0, 80.0, 1.0));
      this.registerSetting(anySlot = new BooleanSetting("AnySlot", true));
      this.registerSetting(s2 = new BooleanSetting("Slot2", true));
      this.registerSetting(s3 = new BooleanSetting("Slot3", true));
      this.registerSetting(s4 = new BooleanSetting("Slot4", true));
      this.registerSetting(check = new SliderSetting("Check", 0.0, 0.0, 100.0, 1.0));
      INSTANCE = this;
   }

   @SubscribeEvent
   public void a(TickEvent.RenderTickEvent ev) {
      if (ev.phase == TickEvent.Phase.END && Utils.Player.isPlayerInGame()) {
         if (mc.gameSettings.showDebugInfo) {
            return;
         }
         mc.fontRendererObj.drawStringWithShadow("Preset:1 " + "Slot2:" + s2.getValue() + " Slot3:" + s3.getValue() + " Slot4:" + s4.getValue() + " Delay:" + delay.getValue(), 10, 10, 0xFFFFFF);
      }
   }

   @SubscribeEvent
   public void re(RenderWorldLastEvent e) {
      if (this.isOn() && mc.thePlayer != null && mc.theWorld != null) {
         if (!Keyboard.isKeyDown(29)) {
            this.hold = false;
         } else {
            boolean isHold = false;
            if (Keyboard.isKeyDown(78)) {
               isHold = true;
               if (!this.hold) {
                  delay.setValue(delay.getValue() + 1.0);
                  mc.thePlayer.addChatMessage(new ChatComponentText("Current " + delay.getValue() + "ms"));
               }
            }

            if (Keyboard.isKeyDown(74)) {
               isHold = true;
               if (!this.hold) {
                  delay.setValue(delay.getValue() - 1.0);
                  mc.thePlayer.addChatMessage(new ChatComponentText("Current " + delay.getValue() + "ms"));
               }
            }

            label277:
            for (int i = 0; i < mc.gameSettings.keyBindsHotbar.length; i++) {
               if (Keyboard.isKeyDown(mc.gameSettings.keyBindsHotbar[i].getKeyCode())) {
                  isHold = true;
                  if (!this.hold) {
                     switch (i) {
                        case 1:
                           s2.setEnabled(!s2.getValue());
                           mc.thePlayer.addChatMessage(new ChatComponentText("Slot2 " + (s2.getValue() ? "on" : "off")));
                           break label277;
                        case 2:
                           s3.setEnabled(!s3.getValue());
                           mc.thePlayer.addChatMessage(new ChatComponentText("Slot3 " + (s3.getValue() ? "on" : "off")));
                           break label277;
                        case 3:
                           s4.setEnabled(!s4.getValue());
                           mc.thePlayer.addChatMessage(new ChatComponentText("Slot4 " + (s4.getValue() ? "on" : "off")));
                        case 4:
                        default:
                           break label277;
                     }
                  }
                  break;
               }
            }

            this.hold = isHold;
         }

         if (onlyFire.getValue() && !mc.gameSettings.keyBindUseItem.isKeyDown()) {
            return;
         }

         if (!this.timer.passed(delay.getValue())) {
            return;
         }

         this.timer.reset();
         int cSlot = mc.thePlayer.inventory.currentItem + 1;
         if (cSlot == 2 && s2.getValue()) {
            int[] toggledSlots = new int[]{3, 4, 6, 7, 8, 9};

            for (int slot : toggledSlots) {
               if (this.isToggled(slot)) {
                  doSwap(slot);
                  return;
               }
            }
         }

         if (cSlot == 3 && s3.getValue()) {
            int[] toggledSlots = new int[]{4, 6, 7, 8, 9, 2};

            for (int slotx : toggledSlots) {
               if (this.isToggled(slotx)) {
                  doSwap(slotx);
                  return;
               }
            }
         }

         if (cSlot == 4 && s4.getValue()) {
            int[] toggledSlots = new int[]{6, 7, 8, 9, 2, 3};

            for (int slotxx : toggledSlots) {
               if (this.isToggled(slotxx)) {
                  doSwap(slotxx);
                  return;
               }
            }
         }
         if (anySlot.getValue() && cSlot != 1) {
            int[] toggledSlots = new int[]{2, 3, 4, 6, 7, 8, 9};

            for (int slotxxxxxxx : toggledSlots) {
               if (this.isToggled(slotxxxxxxx)) {
                  doSwap(slotxxxxxxx);
                  return;
               }
            }
         }
      }
   }

   public static void doSwap(int slot) {
      for (KeyBinding bind : mc.gameSettings.keyBindsHotbar) {
         KeyBinding.setKeyBindState(bind.getKeyCode(), false);
      }

      for (int i = 0; i < mc.gameSettings.keyBindsHotbar.length; i++) {
         if (i == slot - 1) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindsHotbar[i].getKeyCode(), true);
            ((IKeyBinding)mc.gameSettings.keyBindsHotbar[i]).setPressTime(1);
            return;
         }
      }
   }

   private boolean isToggled(int slot) {
      ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot - 1);
      if (stack == null) {
         return false;
      } else if (stack.getItem() == Items.dye) {
         return false;
      } else {
         if (check.getValue() == 0) {

         } else if (check.getValue() >= 100.0) {
            if (slot >= 2 && slot <= 4 && stack.getItemDamage() > 0 && stack.stackSize == 1) {
               return false;
            }
         } else if (slot >= 2
            && slot <= 4
            && 1.0 - (double)stack.getItemDamage() / (double)stack.getMaxDamage() < check.getValue() / 100.0
            && stack.stackSize == 1) {
            return false;
         }

         switch (slot) {
            case 2:
               return s2.getValue();
            case 3:
               return s3.getValue();
            case 4:
               return s4.getValue();
            case 5:
            default:
               return false;
         }
      }
   }
}
