package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.utils.ServerState;

public class ServerChangeStateData implements SerializableData {
    public String server;
    public Integer state;

//    @Override
//    public String getDataKey() {
//        return "server-change-state";
//    }

    public static ServerChangeStateData create(ServerState state) {
        ServerChangeStateData request = new ServerChangeStateData();
        request.state = state.ordinal();
        return request;
    }

}

