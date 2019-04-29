package com.weiweizhang.musicplayer.ui.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;

public class NotificationUtility {
    public static int notificationId = 1;
    static String CHANNEL_ID = "CHANNEL_ID";
    static String CHANNEL_NAME = "CHANNEL_NAME";
    static Context mContext;
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Notify(Context context, Audio activeAudio, PlaybackStatus playbackStatus) {
        if(activeAudio == null) return;
        mContext = context;
        RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.player_notification);
        remoteViews.setTextViewText(R.id.notificationSongName, activeAudio.getTitle());
        remoteViews.setTextViewText(R.id.notificationArtist, activeAudio.getArtist());
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent pauseIntent = null;
        Intent clickIntent = null;
        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            clickIntent = new Intent(ACTION_SHOW_PAUSE);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            clickIntent = new Intent(ACTION_SHOW_PLAY);
        }
        remoteViews.setImageViewResource(R.id.notificationPlayPause ,notificationAction);
        pauseIntent = PendingIntent.getBroadcast(mContext, 100, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notificationPlayPause, pauseIntent);

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
