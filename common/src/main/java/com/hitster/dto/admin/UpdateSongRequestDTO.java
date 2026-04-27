package com.hitster.dto.admin;

public record UpdateSongRequestDTO(
        String title,
        String artist,
        int releaseYear,
        String audioUrl
) {}
