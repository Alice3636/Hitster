package com.hitster.dto.user;

import java.util.List;

public record LeaderboardResponseDTO(
    List<LeaderboardEntryDTO> entries
) {}
