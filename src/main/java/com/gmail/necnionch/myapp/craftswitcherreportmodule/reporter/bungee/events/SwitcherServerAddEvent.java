package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.events;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.SwitcherServer;
import net.md_5.bungee.api.plugin.Event;

public class SwitcherServerAddEvent extends Event {
    private SwitcherServer server;

    public SwitcherServerAddEvent(SwitcherServer server) {
        this.server = server;
    }

    public SwitcherServer getServer() {
        return server;
    }

}
