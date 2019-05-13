package com.weiweizhang;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.ui.fragments.MainFragment;
import com.weiweizhang.musicplayer.ui.fragments.MusicDetailFragment;

import me.yokeyword.fragmentation.SupportActivity;


public class MainActivity extends SupportActivity {

    private boolean isPlayFragmentShow;
    private MusicDetailFragment mMusicDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findFragment(MainFragment.class) == null) {
            loadRootFragment(R.id.fl_container, MainFragment.newInstance());
            parseIntent();
        }
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
        if(mMusicDetailFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        super.onBackPressedSupport();
    }
}
