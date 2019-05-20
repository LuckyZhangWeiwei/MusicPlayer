package com.weiweizhang.musicplayer.ui.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
    static NotificationManager notificationManager;
    static MusicService mMusicService = null;
    static Audio mAudio = null;
    static Thread musicThread = null;
    static Context mContext = null;
    static PlaybackStatus mPlaybackStatus = null;
    static int musicPosition = 0;
    static boolean serviceBound = false;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void Notify(Context context, Audio activeAudio, PlaybackStatus playbackStatus) {
        if(activeAudio == null) return;

        mContext = context;
        mAudio = activeAudio;
        mPlaybackStatus = playbackStatus;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);

        notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(notificationId, getNotification(context, activeAudio, playbackStatus));

//        if(musicThread == null) {
//            musicThread = new Thread(() -> {
//                while (mMusicService.getMediaPlayer() != null) {
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    handler.sendEmptyMessage(0);
//                }
//            });
//            musicThread.start();
//        }

    }

    private static RemoteViews getRemoteViews(Context context, Audio activeAudio, PlaybackStatus playbackStatus, int currentPosition) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.player_notification);

        remoteViews.setTextViewText(R.id.notificationSongName, activeAudio.getTitle());

        remoteViews.setTextViewText(R.id.notificationArtist, activeAudio.getArtist());

        remoteViews.setProgressBar(R.id.pb_play_bar, activeAudio.getDuration(), currentPosition, false);

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

    public static Notification getNotification(Context context, Audio activeAudio, PlaybackStatus playbackStatus){
        RemoteViews remoteViews = getRemoteViews(context, activeAudio, playbackStatus, musicPosition);

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
                intent.putExtra("EXTRA_NOTIFICATION", true);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            default:
                break;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void play(Context context , Audio activeAudio, PlaybackStatus playbackStatus) {
        mMusicService.startForeground(notificationId, getNotification(context, activeAudio, playbackStatus));
    }

    public static void init(Context context) {
        if(!serviceBound) {
            Intent playerIntent = new Intent(context, MusicService.class);
            context.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private static Handler handler;

    static {

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (mMusicService != null
                        &&
                        mPlaybackStatus != PlaybackStatus.PAUSED) {
                    musicPosition = mMusicService.getMediaPlayer().getCurrentPosition();
                    notificationManager.notify(
                            notificationId,
                            getNotification(mContext, mAudio, mPlaybackStatus)
                    );
                }
            }
        };
    }

    public static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mMusicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
}
