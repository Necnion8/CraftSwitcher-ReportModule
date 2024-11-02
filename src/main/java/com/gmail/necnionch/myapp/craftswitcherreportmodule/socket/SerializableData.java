package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket;

import java.util.HashMap;

public interface SerializableData {
    HashMap<String, Class<? extends SerializableData>> TYPES = new HashMap<>();
    HashMap<Class<? extends SerializableData>, String> TYPES_TO_KEY = new HashMap<>();

//    String getDataKey();

    static Class<? extends SerializableData> getClass(String key) {
        return TYPES.get(key);
//        switch (key) {
//            case "invalid": return InvalidData.class;
//            case "login": return LoginData.class;
//            case "empty-response": return EmptyResponseData.class;
//
//            case "status": return StatusData.class;
//
//            case "server-start-request": return ServerStartRequest.class;
//            case "server-stop-request": return ServerStopRequest.class;
//            case "server-list-request": return ServerListRequest.class;
//            case "server-restart-request": return ServerRestartRequest.class;
//            case "server-change-state": return ServerChangeStateData.class;
//            case "server-state-request": return ServerStateRequest.class;
//            case "server-add": return ServerAddData.class;
//            case "server-remove": return ServerRemoveData.class;
//
//        }
//        return null;
    }

    static void putType(String key, Class<? extends SerializableData> data) {
        TYPES.put(key, data);
        TYPES_TO_KEY.put(data, key);
    }

}