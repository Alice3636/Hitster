package com.hitster.dto.admin;

import org.springframework.web.multipart.MultipartFile;

public class CreateSongRequestDTO {
    private String title;
    private String artist;
    private int releaseYear;
    private MultipartFile file;

    public CreateSongRequestDTO() {
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

    public MultipartFile getFile() {
        return file;
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

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}