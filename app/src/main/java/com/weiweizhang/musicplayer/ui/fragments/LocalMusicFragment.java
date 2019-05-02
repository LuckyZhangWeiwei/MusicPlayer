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
import android.widget.Toast;

import com.choices.divider.DividerItemDecoration;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.adapter.LocalMusicAdapter;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.PermissionHelper;
import com.weiweizhang.musicplayer.playerutilities.PlayerService;
import com.weiweizhang.musicplayer.playerutilities.StorageUtil;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.ui.customerui.AgileDividerLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yokeyword.fragmentation.SupportFragment;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_DESTROY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_NEXT;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PRE;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends SupportFragment {

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";

    @BindView(R2.id.rec_localmusic_list)
    RecyclerView recyclerView;

    private LocalMusicAdapter adapter;
    private boolean serviceBound = false;
    private MusicService musicService = null;
    private ArrayList<Audio> localMusics = null;
    private StorageUtil storage = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);

        storage = new StorageUtil(getContext());

        PermissionHelper permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                localMusics = (ArrayList<Audio>) PlayerService.getAudioList(Objects.requireNonNull(getContext()));
                adapter = new LocalMusicAdapter(R.layout.item_layout, localMusics);
                adapter.setOnItemChildClickListener((adapter, view1, position) -> {
                    if(view1.getId() == R.id.play_pause) {
                        if (!serviceBound) {
                            //Store Serializable audioList to SharedPreferences
                            StorageUtil storage = new StorageUtil(getContext());
                            storage.storeAudio(localMusics);
                            storage.storeAudioIndex(position);

                            Intent playerIntent = new Intent(getContext(), MusicService.class);
                            getContext().startService(playerIntent);
                            getContext().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        } else {
                            //Store the new audioIndex to SharedPreferences
                            StorageUtil storage = new StorageUtil(getContext());
                            storage.storeAudioIndex(position);

                            //Service is active
                            //Send a broadcast to the service -> PLAY_NEW_AUDIO
                            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
                            getContext().sendBroadcast(broadcastIntent);
                        }
                    }
                });

                LinearLayoutManager manager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);
                DividerItemDecoration itemDecoration = new DividerItemDecoration();
                itemDecoration.setDividerLookup(new AgileDividerLookup());
                recyclerView.addItemDecoration(itemDecoration);

                registerReceiver();
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
        unRegisterReceiver();
        super.onDestroy();
    }
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            musicService.setMusicList(adapter.getData());
            serviceBound = true;

            if(musicService != null) {
                int audioIndex = storage.loadAudioIndex();
                Audio audio = adapter.getData().get(audioIndex);
                if(audio != null) {
                    audio.setIsplaying(true);
                    adapter.setData(audioIndex, audio);
                }
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

            }
        };
    }

    private BroadcastReceiver showPause;
    {
        showPause = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }
    private BroadcastReceiver playPre;
    {
        playPre =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }

    private BroadcastReceiver finish;
    {
        finish =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Objects.requireNonNull(getContext()).unbindService(serviceConnection);
                getActivity().finish();
                musicService.stopSelf();
            }
        };
    }

    private BroadcastReceiver showNext;
    {
        showNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
    }

    private void registerReceiver() {
//        String actionString = getActivity().getIntent().getAction();
//        if (actionString.equalsIgnoreCase(ACTION_SHOW_PLAY)) {
//
//        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_PAUSE)) {
//
//        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_NEXT)) {
//
//        } else if (actionString.equalsIgnoreCase(ACTION_SHOW_PRE)) {
//
//        } else if (actionString.equalsIgnoreCase(ACTION_DESTROY)) {
//            Toast.makeText(getContext(), "aaaa", Toast.LENGTH_SHORT).show();
//        }
        IntentFilter filter = new IntentFilter(ACTION_SHOW_NEXT);
        getContext().registerReceiver(showNext, filter);

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
        getContext().unregisterReceiver(showNext);
        getContext().unregisterReceiver(showResume);
        getContext().unregisterReceiver(showPause);
        getContext().unregisterReceiver(playPre);
        getContext().unregisterReceiver(finish);
    }
}
