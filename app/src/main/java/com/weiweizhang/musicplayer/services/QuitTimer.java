package com.weiweizhang.musicplayer.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;
import com.weiweizhang.utils.SystemUtils;

public class QuitTimer {
    private Context context;
    private MenuItem menuItem;
    private CountDownTimer countDownTimer;
    private String tipDes;
    private MusicService musicService = null;
    private boolean serviceBound = false;
    private String title;
    private long timerRemain;
    private Handler handler;


    public String getDes() {
        return tipDes == null ? context.getString(R.string.menu_timer) : tipDes;
    }

    public QuitTimer setTextViewListener(MenuItem mItem) {
        menuItem = mItem;
        return this;
    }

    public static QuitTimer get() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final QuitTimer sInstance = new QuitTimer();
    }

    public QuitTimer init(Context context) {
        this.context = context;
        title = context.getString(R.string.menu_timer);
        if(!serviceBound) {
            Intent playerIntent = new Intent(context, MusicService.class);
            context.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        this.handler = new Handler(Looper.getMainLooper());

        return this;
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void start(long milli) {
//        if(countDownTimer != null) {
//            countDownTimer.cancel();
//        }
//
//        countDownTimer = new CountDownTimer(milli, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                tipDes = millisUntilFinished == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", millisUntilFinished);
//                menuItem.setTitle(tipDes);
//            }
//
//            @Override
//            public void onFinish() {
//                if(musicService != null) {
////                    tipDes = null;
////                    musicService.onDestroy();
//                }
//            }
//        }.start();


        if (milli > 0) {
            timerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            handler.post(mQuitRunnable);
        } else {
            timerRemain = 0;
            if(musicService != null)
                musicService.onDestroy();
        }

    }
    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            timerRemain -= DateUtils.SECOND_IN_MILLIS;
            Log.d("zww", timerRemain+"");
            if (timerRemain > 0) {
                handler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            } else {
                if(musicService != null)
                    musicService.onDestroy();
            }
        }
    };
}
