package com.hitster.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaceSongRequestDTO(
    @JsonProperty("index_position") int indexPosition,
    Long songId
) {}