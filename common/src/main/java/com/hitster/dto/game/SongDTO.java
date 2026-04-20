package com.hitster.dto.game;

public record SongDTO(
    Long songId,
    String title,
    String artist,
    int releaseYear,
    String audioUrl
) {}