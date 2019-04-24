package com.weiweizhang.musicplayer.ui.customerui;

import android.graphics.Color;

import com.choices.divider.Divider;
import com.choices.divider.DividerItemDecoration;

public class AgileDividerLookup extends DividerItemDecoration.SimpleDividerLookup{
    @Override
    public Divider getHorizontalDivider(int position) {
        return new Divider.Builder().size(2)
                .color(Color.GRAY)
                .build();
    }
}
