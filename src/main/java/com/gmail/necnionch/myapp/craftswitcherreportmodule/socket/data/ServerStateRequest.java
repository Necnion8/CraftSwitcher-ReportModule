package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;

public class ServerStateRequest implements SerializableData {
    public String server;
    public Integer state;

    public ServerStateRequest(String server) {
        this.server = server;
    }

//    @Override
//    public String getDataKey() {
//        return "server-state-request";
//    }

    public ServerState getState() {
        return ServerState.valueOf(state);
    }

}
