package com.hitster.dto.user;

import java.util.List;

public record UserProfileResponseDTO(
    String username,
    String email,
    int totalWins,
    double winRate,
    List<MatchHistoryDTO> matchHistory,
    String profilePicturePath
) {}
