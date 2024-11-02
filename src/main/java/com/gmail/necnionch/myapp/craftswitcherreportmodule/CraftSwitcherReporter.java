package com.gmail.necnionch.myapp.craftswitcherreportmodule;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.IReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.BungeeReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.TCPClient;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.*;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.CompleteFuture;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class CraftSwitcherReporter implements TCPClient.OnClientListener {
    private static CraftSwitcherReporter instance;
    private static boolean failedAgent;
    private IReporter reporter = null;
    private TCPClient socket;
    private String serverName;
    private Runtime runtime = Runtime.getRuntime();
    private OperatingSystemMXBean operatingSystemMXBean = null;
//    private List<String> serverList = Collections.synchronizedList(new ArrayList<>());
    private Map<String, SwitcherServer> switcherServers = Collections.synchronizedMap(new HashMap<>());
    private ServerState state;

    public static CraftSwitcherReporter getInstance() {
        return instance;
    }

    public static void agentFailed() {
        if (instance != null) {
//            instance.socket.disconnect();
//            instance = null;
            instance.socket.setReconnectFlag(false);
            failedAgent = true;
        }
    }

    public void setReporter(IReporter reporter) {
        if (this.reporter != null)
            throw new UnsupportedOperationException("Cannot set multiple reporter (hybrid?)");

        this.reporter = reporter;
    }

    public IReporter getReporter() {
        return reporter;
    }

    public CraftSwitcherReporter(String serverName) {
        if (instance != null)
            throw new UnsupportedOperationException("Already instanced!");

        instance = this;
        socket = new TCPClient();
        this.serverName = serverName;
        try {
            operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Exception ignored) {}

        socket.addListener(this);
        socket.connect();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void shutdown() {
        if (!failedAgent)
            return;

        System.out.println("Go Shutdown");
        socket.disconnect();
        System.out.println("Good-bye!");
    }


    public String getServerName() {
        return serverName;
    }

    public SwitcherServer[] getServers() {
        return switcherServers.values().toArray(new SwitcherServer[0]);
    }

    public String[] getServerNames() {
        return switcherServers.keySet().toArray(new String[0]);
    }

    public SwitcherServer getServer(String id) {
        return switcherServers.get(id);
    }

    public TCPClient getSocket() {
        return socket;
    }

    public CompleteFuture sendDataFuture(SerializableData data) {
        return socket.sendFuture(data);
    }


    public SerializableData onReceiveData(SerializableData data, int dataId) {
        if (data instanceof StatusData) {
            return makeStatusData();

        } else if (data instanceof ServerListRequest) {
            updateServers((ServerListRequest) data);

        } else if (data instanceof ServerChangeStateData) {
            ServerChangeStateData r = (ServerChangeStateData) data;
            SwitcherServer server = getServer(r.server);
            if (server != null)
                server.state = ServerState.valueOf(r.state);

        } else if (data instanceof ServerAddData) {
            ServerAddData r = (ServerAddData) data;
            SwitcherServer server = new SwitcherServer(r.server, ServerState.STOPPED);
            switcherServers.put(server.getId(), server);

            if (reporter instanceof BungeeReporter && reporter.isStarted()) {
                ((BungeeReporter) reporter).onAddServer(server);
            }

        } else if (data instanceof ServerRemoveData) {
            ServerRemoveData r = (ServerRemoveData) data;
            SwitcherServer server = switcherServers.remove(r.server);

            if (reporter instanceof BungeeReporter && reporter.isStarted()) {
                ((BungeeReporter) reporter).onRemoveServer(server);
            }
        }
        return null;
    }

    public void onConnectClient() {
        if (state != null)
            sendDataFuture(ServerChangeStateData.create(state)).schedule();

        sendDataFuture(new ServerListRequest())
                .done((r) -> {
                    if (r instanceof ServerListRequest) {
                        updateServers((ServerListRequest) r);
                    }
                })
                .fail((e) -> {})
                .schedule();

    }

    public void onDisconnectClient() {
    }


    public StatusData makeStatusData() {
        Double cpuUsage = (operatingSystemMXBean != null) ? operatingSystemMXBean.getProcessCpuLoad() : null;
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        if (reporter == null)
            return new StatusData(
                    null, null, null,
                    cpuUsage, totalMemory, freeMemory, maxMemory
            );

        return new StatusData(
                reporter.getTps(), reporter.getOnlinePlayers(), reporter.getMaxPlayer(),
                cpuUsage, totalMemory, freeMemory, maxMemory
        );
    }

    private void updateServers(ServerListRequest req) {
        Set<String> unloadServers = switcherServers.keySet();

        for (String serverName : req.servers) {
            unloadServers.remove(serverName);

            sendDataFuture(new ServerStateRequest(serverName))
                    .done((r2) -> {
                        if (r2 instanceof ServerStateRequest) {
                            SwitcherServer server = new SwitcherServer((ServerStateRequest) r2);
                            switcherServers.put(serverName, server);

                            if (reporter instanceof BungeeReporter && reporter.isStarted()) {
                                ((BungeeReporter) reporter).onAddServer(server);
                            }
                        }
                    }).schedule();
        }

        unloadServers.forEach(n -> {
            SwitcherServer s = switcherServers.remove(n);
            if (reporter instanceof BungeeReporter && reporter.isStarted()) {
                ((BungeeReporter) reporter).onRemoveServer(s);
            }
        });

    }


    public void changeState(ServerState state) {
        this.state = state;
        SwitcherServer thisServer = switcherServers.get(getServerName());
        if (thisServer != null)
            thisServer.state = state;

        if (socket.isAvailable()) {
            sendDataFuture(ServerChangeStateData.create(state)).schedule();
        }
    }



}
