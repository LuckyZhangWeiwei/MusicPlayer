package com.weiweizhang.musicplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.playerutilities.StorageUtil;
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
    public static final String ACTION_SHOW_PRE = "com.weiweizhang.musicplayer.services.SHOW_PRE";
    public static final String ACTION_SHOW_NEXT = "com.weiweizhang.musicplayer.services.SHOW_NEXT";
    public static final String ACTION_SHOW_PAUSE = "com.weiweizhang.musicplayer.services.SHOW_PAUSE";
    public static final String ACTION_SHOW_PLAY = "com.weiweizhang.musicplayer.services.SHOW_PLAY";
    public static final String ACTION_DESTROY = "com.weiweizhang.musicplayer.services.ACTION_DESTROY";

    private final IBinder iBinder = new LocalBinder();
    public static MediaPlayer mediaPlayer;
    private int audioIndex = -1;
    private Audio activeAudio;
    private List<Audio> audioList;
    private int resumePosition;
    public static Context context;


    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private StorageUtil storage = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        if(context == null) {
            context = getApplicationContext();
        }
        return iBinder;
    }

    public void playMedia(Audio item) {
        activeAudio = item;
        audioIndex = getAudioPlayingIndex(Integer.parseInt(activeAudio.getId()));
        initMediaPlayer(activeAudio);
    }
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCompletion(MediaPlayer mp) {
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
        mediaPlayer.start();
        NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PLAYING);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    @Override
    public void onCreate() {
        NotificationUtility.init(this);
        storage = new StorageUtil(getApplicationContext());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIncomingActions(intent);
        return START_STICKY;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null || transportControls == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_SHOW_PLAY)) {
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
                resumeMedia();
                NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PLAYING);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPause() {
                pauseMedia();
                NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PAUSED);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSkipToNext() {
                skipToNext();
                updateMetaData();
                NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PLAYING);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSkipToPrevious() {
                skipToPrevious();
                updateMetaData();
                NotificationUtility.Notify(getApplicationContext(), activeAudio, PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                NotificationUtility.cancel();
                //Stop the service
                stopSelf();
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
        }
        NotificationUtility.cancel();
        stopForeground(true);
        unregisterReceiver(playNewAudio);
//        storage.clearCachedAudioPlaylist();
        stopSelf();
    }

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
        playMedia(activeAudio);
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
        playMedia(activeAudio);
    }

    private int getAudioPlayingIndex(int audioId) {
        int index = 0;
        for (Audio a : audioList) {
            if(Integer.parseInt(a.getId()) == audioId) {
                return index;
            }
            index++;
        }
        return index;
    }

    private BroadcastReceiver playNewAudio;
    {
        playNewAudio = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                audioIndex = storage.loadAudioIndex();
                if (audioIndex != -1 && audioIndex < audioList.size()) {
                    //index is in a valid range
                    activeAudio = audioList.get(audioIndex);
                } else {
                    stopSelf();
                }
                stopMedia();
                mediaPlayer.reset();
                initMediaPlayer(activeAudio);
                updateMetaData();
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void play(int playIndex) {
        try {
            //Load data from SharedPreferences
            audioList = storage.loadAudio();
            audioIndex = playIndex; //storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        if(mediaSessionManager == null) {
            initMediaSession();
        }
        initMediaPlayer(activeAudio);
    }

}
