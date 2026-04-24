package com.hitster.dto.game;

public record ChallengeStateDTO(
    Long challengerPlayerId,
    Long challengedPlayerId,
    Long songId,
    int originalPlacedIndex,
    int timeLeftSeconds,
    boolean challengeAvailable
) {}
