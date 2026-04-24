package com.hitster.dto.game;

public record ChallengeResultDTO(
    Long challengerPlayerId,
    Long challengedPlayerId,
    Long songId,
    int suggestedIndex,
    boolean challengeCorrect,
    boolean tokenSpent,
    boolean cardTransferred
) {}
