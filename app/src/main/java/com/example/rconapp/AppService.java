package com.example.rconapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AppService extends Service {

        public static AppService Instance;
        public static boolean isEnabled = false;
        public static Integer i = 0;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            // Here we saying all next config iterations will be provided by this context
            // Config.contextOwner = getApplicationContext();

            if (Instance == null) {
                Instance = this;
                // TODO something

            }
            return Service.START_STICKY;
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
            Config config = Config.getConfig();
            for (int i = 0; i < config.ServerList.size(); i++) {
                Config.Server server = config.ServerList.get(i);
                RconManager.addAsService(server);
            }
            Notifications.Create(Instance, "Rcons: " + RconManager.Rcons.size(), "Runned");
        }

        @Override
        public IBinder onBind(Intent intent) {

            return null;
        }
}
