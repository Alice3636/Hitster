package com.hitster.dto.game;

public class CardDTO {
    private Long songId;
    private int year;
    private String artist;
    private String title;

    public CardDTO() {}

    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}