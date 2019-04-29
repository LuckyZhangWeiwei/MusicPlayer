package com.weiweizhang.musicplayer.ui.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.choices.divider.DividerItemDecoration;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.adapter.LocalMusicAdapter;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PermissionHelper;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.playerutilities.PlayerService;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.ui.customerui.AgileDividerLookup;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends SupportFragment {

    @BindView(R2.id.rec_localmusic_list)
    RecyclerView recyclerView;

    private PermissionHelper permissionHelper;
    private LocalMusicAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.registerReceiver();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                List<Audio> localMusics = PlayerService.getAudioList(getContext());
                adapter = new LocalMusicAdapter(getContext() ,R.layout.item_layout, localMusics);
                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
                DividerItemDecoration itemDecoration = new DividerItemDecoration();
                itemDecoration.setDividerLookup(new AgileDividerLookup());
                recyclerView.addItemDecoration(itemDecoration);
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

    @Override
    public void onDestroy() {
        getContext().unbindService(adapter.serviceConnection);
        unRegisterReceiver();
        super.onDestroy();
    }

    private BroadcastReceiver showNext = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Audio activeAudio = adapter.musicService.activeAudio;

            int preIndex = adapter.getAudioPlayingIndex(activeAudio.getId()) - 1;
            if(preIndex < 0) {
                preIndex = 0;
            }
            Audio preAudio = adapter.getData().get(preIndex);
            preAudio.setIsplaying(false);
            adapter.setData(preIndex, preAudio);


            activeAudio.setIsplaying(true);
            int index = adapter.getAudioPlayingIndex(activeAudio.getId());
            adapter.setData(index, activeAudio);
        }
    };

    private BroadcastReceiver showCurrent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicService service = adapter.musicService;
            if(service != null) {
                Audio activeAudio = service.activeAudio;
                if(activeAudio !=null ){
                    int index = adapter.getAudioPlayingIndex(activeAudio.getId());
                    if(activeAudio != null) {
                        if (activeAudio.getisIsplaying()){
                            activeAudio.setIsplaying(true);
                            adapter.prePlayPosition = adapter.getAudioPlayingIndex(activeAudio.getId());
                        } else {
                            activeAudio.setIsplaying(false);
                        }
                        adapter.setData(index, activeAudio);
                    }
                }
            }
        }
    };

    private BroadcastReceiver showResume = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicService service = adapter.musicService;
            if(service != null) {
                Audio activeAudio = service.activeAudio;
                if(activeAudio !=null ){
                    int index = adapter.getAudioPlayingIndex(activeAudio.getId());
                    if(activeAudio != null) {
                        activeAudio.setIsplaying(true);
                        adapter.setData(index, activeAudio);
                    }
                }
            }
        }
    };

    private BroadcastReceiver showPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MusicService service = adapter.musicService;
            if(service != null) {
                Audio activeAudio = service.activeAudio;
                if(activeAudio !=null ){
                    int index = adapter.getAudioPlayingIndex(activeAudio.getId());
                    if(activeAudio != null) {
                        activeAudio.setIsplaying(false);
                        adapter.setData(index, activeAudio);
                    }
                }
            }
        }
    };
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(MusicService.ACTION_SHOW_NEXT);
        getContext().registerReceiver(showNext, filter);

        IntentFilter filter2 = new IntentFilter(MusicService.ACTION_SHOW_CURRENT);
        getContext().registerReceiver(showCurrent, filter2);

        IntentFilter filter3 = new IntentFilter(ACTION_SHOW_PLAY);
        getContext().registerReceiver(showResume, filter3);


        IntentFilter filter4 = new IntentFilter(ACTION_SHOW_PAUSE);
        getContext().registerReceiver(showPause, filter4);
    }

    private void unRegisterReceiver() {
        getContext().unregisterReceiver(showNext);
        getContext().unregisterReceiver(showCurrent);
        getContext().unregisterReceiver(showResume);
        getContext().unregisterReceiver(showPause);
    }


}
