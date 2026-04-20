package com.hitster.dto.game;

public record CardDTO(
    Long songId,
    int year,
    String artist,
    String title
) {}