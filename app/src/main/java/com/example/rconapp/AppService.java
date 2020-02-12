package com.example.rconapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class AppService extends Service {

        public static AppService Instance;

        public static Handler handler;

        public static class History{
            public Config.Server Server;
            public String Message;

            public History(Config.Server server, String message){
                Server = server;
                Message = message;
            }
        }

        public static List<History> MessagesHistory = new ArrayList<>();
        public static boolean isEnabled;
        public static Integer i = 0;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            isEnabled = false;
            if (Instance == null) {
                Instance = this;
                handler = new Handler(Looper.getMainLooper());
                if (MainActivity.Instance == null)
                    setEnable();
            }
            return Service.START_STICKY;
        }

        public static void runOnUiThread(Runnable runnable){
            handler.post(runnable);
        }

        public static void setEnable(){
            if (Instance == null || isEnabled) return;
            isEnabled = true;
            runRcons();
        }

        public static void setDisable(){
            if (Instance == null || !isEnabled) return;
            isEnabled = false;
            RconManager.removeAll();
        }

        private static void runRcons() {

            // Here we saying all next config iterations will be provided by this context
            Config.contextOwner = Instance.getApplicationContext();

            RconManager.removeAll();

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
