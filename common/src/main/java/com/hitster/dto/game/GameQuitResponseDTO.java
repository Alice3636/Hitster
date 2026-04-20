package com.hitster.dto.game;

public record GameQuitResponseDTO(
    String gameId,
    String gameStatus,
    String winnerName,
    String message
) {}