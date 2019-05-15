package com.weiweizhang.musicplayer.ui.fragments;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.SegmentTabLayout;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends SupportFragment {

    private String[] mTitles = {"本地音乐", "在线音乐"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();

    @BindView(R2.id.toolbar)
    public Toolbar mToolbar = null;

    public static DrawerLayout drawerLayout = null;

    public MainFragment() {
        // Required empty public constructor
        mFragments.add(new LocalMusicFragment());
        mFragments.add(new OnLineMucisFragment());
    }

    public static ISupportFragment newInstance(DrawerLayout Drawer) {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        drawerLayout = Drawer;
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        SegmentTabLayout tabLayout = view.findViewById(R.id.tab);
        tabLayout.setTabData(mTitles, this.getActivity(), R.id.fragment_container, mFragments);
        ButterKnife.bind(this, view);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }

            }
        });

        return view;
    }

}
