package com.hitster.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GuessSongRequestDTO(
    String artist,
    @JsonProperty("song_name") String songName
) {}