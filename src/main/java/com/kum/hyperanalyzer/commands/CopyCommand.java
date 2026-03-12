package com.kum.hyperanalyzer.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CopyCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "hacopy";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/hacopy";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String text = SessionStatsCommand.lastCopyText;
        if (text != null && !text.isEmpty()) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
                sender.addChatMessage(new ChatComponentText("\u00a7a[HyperAnalyzer] Stats copied to clipboard!"));
            } catch (Exception e) {
                sender.addChatMessage(new ChatComponentText("\u00a7c[HyperAnalyzer] Failed to copy."));
            }
        }
    }
}
