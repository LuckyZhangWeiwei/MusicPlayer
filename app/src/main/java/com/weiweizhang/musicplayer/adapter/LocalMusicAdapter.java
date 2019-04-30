package com.weiweizhang.musicplayer.adapter;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.ImageButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;

import java.util.List;


public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {

    public LocalMusicAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void convert(BaseViewHolder helper, Audio item) {
        helper.setText(R.id.title, item.getTitle());
        helper.addOnClickListener(R.id.play_pause);
        ImageButton imageButton = helper.itemView.findViewById(R.id.play_pause);
        boolean isPlaying = item.getisIsplaying();
        if (isPlaying) {
            imageButton.setImageResource(R.drawable.ic_media_pause);
        } else {
            imageButton.setImageResource(R.drawable.ic_media_play);
        }
    }
}

