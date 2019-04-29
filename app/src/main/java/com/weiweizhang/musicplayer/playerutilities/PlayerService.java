package com.weiweizhang.musicplayer.playerutilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.weiweizhang.musicplayer.entries.Audio;

import java.util.ArrayList;
import java.util.List;

public class PlayerService {
    public static List<Audio> getAudioList(Context context) {
        List<Audio> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration =cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                if(duration != null && duration != "" && Integer.parseInt(duration) > 100000)
//                if(duration != null && duration != "" && Integer.parseInt(duration) < 50000)
                list.add(new Audio(id ,data, title, album, artist, false));
            }
        }
        cursor.close();
        return list;
    }
}
