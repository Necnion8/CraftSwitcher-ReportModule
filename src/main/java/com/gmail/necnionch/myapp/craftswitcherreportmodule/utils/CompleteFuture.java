package com.gmail.necnionch.myapp.craftswitcherreportmodule.utils;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.exceptions.ResponseError;

public class CompleteFuture {
    private OnDone onDoneHandler = (r) -> {};
    private OnFail onFailHandler = (r) -> {};
    private OnRun onRun;

    public CompleteFuture(OnRun run) {
        this.onRun = run;
    }

    public CompleteFuture done(OnDone handler) {
        this.onDoneHandler = handler;
        return this;
    }

    public CompleteFuture fail(OnFail handler) {
        this.onFailHandler = handler;
        return this;
    }

    public void schedule() {
        if (onRun != null) {
            this.onRun.onRun();
        } else {
            throw new UnsupportedOperationException("Can only be run once");
        }
    }


    public void callDone(SerializableData responseData) {
        try {
            onDoneHandler.onDone(responseData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callFail(ResponseError exception) {
        try {
            onFailHandler.onFail(exception);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface OnDone {
        void onDone(SerializableData responseData);
    }

    public interface OnFail {
        void onFail(ResponseError exception);
    }

    public interface OnRun {
        void onRun();
    }
}
