package com.weiweizhang.musicplayer.adapter;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.widget.ImageButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.playerutilities.MediaPlayerHelper;
import com.weiweizhang.musicplayer.services.MusicService;
import com.weiweizhang.musicplayer.ui.notification.NotificationUtility;

import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {

    private Context mContext;
    private List<Audio> mList;
    private int prePlayPosition = -1;
    private MusicService player;
    boolean serviceBound = false;
    public LocalMusicAdapter(Context context, int layoutResId, List data) {
        super(layoutResId, data);
        mContext = context;
        mList = data;
    }
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void convert(BaseViewHolder helper, Audio item) {
        helper.setText(R.id.title, item.getTitle());
        ImageButton imageButton = helper.itemView.findViewById(R.id.play_pause);
        boolean isPlaying = item.getisIsplaying();
        if(isPlaying) {
            imageButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            imageButton.setImageResource(R.drawable.ic_media_play);
        }
        imageButton.setOnClickListener(view -> {
            boolean isPlaying1 = item.getisIsplaying();
            item.setIsplaying(false);
            resetPrePlayState(prePlayPosition);

            MediaPlayerHelper  mediaPlayerHelper = new MediaPlayerHelper();
            mediaPlayerHelper.setActiveAudio(item);
            mediaPlayerHelper.setAudioList(mList);
            mediaPlayerHelper.initMediaPlayer();

            if(isPlaying1) {
                item.setIsplaying(false);
                imageButton.setImageResource(R.drawable.ic_media_play);
                this.setData(getAudioPlayingIndex(item.getId()),item);
            } else {
                item.setIsplaying(true);
                prePlayPosition = getAudioPlayingIndex(item.getId());
                imageButton.setImageResource(R.drawable.ic_media_pause);
                this.setData(getAudioPlayingIndex(item.getId()),item);

                //
                NotificationUtility.Notify(mContext);
                //
            }
        });
    }

    private void resetPrePlayState(int prePlayPosition) {
        if(prePlayPosition != -1) {
            Audio audio = mList.get(prePlayPosition);
            audio.setIsplaying(false);
            setData(prePlayPosition, audio);
        }
    }

    private int getAudioPlayingIndex(String id) {
        int index = 0;
        for (Audio a : mList) {
            if(a.getId() == id){
                return index;
            }
            index++;
        }
        return index;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
}
