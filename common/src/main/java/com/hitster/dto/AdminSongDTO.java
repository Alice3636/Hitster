package com.hitster.dto;

public class AdminSongDTO {

    private Long songId;
    private String title;
    private String artist;
    private int year;
    private String audioUrl;

    public AdminSongDTO() {
    }

    public AdminSongDTO(Long songId, String title, String artist, int year, String audioUrl) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.audioUrl = audioUrl;
    }

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}