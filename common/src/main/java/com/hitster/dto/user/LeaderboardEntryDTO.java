package com.hitster.dto.user;

public record LeaderboardEntryDTO(
    int rank,
    int id,
    String player,
    int winnings
) {}
