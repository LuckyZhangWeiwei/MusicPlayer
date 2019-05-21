package com.weiweizhang.musicplayer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.playerutilities.StorageUtil;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;
import com.weiweizhang.utils.SystemUtils;


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
    public static final String ACTION_SHOW_PRE = "com.weiweizhang.musicplayer.services.SHOW_PRE";
    public static final String ACTION_SHOW_NEXT = "com.weiweizhang.musicplayer.services.SHOW_NEXT";
    public static final String ACTION_SHOW_PAUSE = "com.weiweizhang.musicplayer.services.SHOW_PAUSE";
    public static final String ACTION_SHOW_PLAY = "com.weiweizhang.musicplayer.services.SHOW_PLAY";
    public static final String ACTION_DESTROY = "com.weiweizhang.musicplayer.services.ACTION_DESTROY";
    public static final String ACTION_START = "com.weiweizhang.musicplayer.services.ACTION_START";
    public static final String ACTION_DOWN_COUNT = "com.weiweizhang.musicplayer.services.ACTION_DOWN_COUNT";

    private final IBinder iBinder = new LocalBinder();

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private MediaPlayer mediaPlayer;
    private int audioIndex = -1;
    private Audio activeAudio;
    private List<Audio> audioList;
    private int resumePosition;
    private Context context;


    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private StorageUtil storage = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(!mp.isPlaying()) {
            skipToNext();
        }
        Intent broadcastIntent = new Intent(ACTION_SHOW_NEXT);
//        getApplicationContext().sendBroadcast(broadcastIntent);
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
        mediaPlayer.start();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        NotificationUtility.init(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(context == null) {
            context = getApplicationContext();
        }
        storage = new StorageUtil(getApplicationContext());
        audioList = storage.loadAudio();
        activeAudio = storage.loadAudio().get(storage.loadAudioIndex());
        if(mediaSessionManager == null) {
            initMediaSession();
        }
        if(mediaPlayer == null) {
            initMediaPlayer(activeAudio);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null || transportControls == null) return;

        String actionString = playbackAction.getAction();

        if(actionString.equalsIgnoreCase(ACTION_START)) {
            startForeground(1, NotificationUtility.getNotification(
                    context,
                    storage.loadAudio().get(storage.loadAudioIndex()),
                    PlaybackStatus.PAUSED)
            );
        }
        else if (actionString.equalsIgnoreCase(ACTION_SHOW_PLAY)) {
            transportControls.play();
            sendBroadcast(new Intent(ACTION_SHOW_PLAY));
        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_PAUSE)) {
            transportControls.pause();
            sendBroadcast(new Intent(ACTION_SHOW_PAUSE));
        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_NEXT)) {
            transportControls.skipToNext();
            sendBroadcast(new Intent(ACTION_SHOW_NEXT));
        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_PRE)) {
            transportControls.skipToPrevious();
            sendBroadcast(new Intent(ACTION_SHOW_PRE));
        } else if (actionString.equalsIgnoreCase(ACTION_DESTROY)) {
            transportControls.stop();
            sendBroadcast(new Intent(ACTION_DESTROY));
        }
        else if(actionString.equalsIgnoreCase(ACTION_DOWN_COUNT)) {
            long mill = playbackAction.getLongExtra("mill", 0);
            UpdateUI(mill);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSession() {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();

                startForeground(1, NotificationUtility.getNotification(
                        context,
                        storage.loadAudio().get(storage.loadAudioIndex()),
                        PlaybackStatus.PLAYING));

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();

                startForeground(1, NotificationUtility.getNotification(
                        context,
                        storage.loadAudio().get(storage.loadAudioIndex()),
                        PlaybackStatus.PAUSED));

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                startForeground(1, NotificationUtility.getNotification(
                        context,
                        storage.loadAudio().get(storage.loadAudioIndex()),
                        PlaybackStatus.PLAYING));
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSkipToPrevious() {
                skipToPrevious();
                updateMetaData();
                startForeground(1, NotificationUtility.getNotification(
                        context,
                        storage.loadAudio().get(storage.loadAudioIndex()),
                        PlaybackStatus.PLAYING));
            }

            @Override
            public void onStop() {
                onDestroy();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null ) {
            stopMedia();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
        stopForeground(true);
    }

    private void initMediaPlayer(Audio activeAudio) {
        if(mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();


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
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }
        storage.storeAudioIndex(audioIndex);
        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer(activeAudio);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
        storage.storeAudioIndex(audioIndex);
        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer(activeAudio);
    }

    private CountDownTimer countDownTimer = null;
    private String title = "停止定时任务";
    private String tipDes;
    public static final String COUNTDOWN_BR = "your_package_name.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    private void UpdateUI(long milli) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(milli, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tipDes = millisUntilFinished == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", millisUntilFinished);
                Log.d("zww", tipDes);
                bi.putExtra("downcount", tipDes);
                sendBroadcast(bi);
            }

            @Override
            public void onFinish() {
                sendBroadcast(new Intent(ACTION_DESTROY));
                countDownTimer.cancel();
                onDestroy();
            }
        };

        countDownTimer.start();
    }

}
