package com.weiweizhang.musicplayer.adapter;


import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.widget.ImageButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;
import com.weiweizhang.musicplayer.services.MusicService;

import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {
    private Context mContext;
    public List<Audio> mList;
    public int prePlayPosition = -1;
    boolean serviceBound = false;

    public MusicService musicService = null;
    private Audio preAudio = null;

    public LocalMusicAdapter(Context context, int layoutResId, List data) {
        super(layoutResId, data);
        mContext = context;
        mList = data;
        Intent playerIntent = new Intent(mContext, MusicService.class);
        if(!serviceBound) {
            mContext.startService(playerIntent);
            mContext.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
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
            if(isPlaying1) {
                item.setIsplaying(false);
                this.setData(getAudioPlayingIndex(item.getId()),item);
                musicService.pauseMedia();
            } else {
                item.setIsplaying(true);
                prePlayPosition = getAudioPlayingIndex(item.getId());
                this.setData(getAudioPlayingIndex(item.getId()),item);
                if(CheckIsResume(item)){
                    musicService.resumeMedia();
                } else {
                    musicService.playMedia(item);
                    musicService.audioIndex = prePlayPosition;
                }
            }
            preAudio = item;
        });
    }

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

    public void resetPrePlayState(int prePlayPosition) {
        if(prePlayPosition != -1) {
            Audio audio = mList.get(prePlayPosition);
            audio.setIsplaying(false);
            setData(prePlayPosition, audio);
        }
    }

    public int getAudioPlayingIndex(String id) {
        int index = 0;
        for (Audio a : mList) {
            if(a.getId() == id){
                return index;
            }
            if(a.getId().equals(id)){
                return index;
            }
            index++;
        }
        return index;
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            musicService.setMusicList(mList);
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
}
