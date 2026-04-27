package com.hitster.dto.admin;

import java.nio.file.Path;

public record AddSongRequestDTO(
        String title,
        String artist,
        int releaseYear,
        Path audioFile
) {}
