package com.weiweizhang.musicplayer.services;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.utils.SystemUtils;

public class QuitTimer {
    private Context context;
    private MenuItem menuItem;
    private CountDownTimer countDownTimer;
    private String tipDes;
    private static MusicService musicService;

    public static void setService(MusicService service) {
        musicService = service;
    }

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
        this.context = context.getApplicationContext();
        return this;
    }

    public void start(long milli) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(milli, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String title = context.getString(R.string.menu_timer);
                tipDes = millisUntilFinished == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", millisUntilFinished);
                menuItem.setTitle(tipDes);
                Log.d("zww", tipDes);
            }

            @Override
            public void onFinish() {
                musicService.stopSelf();
                tipDes = null;
            }
        }.start();
    }
}
