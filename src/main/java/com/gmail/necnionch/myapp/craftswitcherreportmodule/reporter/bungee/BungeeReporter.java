package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.CraftSwitcherReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.SwitcherServer;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.IReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.events.*;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.TCPClient;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerChangeStateData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeReporter implements IReporter, TCPClient.OnClientListener {
    private static BungeeReporter instance;
    private CraftSwitcherReporter craft;
    private ProxyServer proxy;
    private boolean initializedServer;


    public BungeeReporter(CraftSwitcherReporter rep) {
        instance = this;
        proxy = ProxyServer.getInstance();
        proxy.getLogger().info("Loading CraftSwitcherReporter by Necnion8");
        craft = rep;

        craft.getSocket().addListener(this);

        if (craft.getSocket().isAvailable()) {
            rep.sendDataFuture(ServerChangeStateData.create(ServerState.STARTING)).schedule();

        }

    }

    public static void init() {
        CraftSwitcherReporter rep = CraftSwitcherReporter.getInstance();
        if (rep == null)
            return;

        try {
            rep.setReporter(new BungeeReporter(rep));

        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().severe("CraftSwitcherReporter loading fail!");
            e.printStackTrace();
        }
    }

    public static void onInitServer() {
        if (instance != null) {
            instance.initializedServer = true;
            instance.craft.changeState(ServerState.STARTED);

//            ProxyServer.getInstance().getPluginManager().registerListener(null, new PlayerEventListener(instance));
        }
    }


    public void onConnectClient() {
        craft.changeState((initializedServer) ? ServerState.STARTED : ServerState.STARTING);

        proxy.getPluginManager().callEvent(new SwitcherAvailableEvent());
    }

    public void onDisconnectClient() {
        proxy.getPluginManager().callEvent(new SwitcherNotAvailableEvent());

    }

    public SerializableData onReceiveData(SerializableData data, int dataId) {
        if (data instanceof ServerChangeStateData) {
            ServerChangeStateData r = (ServerChangeStateData) data;
            proxy.getPluginManager()
                    .callEvent(new SwitcherServerStateChangedEvent(craft.getServer(r.server)));
        }

        SwitcherDataReceiveEvent event = proxy.getPluginManager().callEvent(new SwitcherDataReceiveEvent(data));
        if (event.getResponse() != null)
            return event.getResponse();

        return null;
    }


    public void onRemoveServer(SwitcherServer server) {
        proxy.getPluginManager().callEvent(new SwitcherServerRemoveEvent(server));
    }

    public void onAddServer(SwitcherServer server) {
        proxy.getPluginManager().callEvent(new SwitcherServerAddEvent(server));
    }






    @Override
    public String getReporterName() {
        return "Bungee";
    }

    @Override
    public Map<UUID, String> getOnlinePlayers() {
        return proxy.getPlayers().stream()
                .collect(Collectors.toMap(ProxiedPlayer::getUniqueId, net.md_5.bungee.api.CommandSender::getName));
    }

    @Override
    public Integer getMaxPlayer() {
        return null;
    }

    @Override
    public Double getTps() {
        return null;
    }

    @Override
    public Boolean isStarted() {
        return initializedServer;
    }
}
