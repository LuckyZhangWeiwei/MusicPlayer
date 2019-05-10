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

import com.weiweizhang.MainActivity;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.services.MusicService;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_DESTROY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_NEXT;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PRE;

public class NotificationUtility {
    static final int notificationId = 1;
    static String CHANNEL_ID = "CHANNEL_ID";
    static String CHANNEL_NAME = "CHANNEL_NAME";
//    static Context mContext;
    static NotificationManager notificationManager;
    static MusicService mMusicService = null;
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Notify(Context context, Audio activeAudio, PlaybackStatus playbackStatus) {
        if(activeAudio == null) return;
//        mContext = context;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);

        notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(notificationId, getNotification(context,getRemoteViews(context, activeAudio, playbackStatus)));
    }

    private static RemoteViews getRemoteViews(Context context, Audio activeAudio, PlaybackStatus playbackStatus) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.player_notification);
        remoteViews.setTextViewText(R.id.notificationSongName, activeAudio.getTitle());
        remoteViews.setTextViewText(R.id.notificationArtist, activeAudio.getArtist());
        if (playbackStatus == PlaybackStatus.PLAYING) {
            remoteViews.setImageViewResource(R.id.notificationPlayPause ,android.R.drawable.ic_media_pause);
            remoteViews.setOnClickPendingIntent(R.id.notificationPlayPause, playbackAction(context,1));
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            remoteViews.setImageViewResource(R.id.notificationPlayPause ,android.R.drawable.ic_media_play);
            remoteViews.setOnClickPendingIntent(R.id.notificationPlayPause, playbackAction(context,0));
        }

        remoteViews.setOnClickPendingIntent(R.id.notificationFForward, playbackAction(context,2));

        remoteViews.setOnClickPendingIntent(R.id.notificationFRewind, playbackAction(context,3));

        remoteViews.setOnClickPendingIntent(R.id.notificationStop, playbackAction(context,4));
        return remoteViews;
    }

    private static Notification getNotification(Context context,RemoteViews remoteViews){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(null)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setShowWhen(false)
                .setAutoCancel(false)
                .setContentIntent(playbackAction(context,5))
                .setCustomContentView(remoteViews)
                .setPriority(NotificationManager.IMPORTANCE_NONE);
        return notificationBuilder.build();
    }

    public static void cancel() {
        notificationManager.cancel(notificationId);
    }

    private static PendingIntent playbackAction(Context context , int actionNumber) {
        Intent playbackAction = new Intent(context, MusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_SHOW_PLAY);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_SHOW_PAUSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_SHOW_NEXT);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_SHOW_PRE);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 4:
                // Previous destory
                playbackAction.setAction(ACTION_DESTROY);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 5:
                Intent intent = new Intent(context, MainActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return PendingIntent.getActivity(context, actionNumber, intent, 0);
            default:
                break;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void play(Context context , Audio activeAudio, PlaybackStatus playbackStatus) {
        RemoteViews remoteViews = getRemoteViews(context, activeAudio, playbackStatus);
        mMusicService.startForeground(notificationId, getNotification(context,remoteViews));
    }

    public static void init(MusicService musicService) {
        mMusicService = musicService;
    }
}
