package com.weiweizhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.services.QuitTimer;
import com.weiweizhang.musicplayer.ui.fragments.MainFragment;
import com.weiweizhang.musicplayer.ui.fragments.MusicDetailFragment;
import com.weiweizhang.musicplayer.ui.navigation.NaviMenuExecutor;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportActivity;


public class MainActivity extends SupportActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private boolean isPlayFragmentShow;
    private MusicDetailFragment mMusicDetailFragment;
    private NaviMenuExecutor mNavMenuExecutor;

    @BindView(R2.id.nav_view)
    public NavigationView navigationView;

    @BindView(R2.id.drawer_layout)
    public DrawerLayout drawerLayout;

    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(findFragment(MainFragment.class) == null) {
            loadRootFragment(R.id.fl_container, MainFragment.newInstance(drawerLayout));
            parseIntent();
        }

        MenuItem downCountMenuItem = navigationView.getMenu().findItem(R.id.nav_setting_time);
        navigationView.setNavigationItemSelectedListener(this);
        QuitTimer.get().init(getApplicationContext()).setTextViewListener(downCountMenuItem);
        downCountMenuItem.setTitle(QuitTimer.get().getDes());

        mNavMenuExecutor = new NaviMenuExecutor(this);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if(intent.hasExtra("EXTRA_NOTIFICATION")) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mMusicDetailFragment == null) {
            mMusicDetailFragment = new MusicDetailFragment();
            ft.replace(android.R.id.content, mMusicDetailFragment);
        } else {
            ft.show(mMusicDetailFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mMusicDetailFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressedSupport() {
        if(drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
            return;
        }
        if(mMusicDetailFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        super.onBackPressedSupport();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                menuItem.setChecked(false);
            }
        }, 500);
        return mNavMenuExecutor.onNavigationItemSelected(menuItem);
    }

}
