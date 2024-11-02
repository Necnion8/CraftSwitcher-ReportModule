package com.gmail.necnionch.myapp.craftswitcherreportmodule.utils;

import java.util.Timer;
import java.util.TimerTask;

public class SimpleTimer {
    private static Timer timer = new Timer(true);

    public static TimerTask schedule(Runnable task, long delay) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        timer.schedule(timerTask, delay);
        return timerTask;
    }

    public static TimerTask scheduleRepeating(Runnable task, long delay, long period) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        timer.schedule(timerTask, delay, period);
        return timerTask;
    }

}
