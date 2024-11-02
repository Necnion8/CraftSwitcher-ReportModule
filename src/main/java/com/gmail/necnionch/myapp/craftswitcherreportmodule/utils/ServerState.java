package com.gmail.necnionch.myapp.craftswitcherreportmodule.utils;

public enum ServerState {
    STOPPED(0),
    STARTED(1),
    STARTING(2),
    STOPPING(3),
    RUNNING(4),
    UNKNOWN(-1);

    public int state;
    ServerState(int state) {
        this.state = state;
    }

    public static ServerState valueOf(int state) {
        switch (state) {
            case 0:
                return STOPPED;
            case 1:
                return STARTED;
            case 2:
                return STARTING;
            case 3:
                return STOPPING;
            case 4:
                return RUNNING;
            default:
                return UNKNOWN;
        }
    }

}
