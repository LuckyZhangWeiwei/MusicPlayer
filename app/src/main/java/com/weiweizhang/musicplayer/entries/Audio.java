package com.weiweizhang.musicplayer.entries;

import java.io.Serializable;

public class Audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;

    public int getDuration() {
        return duration;
    }

    private int duration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public boolean getisIsplaying() {
        return isplaying;
    }

    public void setIsplaying(boolean isplaying) {
        this.isplaying = isplaying;
    }

    private boolean isplaying;

    public Audio(String id ,String data, String title, String album, String artist, boolean isplaying, int duration) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.isplaying = isplaying;
        this.id = id;
        this.duration = duration;
    }

    public String getData() {
        return data;
    }


    public String getTitle() {
        return title;
    }


    public String getAlbum() {
        return album;
    }


    public String getArtist() {
        return artist;
    }

}

