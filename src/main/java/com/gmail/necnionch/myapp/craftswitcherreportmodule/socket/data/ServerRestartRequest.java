package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.google.gson.annotations.SerializedName;

public class ServerRestartRequest implements SerializableData {
    @SerializedName("target_server")
    public String targetServer;
    public boolean success;
    @SerializedName("fail_message")
    public String failMessage;

//    @Override
//    public String getDataKey() {
//        return "server-restart-request";
//    }

    public static ServerRestartRequest create(String server) {
        ServerRestartRequest request = new ServerRestartRequest();
        request.targetServer = server;
        return request;
    }

    public String getLocalizedFailMessage() {
        if (failMessage == null)
            return "不明なエラーが発生しました。";
        switch (failMessage) {
            case "not found server":
                return "そのサーバーはありません。";
            case "stopped server":
                return "起動していません。";
            case "timeout stopping":
                return "停止中に待機時間を超えたため中止されました。";
            case "processing":
                return "サーバーは処理中です。";
        }
        return failMessage;
    }

}
