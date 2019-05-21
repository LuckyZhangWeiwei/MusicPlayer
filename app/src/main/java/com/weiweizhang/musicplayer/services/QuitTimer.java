package com.weiweizhang.musicplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MenuItem;

import com.weiweizhang.musicplayer.R;

import java.util.Objects;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_DOWN_COUNT;

public class QuitTimer {
    private Context context;
    private MenuItem menuItem;
    private String tipDes;


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
        IntentFilter filter = new IntentFilter("your_package_name.countdown_br");
        Objects.requireNonNull(context).registerReceiver(updateUI, filter);
        return this;
    }

    private BroadcastReceiver updateUI;
    {
        updateUI = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String desc = intent.getStringExtra("downcount");
                menuItem.setTitle(desc);
            }
        };
    }

    public void start(long milli) {
        Intent i = new Intent(context, MusicService.class);
        i.setAction(ACTION_DOWN_COUNT);
        i.putExtra("mill", milli);
        context.startService(i);
    }

}
