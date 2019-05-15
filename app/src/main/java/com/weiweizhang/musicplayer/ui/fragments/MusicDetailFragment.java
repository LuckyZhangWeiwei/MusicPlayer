package com.weiweizhang.musicplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.ui.base.BaseFragment;

import me.yokeyword.fragmentation.SupportFragment;

public class MusicDetailFragment extends BaseFragment //SupportFragment
{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        return view;
    }

    public static MusicDetailFragment newInstance() {
        MusicDetailFragment fragment = new MusicDetailFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
