package com.weiweizhang.musicplayer.ui.navigation;

import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Toast;

import com.weiweizhang.MainActivity;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.services.QuitTimer;
import com.weiweizhang.utils.ToastUtils;

public class NaviMenuExecutor {
    private MainActivity activity;
    private MenuItem mItem;

    public NaviMenuExecutor(MainActivity mainActivity) {
        this.activity = mainActivity;
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_setting_time:
                mItem = item;
                timerDialog();
                return true;
        }
        return false;
    }

    private void timerDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), (dialog, which) -> {
                    int[] times = activity.getResources().getIntArray(R.array.timer_int);
                    startTimer(times[which]);
                })
                .show();
    }

    private void startTimer(int minute) {
        QuitTimer.get().setTextViewListener(mItem).start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtils.show(activity.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastUtils.show(R.string.timer_cancel);
        }
    }
}
