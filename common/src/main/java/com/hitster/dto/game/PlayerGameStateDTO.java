package com.hitster.dto.game;

import java.util.List;

public record PlayerGameStateDTO(
    Long playerId,
    String playerName,
    int score,
    int tokens,
    List<CardDTO> timeline
) {}
