package com.gmail.necnionch.myapp.craftswitcherreportmodule;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerRestartRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerStartRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerStateRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data.ServerStopRequest;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.CompleteFuture;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;

public class SwitcherServer {
    private String id;
    ServerState state;

    private SwitcherServer() {}

    SwitcherServer(ServerStateRequest req) {
        this.id = req.server;
        this.state = ServerState.valueOf(req.state);
    }

    SwitcherServer(String id, ServerState state) {
        this.id = id;
        this.state = state;
    }

    public static SwitcherServer getServer(String id) {
        return CraftSwitcherReporter.getInstance().getServer(id);
    }


    public String getId() {
        return id;
    }

    public ServerState getState() {
        return state;
    }


    public CompleteFuture startFuture() {
        return CraftSwitcherReporter.getInstance().sendDataFuture(ServerStartRequest.create(id));
    }

    public CompleteFuture stopFuture() {
        return CraftSwitcherReporter.getInstance().sendDataFuture(ServerStopRequest.create(id));
    }

    public CompleteFuture restartFuture() {
        return CraftSwitcherReporter.getInstance().sendDataFuture(ServerRestartRequest.create(id));
    }

}
