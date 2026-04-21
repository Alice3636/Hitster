package com.hitster.dto.user;

public record MatchHistoryDTO(
    String enemyUsername,
    String date,
    String gameStatus
) {}
