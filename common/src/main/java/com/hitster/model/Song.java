package com.hitster.model;

public class Song {
    private final String id;
    private final String title;
    private final String artist;
    private final int year;
    private final String audioUrl;

    public Song(String id, String title, String artist, int year, String audioUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.year = year;
        this.audioUrl = audioUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getYear() {
        return year;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", year=" + year +
                '}';
    }
}