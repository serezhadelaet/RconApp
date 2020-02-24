package rconapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

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
        super.onStartCommand(intent,flags,startId);
        Instance = this;
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
        Notifications.updateOnGoingNotification(0);
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

    private Thread.UncaughtExceptionHandler defaultUEH;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();

            restart();
            System.exit(2);
        }
    };

    private void restart() {
        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                new Intent(getApplicationContext(), AppService.class),
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        RconManager.removeAll();
        super.onTaskRemoved(rootIntent);

        restart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        RconManager.removeAll();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
