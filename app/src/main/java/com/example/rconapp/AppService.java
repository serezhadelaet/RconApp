package com.example.rconapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.util.List;

public class AppService extends Service {

    private static AppService Instance;

    private static Handler handler = new Handler(Looper.getMainLooper());

    private static boolean isEnabled = true;

    public static AppService getInstance() {
        return Instance;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (getInstance() == null) {
            Instance = this;
        }
        if (MainActivity.getInstance() == null)
            setEnable();
        else
            setDisable();
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }

    public static void setEnable(){
        if (getInstance() == null) return;
        isEnabled = true;
        runRcons();
    }

    public static void setDisable(){
        if (getInstance() == null) return;
        isEnabled = false;

        History.sendDataMessages();

    }

    private static void runRcons() {

        // Here we saying all next config iterations will be provided by this context
        Config.setAsContentOwner(getInstance());
        RconManager.removeAll();

        Config config = Config.getConfig();
        for (int i = 0; i < config.getServerList().size(); i++) {
            Server server = config.getServerList().get(i);
            RconManager.addAsService(server);
        }
    }

    @Override
    public void onDestroy(){
        RconManager.removeAll();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
