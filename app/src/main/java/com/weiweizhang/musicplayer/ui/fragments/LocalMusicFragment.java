package com.weiweizhang.musicplayer.ui.fragments;


import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.adapter.LocalMusicAdapter;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PermissionHelper;
import com.weiweizhang.musicplayer.playerutilities.PlayerService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends SupportFragment {

    @BindView(R2.id.rec_localmusic_list)
    RecyclerView recyclerView;

    private PermissionHelper permissionHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                List<Audio> localMusics = PlayerService.getAudioList(getContext());
                LocalMusicAdapter adapter = new LocalMusicAdapter(getContext() ,R.layout.item_layout, localMusics);
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter); // 获取权限后扫描数据库获取信息
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
            }

            @Override
            public void onPermissionDenied() {
            }

            @Override
            public void onPermissionDeniedBySystem() {
            }
        });

        return view;
    }
}
