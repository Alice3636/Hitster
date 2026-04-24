package com.hitster.dto.game;

public record TurnResultDTO(
    Long playerId,
    Long songId,
    int placedIndex,

    boolean titleCorrect,
    boolean artistCorrect,
    boolean placementCorrect,

    boolean earnedToken
) {}
