package com.hitster.dto.game;

public class SongDTO {

    private Long songId;
    private String title;
    private String artist;
    private int releaseYear;
    private String audioUrl;

    public SongDTO() {
    }

    public SongDTO(Long songId, String title, String artist, int releaseYear, String audioUrl) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
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

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}