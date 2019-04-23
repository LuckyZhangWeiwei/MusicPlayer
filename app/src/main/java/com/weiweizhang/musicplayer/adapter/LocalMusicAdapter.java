package com.weiweizhang.musicplayer.adapter;


import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weiweizhang.musicplayer.R;
import com.weiweizhang.musicplayer.entries.Audio;

import java.util.List;

public class LocalMusicAdapter extends BaseQuickAdapter<Audio, BaseViewHolder> {
    private Context mContext;
    public LocalMusicAdapter(Context context, int layoutResId, List data) {
        super(layoutResId, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Audio item) {
        helper.setText(R.id.title, item.getTitle());
        helper.itemView.findViewById(R.id.play_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
