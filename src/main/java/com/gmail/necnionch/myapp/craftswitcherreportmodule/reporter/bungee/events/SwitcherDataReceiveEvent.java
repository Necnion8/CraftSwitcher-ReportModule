package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter.bungee.events;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import net.md_5.bungee.api.plugin.Event;

public class SwitcherDataReceiveEvent extends Event {
    private SerializableData receiveData;
    private SerializableData responseData;

    public SwitcherDataReceiveEvent(SerializableData received) {
        this.receiveData = received;
    }

    public SerializableData getReceive() {
        return receiveData;
    }

    public SerializableData getResponse() {
        return responseData;
    }

    public void setResponse(SerializableData data) {
        this.responseData = data;
    }

}
