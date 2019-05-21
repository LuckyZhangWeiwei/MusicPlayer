package com.weiweizhang.musicplayer.ui.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.choices.divider.DividerItemDecoration;
import com.weiweizhang.MainActivity;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.adapter.LocalMusicAdapter;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PermissionHelper;
import com.weiweizhang.musicplayer.playerutilities.PlaybackStatus;
import com.weiweizhang.musicplayer.playerutilities.PlayerService;
import com.weiweizhang.musicplayer.playerutilities.StorageUtil;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.ui.customerui.AgileDividerLookup;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_DESTROY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_NEXT;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PRE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_START;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends SupportFragment {

    @BindView(R2.id.rec_localmusic_list)
    RecyclerView recyclerView;
    private LocalMusicAdapter adapter;
    private boolean serviceBound = false;
    private MusicService musicService = null;
    private ArrayList<Audio> localMusics = null;
    private boolean isPlaying = false;
    private StorageUtil storage = null;


    @Override
    public void onStart() {
        super.onStart();

        PermissionHelper permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onPermissionGranted() {
                localMusics = (ArrayList<Audio>) PlayerService.getAudioList(Objects.requireNonNull(getContext()));
                adapter = new LocalMusicAdapter(R.layout.item_layout, localMusics);
                StorageUtil storage = new StorageUtil(getContext());
                storage.storeAudio(localMusics);

                if(!serviceBound) {
                    Intent playerIntent = new Intent(getContext(), MusicService.class);
                    getContext().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                }

                registerReceiver();

                Intent intent = new Intent(getContext() ,MusicService.class);

                adapter.setOnItemChildClickListener((adapter, view1, position) -> {
                    if(view1.getId() == R.id.play_pause) {
                        int storedIndex = storage.loadAudioIndex();
                        if(storedIndex != position) {

                            intent.setAction(ACTION_SHOW_PLAY);
                            getContext().startService(intent);

                            if(storedIndex!=-1) {
                                Audio lastAudio = (Audio) adapter.getData().get(storedIndex);
                                lastAudio.setIsplaying(false);
                                adapter.setData(storedIndex, lastAudio);
                            }

                            storage.storeAudioIndex(position);
                            int activeIndex = storage.loadAudioIndex();
                            Audio activeAudio = (Audio) adapter.getData().get(activeIndex);
                            activeAudio.setIsplaying(true);
                            adapter.setData(activeIndex, activeAudio);

                            isPlaying = true;
                        } else {
                            if(isPlaying) {
                                musicService.pauseMedia();
                                if(storedIndex != -1) {
                                    Audio activeAudio = (Audio) adapter.getData().get(storedIndex);
                                    activeAudio.setIsplaying(false);
                                    adapter.setData(storedIndex, activeAudio);
                                }
                                isPlaying = false;

                                musicService.startForeground(1, NotificationUtility.getNotification(
                                        getContext(),
                                        storage.loadAudio().get(storage.loadAudioIndex()),
                                        PlaybackStatus.PAUSED)
                                );


//                                NotificationUtility.play(getContext(), localMusics.get(position), PlaybackStatus.PAUSED);
                                intent.setAction(ACTION_SHOW_PLAY);
                                getContext().startService(intent);

                            } else {
                                musicService.resumeMedia();
                                if(storedIndex != -1) {
                                    Audio activeAudio = (Audio) adapter.getData().get(storedIndex);
                                    activeAudio.setIsplaying(true);
                                    adapter.setData(storedIndex, activeAudio);
                                }

                                isPlaying = true;

                                musicService.startForeground(1, NotificationUtility.getNotification(
                                        getContext(),
                                        storage.loadAudio().get(storage.loadAudioIndex()),
                                        PlaybackStatus.PLAYING)
                                );
//                                NotificationUtility.play(getContext(), localMusics.get(position), PlaybackStatus.PLAYING);

                                intent.setAction(ACTION_SHOW_PLAY);
                                getContext().startService(intent);

                            }
                        }
                    }
                });

                adapter.setOnItemClickListener((adapter, view12, position) -> {

                });

                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
                DividerItemDecoration itemDecoration = new DividerItemDecoration();
                itemDecoration.setDividerLookup(new AgileDividerLookup());
                recyclerView.addItemDecoration(itemDecoration);



                intent.setAction(ACTION_START);

                getContext().startService(intent);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);
        storage = new StorageUtil(getContext());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            serviceBound = true;

            int activeIndex = storage.loadAudioIndex();
            if(activeIndex != -1) {
                Audio activeAudio = adapter.getData().get(activeIndex);
                activeAudio.setIsplaying(true);
                adapter.setData(activeIndex, activeAudio);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private BroadcastReceiver showResume;
    {
        showResume = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int activeIndex = storage.loadAudioIndex();
                Audio activeAudio = adapter.getData().get(activeIndex);
                activeAudio.setIsplaying(true);
                adapter.setData(activeIndex, activeAudio);
            }
        };
    }
    private BroadcastReceiver showPause;
    {
        showPause = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int activeIndex = storage.loadAudioIndex();
                Audio activeAudio = adapter.getData().get(activeIndex);
                activeAudio.setIsplaying(false);
                adapter.setData(activeIndex, activeAudio);
            }
        };
    }
    private BroadcastReceiver playPre;
    {
        playPre =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int activeIndex = storage.loadAudioIndex();
                Audio activeAudio = adapter.getData().get(activeIndex);
                activeAudio.setIsplaying(true);
                adapter.setData(activeIndex, activeAudio);


                int nextActiveIndex = storage.loadAudioIndex() + 1;
                if(nextActiveIndex <= adapter.getData().size()-1) {
                    Audio nextActiveAudio = adapter.getData().get(nextActiveIndex);
                    nextActiveAudio.setIsplaying(false);
                    adapter.setData(nextActiveIndex, nextActiveAudio);
                }
            }
        };
    }
    private BroadcastReceiver finish;
    {
        finish =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                musicService.unbindService(serviceConnection);
                musicService.stopSelf();
                getActivity().finish();
            }
        };
    }
    private BroadcastReceiver showNext;
    {
        showNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int activeIndex = storage.loadAudioIndex();
                Audio activeAudio = adapter.getData().get(activeIndex);
                activeAudio.setIsplaying(true);
                adapter.setData(activeIndex, activeAudio);


                int preActiveIndex = storage.loadAudioIndex() - 1;
                if(preActiveIndex >= 0 ) {
                    Audio preActiveAudio = adapter.getData().get(preActiveIndex);
                    preActiveAudio.setIsplaying(false);
                    adapter.setData(preActiveIndex, preActiveAudio);
                }
            }
        };
    }
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_SHOW_NEXT);
        Objects.requireNonNull(getContext()).registerReceiver(showNext, filter);

        IntentFilter filter3 = new IntentFilter(ACTION_SHOW_PLAY);
        getContext().registerReceiver(showResume, filter3);


        IntentFilter filter4 = new IntentFilter(ACTION_SHOW_PAUSE);
        getContext().registerReceiver(showPause, filter4);

        IntentFilter filter6 = new IntentFilter(ACTION_SHOW_PRE);
        getContext().registerReceiver(playPre, filter6);

        IntentFilter filter7 = new IntentFilter(ACTION_DESTROY);
        getContext().registerReceiver(finish, filter7);
    }

    private void  unRegisterReceiver() {
        Objects.requireNonNull(getContext()).unregisterReceiver(showNext);
        getContext().unregisterReceiver(showResume);
        getContext().unregisterReceiver(showPause);
        getContext().unregisterReceiver(playPre);
        getContext().unregisterReceiver(finish);
    }

}
