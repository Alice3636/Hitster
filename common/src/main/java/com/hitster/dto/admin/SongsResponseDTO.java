package com.hitster.dto.admin;

import com.hitster.dto.game.SongDTO;
import java.util.List;

public record SongsResponseDTO(
        List<SongDTO> songs
) {}
