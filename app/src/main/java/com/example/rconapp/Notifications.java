package com.example.rconapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;

public class Notifications {

    // TODO: Use notification channels instead

    private static int notificationId = 2;

    public Notifications() {

    }

    public static long lastMessageAmount = 0;

    public static void updateOnGoingNotification(long messagesAmount){
        int current, all;
        if (messagesAmount > 0)
            lastMessageAmount = messagesAmount;
        current = RconManager.GetOverallConnectedServers();
        all = RconManager.Rcons.size();
        String msg = current + "/" + all + " servers";
        if (lastMessageAmount > 0)
            msg+=" | "+ lastMessageAmount + " messages";
        Notifications.CreateOnGoing(AppService.getInstance().getApplicationContext(), msg);
    }

    public static void CreateOnGoing(final Context context, final String text) {
        if (!AppService.isEnabled()) return;
        AppService.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Intent mainActivityIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notify_app)
                                .setContentTitle("RconApp")
                                .setContentText(text)
                                .setOngoing(true)
                                .setContentIntent(pendingIntent);
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, mBuilder.build());
            }
        });
    }

    public static void RemoveOnGoing(final Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void Create(final Context context, final String title, final String text) {
        AppService.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Intent mainActivityIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_notify_app)
                                .setContentTitle(title)
                                .setContentText(text)
                                .setContentIntent(pendingIntent);
                mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
                mBuilder.setVibrate(new long[]{100, 70, 100, 70});
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(notificationId, mBuilder.build());
                notificationId++;
            }
        });

    }
}
