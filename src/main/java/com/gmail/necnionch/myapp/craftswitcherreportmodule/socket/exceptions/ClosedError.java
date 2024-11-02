package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.exceptions;

public class ClosedError extends ResponseError {
    public ClosedError() {
        super("client is closed");
    }
}
