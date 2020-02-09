package com.example.rconapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AppService extends Service {

        public static AppService Instance;
        public static Integer i = 0;
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (Instance == null) {
                Instance = this;
                // TODO something
            }
            return Service.START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            //TODO for communication return IBinder implementation
            return null;
        }
}
