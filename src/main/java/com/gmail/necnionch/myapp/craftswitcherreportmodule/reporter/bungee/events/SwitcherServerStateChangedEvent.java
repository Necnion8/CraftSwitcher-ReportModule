package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.events;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.SwitcherServer;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;
import net.md_5.bungee.api.plugin.Event;

public class SwitcherServerStateChangedEvent extends Event {
    private SwitcherServer server;

    public SwitcherServerStateChangedEvent(SwitcherServer server) {
        this.server = server;
    }

    public SwitcherServer getServer() {
        return server;
    }

    public ServerState getState() {
        return server.getState();
    }

}
