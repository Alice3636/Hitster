package com.hitster.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChallengeRequestDTO(
    @JsonProperty("suggested_index") int suggestedIndex
) {}