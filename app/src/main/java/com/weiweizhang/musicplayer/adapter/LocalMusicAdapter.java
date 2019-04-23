package com.weiweizhang.musicplayer.adapter;


import android.content.Context;
import android.widget.ImageButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;

import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {

    private Context mContext;
    private List<Audio> mList;
    private int prePlayPosition = -1;
    public LocalMusicAdapter(Context context, int layoutResId, List data) {
        super(layoutResId, data);
        mContext = context;
        mList = data;
    }
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
                imageButton.setImageResource(R.drawable.ic_media_play);
                this.setData(getAudioPlayingIndex(item.getId()),item);
            } else {
                item.setIsplaying(true);
                prePlayPosition = getAudioPlayingIndex(item.getId());
                imageButton.setImageResource(R.drawable.ic_media_pause);
                this.setData(getAudioPlayingIndex(item.getId()),item);
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
}
