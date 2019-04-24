package com.weiweizhang.musicplayer.ui.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.weiweizhang.musicplayer.R;

public class NotificationUtility {
    public static int notificationId = 1;
    static String CHANNEL_ID = "CHANNEL_ID";
    static String CHANNEL_NAME = "CHANNEL_NAME";
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Notify(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.player_notification);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(null)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setShowWhen(false)
                .setAutoCancel(true)
                .setContent(remoteViews)
                .setPriority(NotificationManager.IMPORTANCE_NONE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

}
