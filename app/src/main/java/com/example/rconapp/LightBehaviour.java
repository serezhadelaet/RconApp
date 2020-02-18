package com.example.rconapp;

import java.util.Timer;
import java.util.TimerTask;

public class LightBehaviour {

    private Timer timer;

    public LightBehaviour() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 2000, 2000);
    }

    public void update() {

    }

    public void destroy() {
        timer.cancel();
    }

}
