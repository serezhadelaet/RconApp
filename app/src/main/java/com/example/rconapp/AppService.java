package com.example.rconapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class AppService extends Service {

        public static AppService Instance;
        private HandlerThread mHandlerThread;
        private Handler mHandler;
        public static boolean isEnabled = false;
        public static Integer i = 0;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // Here we saying all next config iterations will be provided by this context
            Config.contextOwner = getApplicationContext();

            if (Instance == null) {
                Instance = this;
                mHandlerThread = new HandlerThread("LocalServiceThread");
                mHandlerThread.start();

                mHandler = new Handler(mHandlerThread.getLooper());
                // TODO something

            }
            return Service.START_STICKY;
        }

        public void postRunnable(Runnable runnable) {
            mHandler.post(runnable);
        }

        public static void setEnable(){
            if (Instance == null) return;
            isEnabled = true;
            runRcons();
        }

        public static void setDisable(){
            if (Instance == null) return;
            isEnabled = false;
        }

        private static void runRcons() {
            Log.d("d", "runRcons");
            // Here we saying all next config iterations will be provided by this context
            Config.contextOwner = Instance.getApplicationContext();

            Config config = Config.getConfig();
            for (int i = 0; i < config.ServerList.size(); i++) {
                Config.Server server = config.ServerList.get(i);
                RconManager.addAsService(server);
            }
        }

        @Override
        public IBinder onBind(Intent intent) {

            return null;
        }
}
