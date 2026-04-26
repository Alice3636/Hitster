package com.hitster.dto.admin;

public class UpdateSongRequestDTO {
    private String title;
    private String artist;
    private int releaseYear;
    private String audioUrl;

    public UpdateSongRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}