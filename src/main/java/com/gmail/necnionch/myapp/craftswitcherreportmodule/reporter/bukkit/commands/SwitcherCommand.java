package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bukkit.commands;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.CraftSwitcherReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.SwitcherServer;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerRestartRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerStartRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerStopRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SwitcherCommand extends Command {
    static final CraftSwitcherReporter CRAFT = CraftSwitcherReporter.getInstance();
    static final String PREFIX = ChatColor.GOLD + "▢" + ChatColor.YELLOW + " SWI " + ChatColor.GOLD + "▢" + ChatColor.WHITE + " ";
    static final String PREFIX_ERR = ChatColor.GOLD + "▢" + ChatColor.YELLOW + " SWI " + ChatColor.GOLD + "▢" + ChatColor.RED + " ";
    public static final Permission PERMISSION = new Permission("craftswitcher.command.switcher", PermissionDefault.OP);

    private static boolean allowComponentSend;
    static {
        try {
            Class.forName("org.bukkit.command.CommandSender$Spigot");
            allowComponentSend = true;
        } catch (ClassNotFoundException e) {
            allowComponentSend = false;
        }

    }

    public SwitcherCommand() {
        super(
                "switcher",
                "CraftSwitcher admins command",
                "§c/switcher help",
                Collections.singletonList("swi")
        );
//        setPermission("craftswitcher.command.switcher");
//        setPermissionMessage(prefixRed + "権限がありません。");
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

        if (args.length >= 1 && args[0].equalsIgnoreCase("start")) {
            executeStart(sender, removeOnesArray(args));

        } else if (args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            executeStop(sender, removeOnesArray(args));

        } else if (args.length >= 1 && args[0].equalsIgnoreCase("restart")) {
            executeRestart(sender, removeOnesArray(args));

        } else if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(PREFIX_ERR + "実装されていません。");

        } else if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
            executeList(sender, removeOnesArray(args));

        } else if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            String clientText;
            if (CRAFT.getSocket().isAvailable()) {
                clientText = ChatColor.YELLOW + "ID: " + ChatColor.GOLD + CRAFT.getServerName();
            } else {
                clientText = ChatColor.RED + "利用不可";
            }
            sender.sendMessage(PREFIX + "CraftSwitcher レポートモジュール " + ChatColor.GRAY + "[" + clientText + ChatColor.GRAY + "]");
            sender.sendMessage("");
            sendMessage(sender, applyClickAction(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swi start "),
                    TextComponent.fromLegacyText("§7  /swi §6start §7(§6server§7/§6@ALL§7)    §7サーバーの起動")
            ));
            sendMessage(sender, applyClickAction(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swi stop "),
                    TextComponent.fromLegacyText("§7  /swi §6stop §7(§6server§7/§6@ALL§7)     §7サーバーの停止")
            ));
            sendMessage(sender, applyClickAction(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swi restart "),
                    TextComponent.fromLegacyText("§7  /swi §6restart §7[§6server§7/§6@ALL§7]  §7サーバーの再起動")
            ));
            sendMessage(sender, applyClickAction(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swi list"),
                    TextComponent.fromLegacyText("§7  /swi §6list                 §7サーバー一覧表示")
            ));
            sender.sendMessage("");

            return true;

        } else {
            sender.sendMessage(getUsage());
        }
        return true;

    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (sender instanceof BlockCommandSender) {
            return Collections.emptyList();
        }
        if (!sender.hasPermission(PERMISSION))
            return Collections.emptyList();

        if (args.length == 1) {
            return genComplete(args[0], Arrays.asList("start", "stop", "restart", "help", "list"));

        } else if (args.length == 2 && Arrays.asList("start", "stop", "restart").contains(args[0].toLowerCase())) {
            List<String> l = new ArrayList<>(Arrays.asList(CRAFT.getServerNames()));
            l.add("@all");
            return genComplete(args[1], l);
        }
        return Collections.emptyList();
    }


    static List<String> genComplete(String req, List<String> list) {
        return list.stream()
                .filter(s -> s .startsWith(req))
                .collect(Collectors.toList());
    }

    private static boolean disconnectedClient(CommandSender sender) {
        if (!CRAFT.getSocket().isAvailable()) {
            sender.sendMessage(PREFIX_ERR + "制御サーバーにアクセスできません。");
            return true;
        }
        return false;
    }

    private BaseComponent[] applyClickAction(ClickEvent clickEvent, BaseComponent... components) {
        for (BaseComponent component : components) {
            component.setClickEvent(clickEvent);
        }
        return components;
    }

    private static void sendMessage(CommandSender sender, BaseComponent[] components) {
        if (allowComponentSend) {
            sender.spigot().sendMessage(components);
        } else {
            sender.sendMessage(TextComponent.toLegacyText(components));
        }
    }

    private String[] removeOnesArray(String[] array) {
        List<String> l = new ArrayList<>(Arrays.asList(array));
        l.remove(0);
        return l.toArray(new String[0]);
    }



    public static void executeStart(CommandSender sender, String[] args) {
        if (disconnectedClient(sender))
            return;

        if (args.length == 0) {
            sender.sendMessage(PREFIX_ERR + "サーバーを指定してください。");
            return;
        }

        String serverName = args[0];
        CRAFT.sendDataFuture(ServerStartRequest.create(serverName))
                .done((r) -> {
                    if (r instanceof ServerStartRequest) {
                        ServerStartRequest res = (ServerStartRequest) r;
                        if (res.success) {
                            sender.sendMessage(PREFIX + res.targetServer + "サーバーを起動しています...");
                        } else {
                            sender.sendMessage(PREFIX_ERR + "起動エラー: " + res.getLocalizedFailMessage());
                        }
                    } else {
                        sender.sendMessage(PREFIX_ERR + "無効な応答が返されました。");
                    }

                })
                .fail((e) -> {
                    sender.sendMessage(PREFIX_ERR + "内部エラー: " + e.getMessage());
                })
                .schedule();

    }

    public static void executeStop(CommandSender sender, String[] args) {
        if (disconnectedClient(sender))
            return;

        if (args.length == 0) {
            sender.sendMessage(PREFIX_ERR + "サーバーを指定してください。");
            return;
        }

        String serverName = args[0];
        sender.sendMessage(PREFIX + "停止しています...");
        CRAFT.sendDataFuture(ServerStopRequest.create(serverName))
                .done((r) -> {
                    if (r instanceof ServerStopRequest) {
                        ServerStopRequest res = (ServerStopRequest) r;
                        if (res.success) {
                            sender.sendMessage(PREFIX + res.targetServer + "サーバーを停止しました。");
                        } else {
                            sender.sendMessage(PREFIX_ERR + "停止エラー: " + res.getLocalizedFailMessage());
                        }
                    } else {
                        sender.sendMessage(PREFIX_ERR + "無効な応答が返されました。");
                    }

                })
                .fail((e) -> {
                    sender.sendMessage(PREFIX_ERR + "内部エラー: " + e.getMessage());
                })
                .schedule();

    }

    public static void executeRestart(CommandSender sender, String[] args) {
        if (disconnectedClient(sender))
            return;

        ServerRestartRequest request = ServerRestartRequest.create((args.length == 0) ? CRAFT.getServerName() : args[0]);

        sender.sendMessage(PREFIX + "再起動しています...");
        CRAFT.sendDataFuture(request)
                .done((r) -> {
                    if (r instanceof ServerRestartRequest) {
                        ServerRestartRequest res = (ServerRestartRequest) r;
                        if (res.success) {
                            sender.sendMessage(PREFIX + res.targetServer + "サーバーを起動しています...");
                        } else {
                            sender.sendMessage(PREFIX_ERR + "再起動エラー: " + res.getLocalizedFailMessage());
                        }
                    } else {
                        sender.sendMessage(PREFIX_ERR + "無効な応答が返されました。");
                    }

                })
                .fail((e) -> {
                    sender.sendMessage(PREFIX_ERR + "内部エラー: " + e.getMessage());
                })
                .schedule();

    }

    public static void executeList(CommandSender sender, String[] args) {
        com.gmail.necnionch.myapp.craftswitcherreportmodule.v1.CraftSwitcherAPI.getCurrentServerId();

        if (disconnectedClient(sender)) {
            return;
        }
        SwitcherServer[] servers = CRAFT.getServers();
        if (servers.length == 0) {
            sender.sendMessage(PREFIX + "設定されているサーバーがありません。");
            return;
        }

        List<BaseComponent> entryComponent = new ArrayList<>(Arrays.asList(
                TextComponent.fromLegacyText(PREFIX + "サーバー一覧 (" + servers.length + "): ")
        ));
        SwitcherServer server;
        for (int i = 0; i < servers.length ; i++) {
            server = servers[i];
            ChatColor color;
            String suggestCommand = null;
            String hoverText;

            if (ServerState.STARTED.equals(server.getState())) {
                color = ChatColor.GREEN;
                suggestCommand = "restart";
                hoverText = ChatColor.GREEN + "起動済み";

            } else if (ServerState.STARTING.equals(server.getState())) {
                color = ChatColor.GOLD;
                hoverText = ChatColor.GOLD + "起動処理中";

            } else if (ServerState.STOPPING.equals(server.getState())) {
                color = ChatColor.GOLD;
                hoverText = ChatColor.GOLD + "停止処理中";

            } else if (ServerState.STOPPED.equals(server.getState())) {
                color = ChatColor.RED;
                suggestCommand = "start";
                hoverText = ChatColor.RED + "停止済み";

            } else if (ServerState.RUNNING.equals(server.getState())) {
                color = ChatColor.GOLD;
                hoverText = ChatColor.GOLD + "実行中";

            } else {
                color = ChatColor.RED;
                hoverText = ChatColor.RED + "不明な状態: " + server.getState().name();
            }

            ComponentBuilder cb = new ComponentBuilder(server.getId());
            cb.color(color);
            cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText)));
            if (suggestCommand != null)
                cb.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/swi " + suggestCommand + " " + server.getId()));

            entryComponent.addAll(Arrays.asList(cb.create()));
            if (i+1 < servers.length) {
                entryComponent.add(new TextComponent(ChatColor.GRAY + ", "));
            }
        }

        sendMessage(sender, entryComponent.toArray(new BaseComponent[0]));




    }

}
