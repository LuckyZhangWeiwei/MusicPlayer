package com.weiweizhang.musicplayer.adapter;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;

import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {
    public LocalMusicAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Audio item) {
        helper.setText(R.id.title, item.getTitle());
    }
}
