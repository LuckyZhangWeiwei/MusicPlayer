package com.weiweizhang.musicplayer.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weiweizhang.musicplayer.R;

import me.yokeyword.fragmentation.SupportFragment;

public class MusicFragment extends SupportFragment {
    private static final String MUSICID = "MUSICID";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    public static MusicFragment create(@NonNull int musicID) {
        final Bundle args = new Bundle();
        args.putInt(MUSICID, musicID);
        final MusicFragment musicFragment = new MusicFragment();
        musicFragment.setArguments(args);
        return musicFragment;
    }
}
