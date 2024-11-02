package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;

public class InvalidData implements SerializableData {
    public String message;

//    @Override
//    public String getDataKey() {
//        return "invalid";
//    }

    public InvalidData(String message) {
        this.message = message;
    }




}
