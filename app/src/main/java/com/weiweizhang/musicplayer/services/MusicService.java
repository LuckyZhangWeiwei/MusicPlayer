package com.weiweizhang.musicplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener
{
    public static final String ACTION_SHOW_NEXT = "com.weiweizhang.musicplayer.services.SHOW_NEXT";
    public static final String ACTION_SHOW_CURRENT = "com.weiweizhang.musicplayer.services.SHOW_CURRENT";
    public static final String ACTION_SHOW_PAUSE = "com.weiweizhang.musicplayer.services.SHOW_PAUSE";
    public static final String ACTION_SHOW_PLAY = "com.weiweizhang.musicplayer.services.SHOW_PLAY";
    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    public int audioIndex = -1;
    public Audio activeAudio; //an object on the currently playing audio
    private List<Audio> audioList;
    private int resumePosition;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }

    public void playMedia(Audio item) {
        activeAudio = item;
        initMediaPlayer(activeAudio);
    }
    /***************************************************************/
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer = mp;
        skipToNext();
        Intent broadcastIntent = new Intent(ACTION_SHOW_NEXT);
        getApplicationContext().sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PLAYING);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public void setMusicList(List<Audio> mList) {
        this.audioList = mList;
    }

    /***************************************************************/
    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(ACTION_SHOW_PLAY);
        getApplicationContext().registerReceiver(new BroadcastReceiver(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                resumeMedia();
                NotificationUtility.Notify(context, activeAudio, PlaybackStatus.PLAYING);
            }
        }, filter);


        IntentFilter filter2 = new IntentFilter(ACTION_SHOW_PAUSE);
        getApplicationContext().registerReceiver(new BroadcastReceiver(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                pauseMedia();
                NotificationUtility.Notify(context, activeAudio, PlaybackStatus.PAUSED);
            }
        }, filter2);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent broadcastIntent = new Intent(ACTION_SHOW_CURRENT);
        getApplicationContext().sendBroadcast(broadcastIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /*** MediaPlayer actions*/
    private void initMediaPlayer(Audio activeAudio) {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeAudio.getData());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (mediaPlayer == null) return;
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    public void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        //Update stored index
//        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        playMedia(activeAudio);
    }

    public void skipToPrevious() {
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        // Update stored index
        //new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        playMedia(activeAudio);
    }
    /*** MediaPlayer actions*/
}
