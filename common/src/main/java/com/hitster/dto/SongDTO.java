package com.hitster.dto;

public class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private int releaseYear;
    private String audioUrl;

    public SongDTO(Long id, String title, String artist, int releaseYear, String audioUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.releaseYear = releaseYear;
        this.audioUrl = audioUrl;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getReleaseYear() { return releaseYear; }
    public String getAudioUrl() { return audioUrl; }
}