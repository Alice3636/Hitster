package com.hitster.dto.game;

import java.util.List;

public record GameStateDTO(
    GamePhase phase,

    int turnNumber,
    int timeLeftSeconds,

    Long activePlayerId,
    Long winnerPlayerId,
    String winnerName,

    CurrentSongDTO currentSong,

    List<PlayerGameStateDTO> players,

    TurnResultDTO lastTurnResult,
    ChallengeStateDTO challengeState,
    ChallengeResultDTO lastChallengeResult
) {}
