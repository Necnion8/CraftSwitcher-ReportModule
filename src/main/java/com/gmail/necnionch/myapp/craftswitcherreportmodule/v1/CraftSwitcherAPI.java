package com.gmail.necnionch.myapp.craftswitcherreportmodule.v1;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.CraftSwitcherReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.SwitcherServer;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.IReporter;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.TCPClient;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.CompleteFuture;

public class CraftSwitcherAPI {
    private static CraftSwitcherReporter craft = CraftSwitcherReporter.getInstance();

    public static String getCurrentServerId() {
        return craft.getServerName();
    }

    public static SwitcherServer[] getServers() {
        return craft.getServers();
    }

    public static String[] getServerNames() {
        return craft.getServerNames();
    }

    public static SwitcherServer getServer(String id) {
        return craft.getServer(id);
    }

    public static CompleteFuture makeSendFuture(SerializableData data) {
        return craft.sendDataFuture(data);
    }

    public static void addClientListener(TCPClient.OnClientListener listener) {
        craft.getSocket().addListener(listener);
    }

    public static void removeClientListener(TCPClient.OnClientListener listener) {
        craft.getSocket().removeListener(listener);
    }

    public static boolean isAvailableSwitcher() {
        return craft.getSocket().isAvailable();
    }

    public static IReporter getReporter() {
        return craft.getReporter();
    }

    public static String getReporterName() {
        if (craft.getReporter() != null)
            return craft.getReporter().getReporterName();
        return null;
    }

}
