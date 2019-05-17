package com.weiweizhang.musicplayer.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.weiweizhang.MainActivity;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.R2;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.StorageUtil;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.services.QuitTimer;
import com.weiweizhang.musicplayer.ui.base.BaseFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.SupportFragmentDelegate;

import static com.weiweizhang.musicplayer.services.MusicService.ACTION_DESTROY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_NEXT;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PAUSE;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PLAY;
import static com.weiweizhang.musicplayer.services.MusicService.ACTION_SHOW_PRE;


public class MusicDetailFragment extends SupportFragment //BaseFragment
{
    private StorageUtil storage = null;

    private Audio currentAudio = null;

    @BindView(R2.id.music_bg_imgv)
    public ImageView ivG = null;

    @BindView(R2.id.music_title_tv)
    public TextView tvTitle = null;

    @BindView(R2.id.music_artist_tv)
    public TextView tvArtist = null;

    @BindView(R2.id.music_disc_imagv)
    public ImageView ivRound = null;

    @BindView(R2.id.music_needle_imag)
    public ImageView ivNeed = null;

    @BindView(R2.id.music_current_tv)
    public TextView tvCurrentTime = null;

    @BindView(R2.id.music_seekbar)
    public SeekBar sbMusicBar = null;

    @BindView(R2.id.music_total_tv)
    public TextView tvTotalTime = null;

    @BindView(R2.id.music_play_btn_loop_img)
    public ImageView ivLoop = null;

    @BindView(R2.id.music_prev_imgv)
    public ImageView ivPre = null;

    @BindView(R2.id.music_next_imgv)
    public ImageView ivNext = null;

    @BindView(R2.id.music_pause_imgv)
    public ImageView ivPause = null;

    @OnClick(R2.id.music_down_imgv)
    public void closeFragment(View view) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(this);
        ft.commitAllowingStateLoss();
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        storage = new StorageUtil(getContext());
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, view);
        registerReceiver();
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);

//        MusicService service = ((MainActivity)getActivity()).getMusicService();
//        int position = service.mediaPlayer.getCurrentPosition();

        initView();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void initView() {
        int currentIndex = storage.loadAudioIndex();
        currentAudio = storage.loadAudio().get(currentIndex);
        tvTitle.setText(currentAudio.getTitle());
        tvArtist.setText(currentAudio.getArtist());
//        ivRound.setImageBitmap(currentAudio.getAlbum())
    }

    public static MusicDetailFragment newInstance() {
        MusicDetailFragment fragment = new MusicDetailFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private BroadcastReceiver showNext;
    {
        showNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                initView();
            }
        };
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_SHOW_NEXT);
        Objects.requireNonNull(getContext()).registerReceiver(showNext, filter);
//
//        IntentFilter filter3 = new IntentFilter(ACTION_SHOW_PLAY);
//        getContext().registerReceiver(showResume, filter3);
//
//
//        IntentFilter filter4 = new IntentFilter(ACTION_SHOW_PAUSE);
//        getContext().registerReceiver(showPause, filter4);
//
//        IntentFilter filter6 = new IntentFilter(ACTION_SHOW_PRE);
//        getContext().registerReceiver(playPre, filter6);
//
//        IntentFilter filter7 = new IntentFilter(ACTION_DESTROY);
//        getContext().registerReceiver(finish, filter7);
    }

    private void  unRegisterReceiver() {
        Objects.requireNonNull(getContext()).unregisterReceiver(showNext);
//        getContext().unregisterReceiver(showResume);
//        getContext().unregisterReceiver(showPause);
//        getContext().unregisterReceiver(playPre);
//        getContext().unregisterReceiver(finish);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

}
