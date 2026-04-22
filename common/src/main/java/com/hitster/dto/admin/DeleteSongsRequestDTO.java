package com.hitster.dto.admin;

import java.util.List;

public record DeleteSongsRequestDTO(
        List<Long> songIds
) {}
