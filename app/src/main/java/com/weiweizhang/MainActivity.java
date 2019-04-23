package com.weiweizhang;

import android.os.Bundle;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.ui.fragments.MainFragment;

import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportActivity;

public class MainActivity extends SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(findFragment(MainFragment.class) == null) {
            loadRootFragment(R.id.fl_container, MainFragment.newInstance());
        }
    }
}
