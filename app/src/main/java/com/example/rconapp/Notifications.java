package com.example.rconapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

public class Notifications {

    public Notifications(){

    }

    public static void Create(Context context, String title, String text){

        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PendingIntent.FLAG_ONE_SHOT, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_chat_24px)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(pendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }
}
