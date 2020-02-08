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
                Update();
            }
        }, 2000, 2000);
    }

    public void Update() {

    }

    public void Destroy() {
        timer.cancel();
    }

}