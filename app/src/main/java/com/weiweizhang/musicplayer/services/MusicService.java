package com.weiweizhang.musicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.weiweizhang.musicplayer.playerutilities.MediaPlayerHelper;

public class MusicService extends Service {

    private final IBinder iBinder = new LocalBinder();

    private  MediaPlayerHelper mediaPlayerHelper;

    public void setMediaPlayerHelper(MediaPlayerHelper mediaPlayerHelper) {
        this.mediaPlayerHelper = mediaPlayerHelper;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
}
