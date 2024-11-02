package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.google.gson.annotations.SerializedName;

public class ServerStartRequest implements SerializableData {
    @SerializedName("target_server")
    public String targetServer;
    public boolean success;
    @SerializedName("fail_message")
    public String failMessage;

//    @Override
//    public String getDataKey() {
//        return "server-start-request";
//    }

    public static ServerStartRequest create(String server) {
        ServerStartRequest request = new ServerStartRequest();
        request.targetServer = server;
        return request;
    }

    public String getLocalizedFailMessage() {
        if (failMessage == null)
            return "不明なエラーが発生しました。";
        switch (failMessage) {
            case "not found server":
                return "そのサーバーはありません。";
            case "already running server":
                return "既に起動しています。";
        }
        return failMessage;
    }

}
