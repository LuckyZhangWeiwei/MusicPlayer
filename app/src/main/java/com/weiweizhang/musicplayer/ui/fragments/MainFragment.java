package com.weiweizhang.musicplayer.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.SegmentTabLayout;
import com.weiweizhang.musicplayer.R;

import java.util.ArrayList;

import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends SupportFragment {

    private String[] mTitles = {"本地音乐", "在线音乐"};
    private ArrayList<Fragment> mFragments = new ArrayList<>();

    public MainFragment() {
        // Required empty public constructor
        mFragments.add(new OnLineMucisFragment());
        mFragments.add(new LocalMusicFragment());
    }

    public static ISupportFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        SegmentTabLayout tabLayout = view.findViewById(R.id.tab);
        tabLayout.setTabData(mTitles, this.getActivity(), R.id.fragment_container, mFragments);
        return view;
    }

}
