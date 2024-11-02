package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands.SwitcherCommand.*;

public class SwitcherStopCommand extends Command {
    public SwitcherStopCommand() {
        super("swiStop".toLowerCase());
        setUsage("CraftSwitcher server stop command");
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(PREFIX_ERR + "許可されていません。");
            return true;
        }

        if (sender instanceof BlockCommandSender) {
            sender.sendMessage(PREFIX_ERR + "コマンドブロックから実行することはできません。");
            return true;
        }

        executeStop(sender, args);
        return true;
    }


    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (sender instanceof BlockCommandSender) {
            return Collections.emptyList();
        }
        if (!sender.hasPermission(PERMISSION))
            return Collections.emptyList();

        if (args.length == 1) {
            List<String> l = new ArrayList<>(Arrays.asList(CRAFT.getServerNames()));
            l.add("@all");
            return genComplete(args[0], l);
        }
        return Collections.emptyList();
    }


}
