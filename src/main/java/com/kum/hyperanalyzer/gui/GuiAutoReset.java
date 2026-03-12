package com.kum.hyperanalyzer.gui;

import com.kum.hyperanalyzer.data.ModConfig;
import com.kum.hyperanalyzer.tracking.AutoReset;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * Auto Reset settings screen.
 *
 * Bad Blood / Dead End: 5 target-chest slots (each cycles through NA + the map's valid chests).
 * Prison:               3 target-chest slots (NA + all Prison chests) + "Yard Key Part Only" toggle.
 *
 * Each map's selections are saved independently so switching maps never loses your other settings.
 * Settings are saved on "Save" and persist across relaunches.
 */
public class GuiAutoReset extends GuiScreen {

    private final GuiScreen parent;
    private static final String[] MAP_NAMES = AutoReset.AUTO_RESET_MAPS;

    // Working copies (committed to ModConfig on Save)
    private boolean enabled;
    private int mapIndex;

    // Per-map working copies (index into cycleOptions for that slot)
    // BB: 5 slots, DE: 5 slots, Prison: 3 slots
    private int[] bbSlotIdx   = new int[5];
    private int[] deSlotIdx   = new int[5];
    private int[] prSlotIdx   = new int[3];
    private boolean prYardOnly;

    // Cycle options: "NA" first, then the valid chests for that map
    private static final String[] BB_OPTIONS;
    private static final String[] DE_OPTIONS;
    private static final String[] PR_OPTIONS;

    static {
        String[] bbChests = AutoReset.getStartChestOptions("Bad Blood");
        BB_OPTIONS = prepend("NA", bbChests);

        String[] deChests = AutoReset.getStartChestOptions("Dead End");
        DE_OPTIONS = prepend("NA", deChests);

        String[] prChests = AutoReset.getStartChestOptions("Prison");
        PR_OPTIONS = prepend("NA", prChests);
    }

    private static String[] prepend(String first, String[] rest) {
        String[] out = new String[rest.length + 1];
        out[0] = first;
        System.arraycopy(rest, 0, out, 1, rest.length);
        return out;
    }

    public GuiAutoReset(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Load from config
        enabled  = ModConfig.instance.autoResetEnabled;
        mapIndex = indexOfMap(ModConfig.instance.autoResetMap);

        // Load BB slots
        for (int i = 0; i < 5; i++)
            bbSlotIdx[i] = indexInOptions(BB_OPTIONS, ModConfig.instance.arBadBloodTargets[i]);
        // Load DE slots
        for (int i = 0; i < 5; i++)
            deSlotIdx[i] = indexInOptions(DE_OPTIONS, ModConfig.instance.arDeadEndTargets[i]);
        // Load Prison slots
        for (int i = 0; i < 3; i++)
            prSlotIdx[i] = indexInOptions(PR_OPTIONS, ModConfig.instance.arPrisonTargets[i]);
        prYardOnly = ModConfig.instance.arPrisonYardOnly;

        buildButtons();
    }

    // Button IDs:
    //   0        = Auto Reset ON/OFF
    //   1        = Map cycle
    //   10..14   = BB chest slots 0..4
    //   20..24   = DE chest slots 0..4
    //   30..32   = Prison chest slots 0..2
    //   33       = Prison Yard toggle
    //   90       = Save
    //   91       = Back

    private void buildButtons() {
        buttonList.clear();

        int cx = width / 2;
        int y  = 55;

        // ON/OFF
        String onOff = enabled ? "\u00a7aON" : "\u00a7cOFF";
        buttonList.add(new GuiButton(0, cx - 100, y, 200, 20, "Auto Reset: " + onOff + "\u00a7r"));
        y += 24;

        // Map
        buttonList.add(new GuiButton(1, cx - 100, y, 200, 20, "Map: \u00a7e" + MAP_NAMES[mapIndex] + "\u00a7r"));
        y += 28;

        String map = MAP_NAMES[mapIndex];
        if ("Bad Blood".equals(map)) {
            buildSlotButtons(10, BB_OPTIONS, bbSlotIdx, "Target Chest", cx, y);
        } else if ("Dead End".equals(map)) {
            buildSlotButtons(20, DE_OPTIONS, deSlotIdx, "Target Chest", cx, y);
        } else { // Prison
            buildSlotButtons(30, PR_OPTIONS, prSlotIdx, "Target Chest", cx, y);
            y += 3 * 24;
            String yardLabel = prYardOnly ? "\u00a7cReset if Yard: ON\u00a7r" : "\u00a77Reset if Yard: OFF\u00a7r";
            buttonList.add(new GuiButton(33, cx - 100, y, 200, 20, yardLabel));
        }

        // Save / Back
        buttonList.add(new GuiButton(90, cx - 105, height - 30, 100, 20, "\u00a7aSave"));
        buttonList.add(new GuiButton(91, cx + 5,   height - 30, 100, 20, "Back"));
    }

    private void buildSlotButtons(int baseId, String[] options, int[] slotIdx, String label, int cx, int startY) {
        for (int i = 0; i < slotIdx.length; i++) {
            String val = options[slotIdx[i]];
            String color = "NA".equals(val) ? "\u00a77" : "\u00a76";
            buttonList.add(new GuiButton(baseId + i, cx - 100, startY + i * 24, 200, 20,
                    label + " " + (i + 1) + ": " + color + val + "\u00a7r"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        int id = button.id;

        if (id == 0) {
            enabled = !enabled;
        } else if (id == 1) {
            mapIndex = (mapIndex + 1) % MAP_NAMES.length;
            // NOTE: we do NOT reset slot indices — each map keeps its own state
        } else if (id >= 10 && id <= 14) {
            int slot = id - 10;
            bbSlotIdx[slot] = (bbSlotIdx[slot] + 1) % BB_OPTIONS.length;
        } else if (id >= 20 && id <= 24) {
            int slot = id - 20;
            deSlotIdx[slot] = (deSlotIdx[slot] + 1) % DE_OPTIONS.length;
        } else if (id >= 30 && id <= 32) {
            int slot = id - 30;
            prSlotIdx[slot] = (prSlotIdx[slot] + 1) % PR_OPTIONS.length;
        } else if (id == 33) {
            prYardOnly = !prYardOnly;
        } else if (id == 90) {
            // Save everything
            ModConfig.instance.autoResetEnabled = enabled;
            ModConfig.instance.autoResetMap     = MAP_NAMES[mapIndex];
            for (int i = 0; i < 5; i++) ModConfig.instance.arBadBloodTargets[i] = BB_OPTIONS[bbSlotIdx[i]];
            for (int i = 0; i < 5; i++) ModConfig.instance.arDeadEndTargets[i]  = DE_OPTIONS[deSlotIdx[i]];
            for (int i = 0; i < 3; i++) ModConfig.instance.arPrisonTargets[i]   = PR_OPTIONS[prSlotIdx[i]];
            ModConfig.instance.arPrisonYardOnly = prYardOnly;
            ModConfig.save();
            mc.displayGuiScreen(parent);
            return;
        } else if (id == 91) {
            mc.displayGuiScreen(parent);
            return;
        }

        buildButtons();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(fontRendererObj, "\u00a7lAuto Reset Settings\u00a7r", width / 2, 10, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "\u00a77Resets until the Round 1 chest matches one of your targets.\u00a7r",
                width / 2, 22, 0xFFFFFF);
        drawCenteredString(fontRendererObj,
                "\u00a77Set slots to \u00a77NA\u00a77 to ignore them.\u00a7r",
                width / 2, 32, 0xFFFFFF);

        // Show requeue command
        String cmd = AutoReset.getRequeueCommand(MAP_NAMES[mapIndex]);
        if (cmd != null) {
            drawCenteredString(fontRendererObj,
                    "\u00a77Command: \u00a7e" + cmd + "\u00a7r", width / 2, height - 50, 0xFFFFFF);
        }

        // Prison extra explanation
        if ("Prison".equals(MAP_NAMES[mapIndex])) {
            int slotBottom = 55 + 24 + 28 + 3 * 24 + 24 + 10;
            drawCenteredString(fontRendererObj,
                    "\u00a77Reset if Yard: resets immediately when key part = Yard\u00a7r",
                    width / 2, slotBottom, 0xAAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ---- Helpers ----

    private int indexOfMap(String map) {
        for (int i = 0; i < MAP_NAMES.length; i++) if (MAP_NAMES[i].equals(map)) return i;
        return 0;
    }

    private int indexInOptions(String[] options, String value) {
        if (value == null) return 0;
        for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(value)) return i;
        return 0; // default to NA (index 0)
    }
}
