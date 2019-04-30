package com.weiweizhang.musicplayer.ui.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.choices.divider.DividerItemDecoration;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.adapter.LocalMusicAdapter;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PermissionHelper;
import com.weiweizhang.musicplayer.playerutilities.PlayerService;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.ui.customerui.AgileDividerLookup;

import java.util.List;
import java.util.Objects;

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

    private LocalMusicAdapter adapter;
    private boolean serviceBound = false;
    private MusicService musicService = null;
    private Audio currentAudio = null;
    private Audio preAudio = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.registerReceiver();
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);

        Intent playerIntent = new Intent(getContext(), MusicService.class);
        if(!serviceBound) {
            Objects.requireNonNull(getContext()).startService(playerIntent);
            getContext().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        PermissionHelper permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                List<Audio> localMusics = PlayerService.getAudioList(Objects.requireNonNull(getContext()));
                adapter = new LocalMusicAdapter(R.layout.item_layout, localMusics);
                adapter.setOnItemChildClickListener((adapter, view1, position) -> {
                    if(view1.getId() == R.id.play_pause) {
                        currentAudio = (Audio) adapter.getData().get(position);
                        resetPrePlayState(preAudio);
                        if(currentAudio.getisIsplaying()) { // pause
                            currentAudio.setIsplaying(false);
                            adapter.setData(getAudioPlayingIndex(Integer.parseInt(currentAudio.getId())), currentAudio);
                            musicService.pauseMedia();
                        } else {
                            currentAudio.setIsplaying(true);
                            adapter.setData(getAudioPlayingIndex(Integer.parseInt(currentAudio.getId())), currentAudio);
                            if(CheckIsResume(currentAudio)) {  //resume
                                musicService.resumeMedia();
                            } else { // play
                                musicService.playMedia(currentAudio);
                            }
                            preAudio = currentAudio;
                        }
                    }
                });

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
        if(serviceConnection != null) {
            Objects.requireNonNull(getContext()).unbindService(serviceConnection);
        }
        unRegisterReceiver();
        super.onDestroy();
    }

    private BroadcastReceiver showNext;
    {
        showNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Audio activeAudio = musicService.activeAudio;

                int preIndex = getAudioPlayingIndex(Integer.parseInt(activeAudio.getId())) - 1;
                if (preIndex < 0) {
                    preIndex = 0;
                }
                Audio preAudio = adapter.getData().get(preIndex);
                preAudio.setIsplaying(false);
                adapter.setData(preIndex, preAudio);
                activeAudio.setIsplaying(true);
                int index = getAudioPlayingIndex(Integer.parseInt(activeAudio.getId()));
                adapter.setData(index, activeAudio);
            }
        };
    }
    private BroadcastReceiver showResume;
    {
        showResume = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (musicService != null) {
                    Audio activeAudio = musicService.activeAudio;
                    if (activeAudio != null) {
                        int index = getAudioPlayingIndex(Integer.parseInt(activeAudio.getId()));
                        activeAudio.setIsplaying(true);
                        adapter.setData(index, activeAudio);
                    }
                }
            }
        };
    }

    private BroadcastReceiver showPause;
    {
        showPause = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (musicService != null) {
                    Audio activeAudio = musicService.activeAudio;
                    if (activeAudio != null) {
                        int index =  getAudioPlayingIndex(Integer.parseInt(activeAudio.getId()));
                        activeAudio.setIsplaying(false);
                        adapter.setData(index, activeAudio);
                    }
                }
            }
        };
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(MusicService.ACTION_SHOW_NEXT);
        getContext().registerReceiver(showNext, filter);

        IntentFilter filter3 = new IntentFilter(ACTION_SHOW_PLAY);
        getContext().registerReceiver(showResume, filter3);


        IntentFilter filter4 = new IntentFilter(ACTION_SHOW_PAUSE);
        getContext().registerReceiver(showPause, filter4);
    }

    private void unRegisterReceiver() {
        getContext().unregisterReceiver(showNext);
        getContext().unregisterReceiver(showResume);
        getContext().unregisterReceiver(showPause);
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            musicService.setMusicList(adapter.getData());
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private boolean CheckIsResume(Audio item) {
        if(preAudio == null) {
            return false;
        }
        else{
            if(preAudio.getId() == item.getId()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void resetPrePlayState(Audio preAudio) {
        if(preAudio != null) {
            int prePlayPosition = getAudioPlayingIndex(Integer.parseInt(preAudio.getId()));
            Audio audio = adapter.getData().get(prePlayPosition);
            audio.setIsplaying(false);
            adapter.setData(prePlayPosition, audio);
        }
    }

    private int getAudioPlayingIndex(int audioId) {
        int index = 0;
        for (Audio a : adapter.getData()) {
            if(Integer.parseInt(a.getId()) == audioId) {
                return index;
            }
            index++;
        }
        return index;
    }
}
