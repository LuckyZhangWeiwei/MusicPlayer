package com.weiweizhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.services.QuitTimer;
import com.weiweizhang.musicplayer.ui.base.BaseActivity;
import com.weiweizhang.musicplayer.ui.fragments.MainFragment;
import com.weiweizhang.musicplayer.ui.fragments.MusicDetailFragment;
import com.weiweizhang.musicplayer.ui.navigation.NaviMenuExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity
        implements
        NavigationView.OnNavigationItemSelectedListener {

    private NaviMenuExecutor mNavMenuExecutor;
    @BindView(R2.id.nav_view)
    public NavigationView navigationView;
    @BindView(R2.id.drawer_layout)
    public DrawerLayout drawerLayout;
    private Handler handler;
    private MainFragment mainFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if(findFragment(MainFragment.class) == null) {
            mainFragment = (MainFragment) MainFragment.newInstance(drawerLayout);
            loadRootFragment(R.id.fl_container, mainFragment);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        MenuItem downCountMenuItem = navigationView.getMenu().findItem(R.id.nav_setting_time);
        navigationView.setNavigationItemSelectedListener(this);
        QuitTimer.get().init(getApplicationContext()).setTextViewListener(downCountMenuItem);
        downCountMenuItem.setTitle(QuitTimer.get().getDes());

        mNavMenuExecutor = new NaviMenuExecutor(this);
        handler = new Handler(Looper.getMainLooper());
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        handler.postDelayed(() -> menuItem.setChecked(false), 500);
        return mNavMenuExecutor.onNavigationItemSelected(menuItem);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if(intent.hasExtra("EXTRA_NOTIFICATION")) {
            mainFragment.start(new MusicDetailFragment());
            setIntent(new Intent());
        }
    }

}
